package peterlavalle.dcm

import javax.inject.Inject

import org.gradle.api.internal.file.SourceDirectorySetFactory
import peterlavalle.gbt.APlugin

class ThePlugin @Inject()
(
	sourceDirectorySetFactory: SourceDirectorySetFactory
) extends APlugin(sourceDirectorySetFactory) {

	plugin[peterlavalle.cgc.ThePlugin]

	install[ModuleTask]
	install[ListsTask]
}
