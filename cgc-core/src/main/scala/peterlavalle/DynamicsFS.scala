package peterlavalle

import java.io.{ByteArrayInputStream, File, FileInputStream, InputStream}
import java.util.zip.{ZipEntry, ZipInputStream}

import scala.languageFeature.{implicitConversions, postfixOps}

/**
	* Wrapper around actual file-system to ease testing
	*
	* The name means NOTHING, see; https://icculus.org/physfs/
	*/
object DynamicsFS {

	println("PHile works better. upstream that and adapt this")

	import scala.languageFeature.{implicitConversions, postfixOps}

	implicit def fromDir(file: File): DynamicsFS =
		if (file.getAbsolutePath != file.getPath)
			DynamicsFS.fromDir(file.getAbsoluteFile)
		else
			new DynamicsFS {
				/**
					* list all files in the fs
					*/
				override def * : Stream[String] = ???

				/**
					* open the named path, return null iff it's not there
					*/
				override def ?(path: String): InputStream =
					new FileInputStream(new File(file, path))
			}

	implicit def fromZipStream(inputStream: InputStream): DynamicsFS =
		inputStream match {
			case zipInputStream: ZipInputStream =>
				new DynamicsFS {

					val contents: Map[String, Array[Byte]] = {

						def recu: Stream[(String, Array[Byte])] =
							zipInputStream.getNextEntry match {
								case null =>
									Stream.Empty

								case entry: ZipEntry =>

									def load(data: Array[Byte], off: Int): Array[Byte] =
										if (data.length < off)
											sys.error("SAN check failed")
										else if (data.length == off)
											data
										else
											load(
												data,
												off + zipInputStream.read(
													data,
													off,
													data.length - off
												)
											)

									def data: Array[Byte] =
										load(
											Array.ofDim[Byte](entry.getSize.toInt),
											0
										)

									if (!entry.isDirectory)
										(entry.getName, data) #:: recu
									else
										recu
							}

						recu.toMap
					}

					/**
						* list all files in the fs
						*/
					override def * : Stream[String] =
						contents.keySet.toStream

					/**
						* open the named path, return null iff it's not there
						*/
					override def ?(path: String): InputStream =
						if (contents contains path)
							new ByteArrayInputStream(contents(path))
						else
							null
				}
			case _ =>
				fromZipStream(new ZipInputStream(inputStream))
		}


	case object Fresh extends DynamicsFS {
		/**
			* list all files in the fs
			*/
		override def * : Stream[String] = Stream.Empty

		/**
			* open the named path, return null iff it's not there
			*/
		override def ?(path: String): InputStream = null
	}

	println("TODO; upstream this")
}


trait DynamicsFS {

	def write(path: String): File =
		???

	import scala.languageFeature.{implicitConversions, postfixOps}

	def nonEmpty: Boolean = (this *).nonEmpty

	def isEmpty: Boolean = (this *).isEmpty

	def /(path: String): DynamicsFS =
		if (!path.endsWith("/"))
			this / (path + "/")
		else {
			require("/" != path)
			val base: DynamicsFS = this
			new DynamicsFS {
				/**
					* list all files in the fs
					*/
				override def * : Stream[String] =
					(base *)
						.filter((_: String).startsWith(path))
						.map((_: String).substring(path.length))

				/**
					* open the named path, return null iff it's not there
					*/
				override def ?(path: String): InputStream = ???
			}
		}

	def update(name: String, data: Array[Byte]): DynamicsFS = {
		val self: DynamicsFS = this
		new DynamicsFS {
			/**
				* list all files in the fs
				*/
			override def * : Stream[String] =
				(name #:: this.*).distinct

			/**
				* open the named path, return null iff it's not there
				*/
			override def ?(path: String): InputStream =
				if (path == name)
					new ByteArrayInputStream(data)
				else
					self ? path
		}
	}

	def apply(overlaid: (String, Array[Byte])*): DynamicsFS =
		overlaid.toList.reverse.foldLeft(this) {
			case (l: DynamicsFS, (p: String, n: Array[Byte])) =>
				l(p) = n
		}

	/**
		* returns a fs that caches any accessed filed into memory
		*/
	def cache: DynamicsFS =
		???

	/**
		* list all files in the fs
		*/
	def * : Stream[String]

	def \(pattern: String) = {
		val base: DynamicsFS = this
		new DynamicsFS {

			/**
				* list all files in the fs
				*/
			override def * : Stream[String] = base * pattern

			/**
				* open the named path, return null iff it's not there
				*/
			override def ?(path: String): InputStream = base ? path

		}
	}

	/**
		* list all files with a matching path
		*/
	def *(pattern: String): Stream[String] =
		(this *).filter((_: String).matches(pattern))

	/**
		* open the named path, return null iff it's not there
		*/
	def ?(path: String): InputStream

	/**
		* use the fallback if this doesn't have a file
		*/
	def ++(fall: DynamicsFS): DynamicsFS = {
		val base: DynamicsFS = this
		new DynamicsFS {
			/**
				* list all files in the fs
				*/
			override def * : Stream[String] =
				((base *) ++ (fall *)).distinct

			/**
				* open the named path
				*/
			override def ?(path: String): InputStream =
				base ? path match {
					case null =>
						fall ? path
					case file =>
						file
				}
		}
	}
}
