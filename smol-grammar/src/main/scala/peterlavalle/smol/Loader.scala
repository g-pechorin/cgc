package peterlavalle.smol

import java.io.Writer

import peterlavalle.TS
import peterlavalle.smol.SmolIr.TCall

class Loader(m: SmolIr.Module) {
	implicit val module: SmolIr.Module = m
	val examine: Examine = new Examine(module)

	val moduleName: String = module.name.text
	val prefixName: String = module.prefix.text

	def apply[W <: Writer](writer: W): W = {

		writer
			.appund {
				"\n#include \"" + moduleName + ".hpp\"\n\n"
			}
			.appund {
				s"""
					 |void smol_code(size_t count, const char* prefix, void** ptr, void* userdata, void*(*callback)(void*, const char*), const char* allnames);
					 |
					 |$moduleName _$moduleName;
					 |
					 |void $moduleName::def(void* userdata, void*(*callback)(void*, const char*), const char* allnames)
					 |{
					 |	smol_code(
					 |		${examine.allCallable.distinctBy(_.name.text).size},
					 |		"$prefixName",
					 |		reinterpret_cast<void**>(&(_$moduleName)),
					 |		userdata,
					 |		callback,
					 |		allnames
					 |	);
					 |}
					 |
					 |#include <assert.h>
					 |#ifdef WIN32
					 |#	define SMOL_CALL __stdcall*
					 |#else
					 |#	define SMOL_CALL *
					 |#endif
					 |
					""".stripTrim
			}
			.appund(examine.allCallableGroups.toList.sortBy((_: (String, Iterable[SmolIr.TCall]))._1).flatMap { case (g, d) => g :: d.toList.sortBy((_: SmolIr.TCall).name.text) }.filter("" != (_: Object))) {
				case group: String =>
					s"\n\n//\n// $group\n"

				case callable: SmolIr.TCall =>
					callable match {
						case SmolIr.Prototype(code, name, args, kind) =>
							val call: String =
								reinterpretFunctionPointer(
									name,
									"", SmolIr.KVoid,
									args,
									kind
								)

							val (pre: String, end: String) =
								result(kind)

							val head: String =
								s"${Cpp.textForKind(kind)} $moduleName::${code.text}(${Header.argsToArguments(args)})"

							emitCallable(
								callable,
								head,
								pre, call, end
							)

						case SmolIr.Member(typeDef: SmolIr.TypeDef, SmolIr.TypeDef.Constructor(name: TS.Tok, args: List[SmolIr.TCall.Arg])) =>
							val call: String =
								reinterpretFunctionPointer(
									name,
									"", SmolIr.KVoid,
									args,
									typeDef.base
								)

							val (pre: String, end: String) =
								("_this = ", "")

							val head: String =
								s"$moduleName::${typeDef.name.text}::${typeDef.name.text}(${Header.argsToArguments(args)})"

							emitCallable(
								callable,
								head,
								pre, call, end
							)

						case SmolIr.Member(typeDef: SmolIr.TypeDef, SmolIr.TypeDef.Method(code: TS.Tok, name: TS.Tok, args: SmolIr.TCall.Args, kind: SmolIr.TKind)) =>
							val call: String =
								reinterpretFunctionPointer(
									name,
									"_this", typeDef.base,
									args,
									kind
								)

							val (pre: String, end: String) =
								result(kind)

							val head: String =
								s"${Cpp.textForKind(kind)} $moduleName::${typeDef.name.text}::${code.text}(${Header.argsToArguments(args.filterNot(SmolIr.TCall.ThisArg == _))})"

							emitCallable(
								callable,
								head,
								pre, call, end
							)

						case SmolIr.Member(typeDef: SmolIr.TypeDef, SmolIr.TypeDef.Destructor(name: TS.Tok)) =>
							val call: String =
								reinterpretFunctionPointer(
									name,
									"_this", typeDef.base,
									Nil,
									SmolIr.KVoid
								)

							val (pre: String, end: String) =
								("", "")


							emitCallable(
								callable,
								s"void $moduleName::${typeDef.name.text}::_${typeDef.name.text}(void)",
								pre, call, end
							)

					}

			}
	}

	def result(kind: SmolIr.TKind): (String, String) =
		kind match {
			case SmolIr.KVoid =>
				("", "")
			case _: SmolIr.TypeDef =>
				(s"return ${Cpp.textForKind(kind)}(", ")")
			case _ =>
				("return ", "")
		}

	def reinterpretFunctionPointer
	(
		name: TS.Tok,
		selfData: TS.Tok,
		selfKind: SmolIr.TKind,
		args: SmolIr.TCall.Args,
		output: SmolIr.TKind
	): String = {

		val fullArgs: SmolIr.TCall.Args =
			args

		val castKind: String = {
			val castArgs: String =
				if (fullArgs.nonEmpty)
					fullArgs.map {
						case SmolIr.TCall.ThisArg =>
							selfKind
						case (arg: TCall.TKArg) => arg.kind
					}.map(Cpp.textForKind).reduce((_: String) + ", " + (_: String))
				else
					"void"

			val result: String =
				output match {
					case typeDef: SmolIr.TypeDef => Cpp.textForKind(typeDef.base)
					case _ => Cpp.textForKind(output)
				}

			s"$result(SMOL_CALL)($castArgs)"
		}

		val callArgs: String =
			if (fullArgs.nonEmpty)
				fullArgs.map {
					case (arg: SmolIr.TCall.Arg) => arg.name.text
					case SmolIr.TCall.ThisArg => "_this"
					case value: SmolIr.TCall.Value => value.hex.text
				}.reduce((_: String) + ", " + (_: String))
			else
				""

		s"(reinterpret_cast<$castKind>(_$moduleName._${name.text}))($callArgs)"
	}

	def emitCallable(callable: SmolIr.TCall, head: String, pre: String, call: String, end: String): String =
		(pre, callable.kind, end) match {
			case ("return ", enum: SmolIr.EnumKind, "") =>

				val tail: String =
					Cpp.emitAssertEnumOrFlag(
						moduleName,
						enum,
						"_out"
					)

				s"""
					 |$head
					 |{${emitEnumArgChecks(callable.args)}
					 |	auto _out = $call;$tail
					 |	return _out;
					 |}
				""".stripMargin.trim + '\n'

			case _ =>
				s"""
					 |$head
					 |{${emitEnumArgChecks(callable.args)}
					 |	$pre$call$end;
					 |}
				""".stripMargin.trim + '\n'
		}

	def emitEnumArgChecks(args: SmolIr.TCall.Args): String =
		args.filterTo[TCall.Arg].filter((_: TCall.Arg).kind.isInstanceOf[SmolIr.EnumKind]) match {
			case Nil => ""
			case enumArgs: List[TCall.Arg] =>
				enumArgs
					.map {
						case SmolIr.TCall.Arg(name: TS.Tok, enum: SmolIr.EnumKind) =>
							Cpp.emitAssertEnumOrFlag(
								moduleName,
								enum,
								name.text
							)
					}.reduce((_: String) + (_: String))
		}

}

