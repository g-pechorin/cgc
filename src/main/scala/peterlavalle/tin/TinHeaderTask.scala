package peterlavalle.tin

import java.io.File

import peterlavalle.gbt.TProperTask
import peterlavalle.gbt.TProperTask.Phase
import peterlavalle.{Later, OverWriter}

class TinHeaderTask extends TProperTask.TTaskPhased(
	"build",
	"creates a header of all .tin fragments"
) {

	// TODO; find some better way to get these
	val blobs: Later[List[TinCompressTask]] =
		consume[TinCompressTask]

	dependsOn[TinCompressTask]

	val header: Later[File] =
		produce("cgc") {
			(cgcOut: File) =>

				val tinHead: File = cgcOut / s"tin-${phase.toString.toLowerCase}.hpp"
				val output =
					new OverWriter(tinHead)

				println("TODO; really upstream the extensions")

				// TODO; emit name-phase.hpp with all bits concat in it
				// TODO; emit a target header which includes the above


				blobs.get.foreach {
					(task: TinCompressTask) =>

						def isMain = Phase.Main == task.phase

						def isSame = this.phase == task.phase

						if (isMain || isSame)
							output
								.appund("\n\n// " + task.getPath)
								.appund(task.compressed.get) {
									(tinFile: File) =>


										"\n\t#include \"" + (tinHead.getParentFile.walkTo(tinFile)) + "\""
								}

				}

				output
					.closeFile
		}
}
