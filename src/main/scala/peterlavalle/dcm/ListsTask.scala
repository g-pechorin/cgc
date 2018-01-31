package peterlavalle.dcm

import java.io.File

import org.gradle.api.Project
import peterlavalle.cgc.Generate
import peterlavalle.gbt.TProperTask
import peterlavalle.gbt.TProperTask.Phase
import peterlavalle.{Later, OverWriter}

class ListsTask extends TProperTask.TTaskSingle(
	"build setup",
	"generates/updates any/all CMakeLists.txt files"
) {

	connect {
		// we want the C/++ stuffs donzies
		getProject.getTasks.filterTo[Generate].foreach {
			task: Generate =>
				dependsOn(task)
		}
	}
	val dynamics: Later[List[ModuleTask]] = consumePhased[ModuleTask]

	val cmakeLists: Later[File] =
		perform {
			val subProjects: List[Project] = getProject.getSubprojects.toList.sortBy((_: Project).getName)

			val cmakeLists: OverWriter =
				new OverWriter(getProject.getProjectDir / "CMakeLists.txt")
					.appund(
						s"""
							 |cmake_minimum_required (VERSION 3.9.0)
							 |add_definitions(
							 |	-D_CRT_SECURE_NO_WARNINGS # disable warning about using C++ functions (M$$ wants us to use their own non-conformant variants)
							 |	-D_SCL_SECURE_NO_WARNINGS # disable more dumbstuff
							 |	/wd4577 # disable warnings about `noexcept` when we've disabled exceptions (lets me slap noexcept into places that it should be)
							 |	/wd4530 # disable warnings about `try{}catch{}` when we've disabled exceptions (VS includes them)
							 | 	-D_SILENCE_TR1_NAMESPACE_DEPRECATION_WARNING # not so much a V$$ problem
							 |)
							 |
							 |if (NOT TARGET ${getProject.getName})
						""".stripMargin.trim
					)

			if (getProject.getRootProject == getProject)
				cmakeLists
					.appund(
						"\n\n" +
							"""
								|
								|# remove exceptions (maybe put them back in ot not-min?)
								|	string(REPLACE "/EHsc" ""	CMAKE_C_FLAGS		"${CMAKE_C_FLAGS}")
								|	string(REPLACE "/EHsc" ""	CMAKE_CXX_FLAGS	"${CMAKE_CXX_FLAGS}")
								|	string(REPLACE "/EHsa" ""	CMAKE_C_FLAGS		"${CMAKE_C_FLAGS}")
								|	string(REPLACE "/EHsa" ""	CMAKE_CXX_FLAGS	"${CMAKE_CXX_FLAGS}")
								|	string(REPLACE "/EHs" ""	CMAKE_C_FLAGS		"${CMAKE_C_FLAGS}")
								|	string(REPLACE "/EHs" ""	CMAKE_CXX_FLAGS	"${CMAKE_CXX_FLAGS}")
								|	string(REPLACE "/EHa" ""	CMAKE_C_FLAGS		"${CMAKE_C_FLAGS}")
								|	string(REPLACE "/EHa" ""	CMAKE_CXX_FLAGS	"${CMAKE_CXX_FLAGS}")
								|
								|# favour small code
								|	set(CMAKE_C_FLAGS_MINSIZEREL		"${CMAKE_C_FLAGS_MINSIZEREL}   /Os")
								|	set(CMAKE_CXX_FLAGS_MINSIZEREL	"${CMAKE_CXX_FLAGS_MINSIZEREL} /Os")
								|
								|# strip security checks
								|	set(CMAKE_C_FLAGS_MINSIZEREL		"${CMAKE_C_FLAGS_MINSIZEREL}   /GS-")
								|	set(CMAKE_CXX_FLAGS_MINSIZEREL	"${CMAKE_CXX_FLAGS_MINSIZEREL} /GS-")
								|
								|## use fastcall
								|#	set(CMAKE_C_FLAGS_MINSIZEREL		"${CMAKE_C_FLAGS_MINSIZEREL}   /Gr")
								|#	set(CMAKE_CXX_FLAGS_MINSIZEREL	"${CMAKE_CXX_FLAGS_MINSIZEREL} /Gr")
								|
								|# strip RTTI
								|	set(CMAKE_C_FLAGS_MINSIZEREL		"${CMAKE_C_FLAGS_MINSIZEREL}   /GR-")
								|	set(CMAKE_CXX_FLAGS_MINSIZEREL	"${CMAKE_CXX_FLAGS_MINSIZEREL} /GR-")
								|
								|# pool strings
								|	set(CMAKE_C_FLAGS		"${CMAKE_C_FLAGS}		/GF")
								|	set(CMAKE_CXX_FLAGS	"${CMAKE_CXX_FLAGS}	/GF")
								|
							""".stripMargin.trim + '\n'
					)

			dCMake
				.emit(cmakeLists)

				.appund {
					//
					// add the magic for minimising the .exe
					//

					def bin(text: String): String =
						(osName, osArch) match {
							case ("windows", _) =>
								text + ".exe"
						}

					val upxMissing: Boolean = !(getProject.getRootDir / bin("upx")).exists()

					val testNonExe: Boolean = !dCMake.testOut

					if (testNonExe || upxMissing)
						""
					else {
						val packed = s"$$<TARGET_FILE_DIR:${dCMake.testName}>/${bin(getProject.getName + ".upx")}"
						val origin = s"$$<TARGET_FILE:${dCMake.testName}>"

						'\n' +
							s"""
								 |# some black-magic to post-process the binary
								 |		add_custom_target(
								 |			${getProject.getName}.upx
								 |				$${CMAKE_COMMAND} -E remove -f $packed
								 |				COMMAND upx -9 -o $$<TARGET_FILE_DIR:${dCMake.testName}>/${bin(getProject.getName + ".upx")} $origin
								 |				DEPENDS ${dCMake.testName}
								 |				WORKING_DIRECTORY $${CMAKE_SOURCE_DIR}
								 |  		)
						""".stripMargin.trim
					}
				}

				//
				// add sub projects; makes the root really a "root"
				.appund(s"\n\n# sub-projects")
				.appund(subProjects) {
					subProject: Project =>
						// assumes that the lists file is written to the root dir
						val path: String =
							getProject.getProjectDir.walkTo(subProject.getProjectDir).reverse.dropWhile('/' == _).reverse

						List("",
							s"\t\tadd_subdirectory($path)",
							s"\t\t\tadd_dependencies(${getProject.getName} ${subProject.getName})"
						).reduce(_ + "\n" + _)
				}
				.appund {
					if (subProjects.isEmpty)
						""
					else
						subProjects.map((_: Project).getName).foldLeft("\n\tadd_dependencies(" + getProject.getName)((_: String) + "\n\t\t" + (_: String)) + "\n\t)"
				}
				.appund {
					// retup seinvoker
					if (!isRootAndWrapped)
						""
					else {
						'\n' +
							s"""
								 |add_custom_target(dcm
								 |	# recreate teh stuff from gradle
								 |		COMMAND gradlew ${ext[Config].regen}
								 |	# do it from here
								 |		WORKING_DIRECTORY $${CMAKE_SOURCE_DIR}
								 |	)
								 |	add_custom_command(TARGET dcm
								 |		POST_BUILD
								 |		COMMAND $${CMAKE_COMMAND} $${CMAKE_CURRENT_BINARY_DIR}
								 |		WORKING_DIRECTORY $${CMAKE_SOURCE_DIR}
								 |	)
							""".stripMargin.trim + '\n'
					}
				}
				.appund(s"\nendif (NOT TARGET ${getProject.getName})\n")
				.closeFile
		}

	/// is this the root project and does it have a gradlewrapper?
	def isRootAndWrapped: Boolean =
		getProject.getProjectDir.AbsolutePath == getProject.getRootDir.AbsolutePath && (getProject.getRootDir / "gradlew").exists()

	def dCMake: DCMake = {

		lazy val mainTask: ModuleTask =
			dynamics ? {
				tasks: List[ModuleTask] =>

					assert(2 == tasks.length)

					tasks.find((_: ModuleTask).phase == Phase.Main).get
			}

		lazy val testTask: ModuleTask =
			dynamics ? {
				tasks: List[ModuleTask] =>

					assert(2 == tasks.length)

					tasks.find((_: ModuleTask).phase == Phase.Test).get
			}

		new DCMake(
			getProject.getName, getProject.getProjectDir.getAbsoluteFile,
			mainTask.dfs.get, mainTask.lib, mainTask.inc.get, mainTask.standalone,
			testTask.dfs.get, testTask.lib, testTask.inc.get, testTask.standalone
		)
	}


}
