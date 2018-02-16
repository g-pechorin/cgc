package peterlavalle.smol

import javax.inject.Inject

import org.gradle.api.internal.file.SourceDirectorySetFactory
import peterlavalle.gbt.APlugin

class ThePlugin @Inject()
(
	sourceDirectorySetFactory: SourceDirectorySetFactory
) extends APlugin(sourceDirectorySetFactory) {

	// need dcm to build stuff
	plugin[peterlavalle.dcm.ThePlugin]

	// need tin to compress the names
	plugin[peterlavalle.tin.ThePlugin]

	// create our source-set
	sourceSet("smol")(
		"**/**.smol"
	)
	// install our task(s)
	install[SmolLdrTask]


}
