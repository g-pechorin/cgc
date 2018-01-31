package peterlavalle.cgc

import javax.inject.Inject

import groovy.lang.Closure
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.internal.file.SourceDirectorySetFactory
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.SourceSet
import org.gradle.util.ConfigureUtil
import peterlavalle.gbt.APlugin

import scala.beans.BeanProperty

class ThePlugin @Inject()
(
	sourceDirectorySetFactory: SourceDirectorySetFactory
) extends APlugin {

	override def newSourceSet(kind: String, theDisplayName: String, theSourceDirectorySet: SourceDirectorySet): APlugin.TSourceSet =
		kind match {
			case "cgc" =>
				new APlugin.TSourceSet {
					override val displayName: String = theDisplayName
					override val src: SourceDirectorySet = theSourceDirectorySet

					@BeanProperty
					val cgc: SourceDirectorySet = theSourceDirectorySet

					def cgc(configureClosure: Closure[_]): APlugin.TSourceSet = {
						ConfigureUtil.configure(configureClosure, theSourceDirectorySet)
						this
					}
				}
		}

	plugin[JavaPlugin]

	configure {

		// create the source sets
		addSourceSet("cgc", sourceDirectorySetFactory, project) {
			(sourceSet: SourceSet, sourceDirectorySet: SourceDirectorySet) =>
				sourceDirectorySet.include(
					"**/**.c",
					"**/**.cc",
					"**/**.cpp",
					"**/**.h",
					"**/**.hh",
					"**/**.hpp"
				)
		}

		// create the generate tasks for cgc
		// since both the GCC and CMake paths exist - need some explicit task/point to chain other plugins off of
		project.install[Generate]
	}
}
