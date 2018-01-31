package peterlavalle.dcm

import java.io.File

/**
	* tests it with dependency project(s) and some test/ code
	*/
class PPCDCMakeTest extends TDCMakeTest {

	override def expected: String =
		"""
			|
			|project (ppc)
			|
			|# gmocktest
			|add_subdirectory(googletest-release-1.8.0/ googletest-release-1.8.0/)
			|
			|# pal
			|add_subdirectory(../cm.pal/ cm.pal/)
			|
			|add_library(ppc
			|		build/.ppc-KICK.cpp
			|		src/main/cgc/ppc.hpp
			|		src/main/cgc/ppc.cpp
			|	)
			|	target_include_directories(ppc
			|		PUBLIC src/main/cgc/
			|	)
			|	target_link_libraries(
			|		ppc
			|			pal
			|	)
			|	add_dependencies(ppc
			|		pal
			|	)
			|
			|add_executable(ppc-test
			|		src/test/cgc/ppc_test.cpp
			|		src/test/cgc/test.cpp
			|	)
			|	set_target_properties(ppc-test PROPERTIES VS_DEBUGGER_WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR})
			|	target_include_directories(ppc-test
			|		PUBLIC src/test/cgc/
			|	)
			|	target_link_libraries(
			|		ppc-test
			|			gmocktest
			|			ppc
			|	)
			|	add_dependencies(ppc-test
			|		gmocktest
			|		ppc
			|	)
			|
		""".stripMargin

	override def actual: DCMake = new DCMake(
		"ppc", new File("C:/foo/bar/cm.ppc/"),
		zipDFS("cm.ppc.zip", ".*\\.[ch]pp", "cm.ppc") \ "src/main/.*", List(
			DCMake.Module("pal", new File("C:/foo/bar/cm.pal/"))
		), List(
			new File("C:/foo/bar/cm.ppc/src/main/cgc")
		), false,
		zipDFS("cm.ppc.zip", ".*\\.[ch]pp", "cm.ppc") \ "src/test/.*", List(
			DCMake.Module("gmocktest", new File("C:/foo/bar/cm.ppc/googletest-release-1.8.0"))
		), List(
			new File("C:/foo/bar/cm.ppc/src/test/cgc")
		), false
	)

}
