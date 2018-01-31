package peterlavalle.tin

import java.io.{File, InputStream}
import java.security.MessageDigest

import peterlavalle.{DynamicsFS, OverWriter}

import scala.io.Source

class JobCompress
(
	iterations: Int, // = 34
	blockSplittingMax: Int, // = 14
	grouping: Int // = 14
) {
	def compress(dynamicsFS: DynamicsFS): Iterable[File] =
		dynamicsFS.*.map {
			path: String =>
				val data: File =
					dynamicsFS.write(s"$path.tin-inc")

				def inputStream: InputStream =
					dynamicsFS ? path

				lazy val md5: String = {

					// start the message digest
					val messageDigest: MessageDigest = MessageDigest.getInstance("MD5")

					// put all chunks into it
					try {
						Compress.chunkit(inputStream).foreach {
							chunk: Array[Byte] =>
								messageDigest.update(chunk)
						}
					} catch {
						case e: Throwable =>
							throw new RuntimeException("Problem loading chunks into MD5:" + e.getMessage)
					}

					messageDigest.digest().map((b: Byte) => "%02X".format(b)).reduce((_: String) + (_: String))
				}

				lazy val newManifest: String =
					s"// iterations = $iterations, blockSplittingMax = $blockSplittingMax, hash = $md5"

				// wasteful way to read the start of the file BUT if it doesn't work ... we're probably not going to be able to compress the file
				lazy val oldManifest: String =
					if (data.exists())
						Source.fromFile(data).MkString.split("\r?\n").head
					else
						""

				lazy val compressed: Array[Byte] =
					Compress.zopfli(
						Compress.mergeFlat(
							Compress.chunkit(inputStream)
						),
						iterations,
						blockSplittingMax
					).toArray


				if (newManifest == oldManifest) {
					data
				} else {
					val originalLength: Int = Compress.chunkit(inputStream).map((_: Array[Byte]).length).sum
					val compressedSize: Int = compressed.size
					new OverWriter(data)
						.appund(
							s"""
								 |$newManifest
								 |// tin-can header
								 |TIN_START(
								 |	// file name
								 |		"$path",
								 |	// original file size
								 |		$originalLength,
								 |	// compressed size
								 |		$compressedSize
								 |)
								 |	// compressed data
							""".stripTrim
						)
						.appund(compressed.grouped(grouping)) {
							line: Array[Byte] =>

								val data: String =
									line.toList
										.map((b: Byte) => ByeCoder(b) + ",")
										.reduce((_: String) + " " + (_: String))

								s"\t\t\t$data\n"
						}
						.appund(
							s"""
								 |// tin-can footer
								 |TIN_CLOSE("$path",$originalLength,$compressedSize)
							""".stripTrim
						)
						.closeFile
				}
		}
}
