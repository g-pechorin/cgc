package peterlavalle.dcm

import javax.inject.Inject

import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.internal.file.SourceDirectorySetFactory
import peterlavalle.gbt.APlugin

class ThePlugin @Inject()
(
	sourceDirectorySetFactory: SourceDirectorySetFactory
) extends APlugin {

	plugin[peterlavalle.cgc.ThePlugin]

	install[ModuleTask]
	install[ListsTask]

	override def newSourceSet(kind: String, displayName: String, src: SourceDirectorySet) = {
		sys.error("this shouldn't be needed")
	}
}
