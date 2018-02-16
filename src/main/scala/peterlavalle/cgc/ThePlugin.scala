package peterlavalle.cgc

import javax.inject.Inject

import org.gradle.api.internal.file.SourceDirectorySetFactory
import org.gradle.api.plugins.JavaPlugin
import peterlavalle.gbt.APlugin

class ThePlugin @Inject()
(
	sourceDirectorySetFactory: SourceDirectorySetFactory
) extends APlugin(sourceDirectorySetFactory) {

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

	configure {
		// create the generate tasks for cgc
		// since both the GCC and CMake paths exist - need some explicit task/point to chain other plugins off of
		project.install[Generate]
	}
}
