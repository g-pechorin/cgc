package peterlavalle.smol

import java.io.{File, FileInputStream, FileOutputStream}

import org.antlr.v4.runtime.{ANTLRInputStream, CommonTokenStream, RecognitionException}
import org.gradle.api.GradleException
import peterlavalle.cgc.Generate
import peterlavalle.gbt.TProperTask
import peterlavalle.{Later, OverWriter}


class SmolLdrTask extends TProperTask.TTaskPhased(
	"build setup",
	"builds a list of stuff to make smol-loaders from"
) {
	connect {
		// we're going to generate C/++ headers

		findPhasedTask[Generate].dependsOn(this)
	}

	// cool - we need to compile all of the things!
	val modules: Later[Iterable[(String, SmolIr.Module)]] =
		consume("smol") {
			sources: Iterable[TProperTask.Source] =>
				sources.map {
					case (root, path) =>
						val name: String = path.reverse.dropWhile('.' != (_: Char)).tail.reverse

						def handler(recognitionException: RecognitionException, message: String, line: Int): Unit =
							throw new GradleException(
								message + s"\n\t\t@$path\n\t\t:$line"
							)

						val parser: SmolIrParser =
							new SmolIrParser(
								new CommonTokenStream(
									new SmolIrLexer(
										new ANTLRInputStream(new FileInputStream(root / path))
											.setName(path)
									).handleErrors(handler)
								)
							).handleErrors(handler)

						(name, Compiler(
							parser.module()
						))
				}
		}

	// emit headers and loaders
	produce("cgc") {
		smolOutput: File =>
			modules.get.foreach {
				case (name: String, data: SmolIr.Module) =>
					new Header(data)(new OverWriter(smolOutput / s"$name.hpp"))
						.appund(HardCoded.loader)
						.appund("\n#endif\n").closeFile
					new Loader(data)(new OverWriter(smolOutput / s"$name.cpp")).closeFile
			}
	}

	// emit the names into a blob that the toolchain will compress
	produce("tin") {
		smolOutput: File =>
			modules.get.foreach {
				case (name: String, data: SmolIr.Module) =>
					new Labels(new FileOutputStream(smolOutput / s"$name.tin"))(data).close()
			}
	}
}
