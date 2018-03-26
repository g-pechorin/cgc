package peterlavalle.cgc

import javax.inject.Inject

import org.gradle.api.internal.file.SourceDirectorySetFactory
import org.gradle.api.plugins.JavaPlugin
import peterlavalle.gbt.{APlugin, MContent}

class ThePlugin @Inject()
(
	sourceDirectorySetFactory: SourceDirectorySetFactory
) extends APlugin(sourceDirectorySetFactory)
	with MContent.MPlugin {

	plugin[JavaPlugin]

	// create the source sets
	sourceSet("cgc")(

		"**/**.hpp",
		"**/**.cpp",

		"**/**.hh",
		"**/**.cc",

		"**/**.h",
		"**/**.c"

	)
}
