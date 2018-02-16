package peterlavalle.tin

import javax.inject.Inject

import org.gradle.api.internal.file.SourceDirectorySetFactory
import peterlavalle.gbt.APlugin

class ThePlugin @Inject()
(
	sourceDirectorySetFactory: SourceDirectorySetFactory
) extends APlugin(sourceDirectorySetFactory) {

	// apply the base plugin
	plugin[peterlavalle.cgc.ThePlugin]

	// create our source sets
	sourceSet("tin")(".tin")

	// create the tasks
	install[TinCompressTask]
	install[TinHeaderTask]

}
