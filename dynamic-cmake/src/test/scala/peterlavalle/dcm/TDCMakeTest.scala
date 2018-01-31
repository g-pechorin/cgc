package peterlavalle.dcm

import java.io.StringWriter

import org.junit.Assert._
import peterlavalle.DynamicsFS


trait TDCMakeTest extends peterlavalle.ATestCase {

	def zipDFS(name: String, filter: String, path: String): DynamicsFS =
		DynamicsFS.fromZipStream(ClassLoader.getSystemResourceAsStream(name)) \ filter / path

	def actual: DCMake

	def expected: String

	/**
		* tests it with a simple project
		*/
	def testCMakeLists(): Unit = {

		val e: String =
			try {
				expected.trim.replaceAll("[\r \t]*\n", "\n")
			} catch {
				case n: NullPointerException =>
					fail("null-ptr-ex while trying to load expected")
					throw n
			}

		val a: String =
			try {
				actual
					.emit(new StringWriter())
					.toString
					.trim.replaceAll("[\r \t]*\n", "\n")
			} catch {
				case n: NullPointerException =>
					fail("null-ptr-ex while trying to make actual")
					throw n
			}

		assertEquals(e, a)
	}
}
