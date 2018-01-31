package peterlavalle.dcm

import java.io.{File, StringWriter, Writer}

import peterlavalle.{DynamicsFS, OverWriter}

/**
	* another attempt to wrap CMake, this time using interfaces and proxies to allow obsessive testing before actually using it
	*/
class DCMake(
							val name: String, val home: File,
							mainSrc: DynamicsFS, mainLib: Iterable[DCMake.Module], mainInc: Iterable[File], val mainOut: Boolean,
							testSrc: DynamicsFS, testLib: Iterable[DCMake.Module], testInc: Iterable[File], val testOut: Boolean
						) extends DCMake.Module {

	val mainName: String =
		name

	val testName: String =
		if (testOut)
			name + ".bin"
		else {
			name + "-test"
		}


	def emit[W <: Writer](writer: W): W = {
		import DCMake._

		writer
			.appund(s"\nproject ($name)\n")
			.appund((mainLib ++ testLib).toList.sortBy((_: DCMake.Module).name)) {
				module: DCMake.Module =>
					val path: String =
						home walkTo module.home

					val sub: String =
						module.home.getName

					s"\n# ${module.name}\nadd_subdirectory($path/ $sub/)\n"
			}

			//
			// create the main target
			.appund("\nadd_library(" + mainName)
			.appund {
				if (mainOut)
					" SHARED"
				else
					""
			}
			.appund {
				"\n\t\t" + home.EnsureExists.walkTo(new OverWriter(home / ("build/." + name + "-KICK.cpp")).appund("\n// no-nothing source file to kick cmake\n").closeFile)
			}
			.appund {
				new StringWriter()
					.appund(mainSrc *)("\n\t\t" + (_: String))
					.toString
			}
			.appund("\n\t)")

			.include(home, mainName, mainInc)
			.modules(home, mainName, mainLib)

			.appund {
				//
				// create the test target
				if (testSrc.isEmpty || (testSrc * ".+\\.(c|cc|m|mm|cpp|cxx)").isEmpty)
					""
				else
					new StringWriter()
						.appund {
							"\n\nadd_executable(" + testName
						}
						.appund(testSrc *)("\n\t\t" + (_: String))
						.appund("\n\t)")
						.appund(
							s"\n\tset_target_properties($testName PROPERTIES VS_DEBUGGER_WORKING_DIRECTORY $${CMAKE_CURRENT_SOURCE_DIR})"
						)
						.include(home, testName, testInc)
						.modules(home, testName, this :: testLib.toList)
						.toString
			}
	}
}

object DCMake {

	def Module(nume: String, rout: File): Module =
		new Module {
			override val home: File = rout
			override val name: String = nume
		}

	trait Module {
		val name: String
		val home: File
	}


	implicit class EmitWrapper[O <: Writer](writer: O) {
		def modules(home: File, name: String, lib: Iterable[DCMake.Module]): O =
			if (lib.isEmpty)
				writer
			else
				writer
					.appund {
						lib
							.map((_: DCMake.Module).name).toList.sorted
							.foldLeft(s"\n\ttarget_link_libraries(\n\t\t$name")((_: String) + "\n\t\t\t" + (_: String)) + "\n\t)"
					}
					.appund {
						lib
							.map((_: DCMake.Module).name).toList.sorted
							.foldLeft(s"\n\tadd_dependencies($name")((_: String) + "\n\t\t" + (_: String)) + "\n\t)"
					}


		def include(home: File, name: String, inc: Iterable[File]): O =
			if (inc.isEmpty)
				writer
			else
				writer
					.appund {

						def directories: String =
							inc.map(home walkTo (_: File)).map("\n\t\tPUBLIC " + (_: String) + '/').reduce((_: String) + (_: String))

						s"\n\ttarget_include_directories($name" + directories + "\n\t)"
					}

	}

}
