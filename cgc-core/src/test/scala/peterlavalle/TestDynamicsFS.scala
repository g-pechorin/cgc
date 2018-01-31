package peterlavalle

import org.junit.Assert._

class TestDynamicsFS extends ATestCase {
	def testItThing(): Unit =
		assertEquals(
			Stream(
				"cm.pal/CMakeLists.txt",
				"cm.pal/src/main/cgc/pal.cpp",
				"cm.pal/src/main/cgc/pal.inc.event_manager.hpp",
				"cm.pal/src/main/cgc/pal.hpp"
			).sorted,
			(fs *) sorted
		)

	def testSub(): Unit =
		assertEquals(
			Stream(
				"CMakeLists.txt",
				"src/main/cgc/pal.cpp",
				"src/main/cgc/pal.inc.event_manager.hpp",
				"src/main/cgc/pal.hpp"
			).sorted,
			(fs / "cm.pal" *) sorted
		)

	def fs: DynamicsFS =
		DynamicsFS.fromZipStream(
			ClassLoader.getSystemResourceAsStream("cm.pal.zip")
		)

}
