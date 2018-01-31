package peterlavalle.smol

import java.io.{ByteArrayOutputStream, StringReader, StringWriter}

import org.antlr.v4.runtime.{ANTLRInputStream, CommonTokenStream, RecognitionException}
import org.junit.Assert._

trait TTestCase extends peterlavalle.ATestCase {
	def smol: String

	def treeCode: SmolIr.Module

	def header: String

	def expandedEnums: List[SmolIr.EnumKind]

	def expandedCalls: List[SmolIr.TCall]

	def testAllEnums(): Unit = {
		val actual: List[SmolIr.EnumKind] = new Examine(treeCode).allEnums.toList
		val expected: List[SmolIr.EnumKind] = expandedEnums
		sAssertEqual(
			"contents are different",
			expected.sortBy(_.name.text),
			actual.sortBy(_.name.text)
		)

		sAssertEqual(
			"ordering is different",
			expected,
			actual
		)
	}

	def testAllCallable(): Unit = {
		val actual: List[SmolIr.TCall] = new Examine(treeCode).allCallable.toList
		val expected: List[SmolIr.TCall] = expandedCalls
		sAssertEqual(
			"contents are different",
			expected.sortBy(_.name.text),
			actual.sortBy(_.name.text)
		)

		sAssertEqual(
			"ordering is different",
			expected,
			actual
		)
	}

	def labels: List[String]

	def loader: String

	def testDefined(): Unit = {
		smol
		treeCode
		header
		labels
		loader
		expandedEnums
	}

	def testParse(): Unit = {
		parser.module()
	}

	def testCompile(): Unit = {

		val actual: SmolIr.Module =
			Compiler(parser.module())

		val expected: SmolIr.Module =
			treeCode

		sAssertEqual(
			expected,
			actual
		)
	}

	private def parser =
		new SmolIrParser(
			new CommonTokenStream(
				new SmolIrLexer(
					new ANTLRInputStream(
						new StringReader(smol)
					).setName(getName)
				).handleErrors(failError)
			)
		).handleErrors(failError)

	def failError(recognitionException: RecognitionException, message: String, line: Int): Unit =
		fail(
			s"in $getName; `$message` @ $line"
		)

	def testHeader(): Unit = {

		val actual: String =
			new Header(treeCode)(new StringWriter()).toString.replaceAll("[\r \t]*\n", "\n")

		val expected: String =
			header.replaceAll("[\r \t]*\n", "\n")

		assertEquals(
			expected,
			actual
		)
	}

	def testLoader(): Unit = {

		val actual: String =
			new Loader(treeCode)(new StringWriter())
				.toString
				.replaceAll("[\r \t]*\n", "\n")

		val expected: String =
			loader
				.replaceAll("[\r \t]*\n", "\n")

		assertEquals(
			expected,
			actual
		)
	}

	def testNameText(): Unit = {

		val actual =
			new Labels(new ByteArrayOutputStream()).strings(treeCode)

		val expected =
			labels

		assertEquals(
			expected,
			actual.toList
		)
	}

	def testNameBytes(): Unit = {

		val actual: Array[Byte] =
			new Labels(new ByteArrayOutputStream())(treeCode)
				.toByteArray

		val expected: Array[Byte] =
			labels
				.flatMap {
					name: String =>
						name.getBytes ++ Array[Byte](0)
				}.toArray

		assertEquals(
			expected.toList,
			actual.toList
		)
	}
}
