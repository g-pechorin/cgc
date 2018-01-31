package peterlavalle.dcm

import java.io.File

import peterlavalle.DynamicsFS


/**
	* tests it with a simple project
	*/
class PALDCMakeTest extends TDCMakeTest {
	def actual: DCMake =
		new DCMake(
			"pal", new File("C:/foo/bar/cm.ppc/"),

			// main gubbins
			zipDFS("cm.pal.zip", ".*\\.[ch]pp", "cm.pal"), Nil,
			List(
				new File("C:/foo/bar/cm.ppc/src/main/cgc")
			), false,

			// test gubbins
			DynamicsFS.Fresh, Nil, Nil, false
		)

	def expected: String =
		"""
			|
			|project (pal)
			|
			|add_library(pal
			|		build/.pal-KICK.cpp
			|		src/main/cgc/pal.cpp
			|		src/main/cgc/pal.hpp
			|		src/main/cgc/pal.inc.event_manager.hpp
			|	)
			|	target_include_directories(pal
			|		PUBLIC src/main/cgc/
			|	)
			|
		""".stripMargin
}
