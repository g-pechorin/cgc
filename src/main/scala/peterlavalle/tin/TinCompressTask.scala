package peterlavalle.tin

import java.io.{File, FileInputStream, InputStream}

import peterlavalle.gbt.TProperTask
import peterlavalle.gbt.TProperTask._
import peterlavalle.{DynamicsFS, Later}

class TinCompressTask extends TTaskPhased(
	"build",
	"compresses files into .tin headers"
) {

	val sources: Later[List[TProperTask.Source]] =
		consume("tin") {
			sources: Iterable[TProperTask.Source] =>
				sources.toList
		}

	val compressed: Later[List[File]] =
		produce("cgc") {
			(cgcOut: File) =>

				val dynamicsFS =
					new DynamicsFS {

						override def write(path: String): File =
							cgcOut / path

						private val value: List[(File, String)] = sources.get

						/**
							* list all files in the fs
							*/
						override def * : Stream[String] = value.toStream.map(_._2)

						/**
							* open the named path, return null iff it's not there
							*/
						override def ?(path: String): InputStream =
							value.find((_: TProperTask.Source)._2 == path) match {
								case Some(source: TProperTask.Source) =>
									new FileInputStream(
										source._1 / source._2
									)
							}
					}


				new JobCompress(
					ext[Config].grouping,
					ext[Config].iterations,
					ext[Config].blockSplittingMax
				)
					.compress(dynamicsFS)
					.toList
		}
}
