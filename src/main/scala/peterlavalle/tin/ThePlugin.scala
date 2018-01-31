package peterlavalle.tin

import javax.inject.Inject

import groovy.lang.Closure
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.internal.file.SourceDirectorySetFactory
import org.gradle.api.tasks.SourceSet
import org.gradle.util.ConfigureUtil
import peterlavalle.gbt.APlugin

class ThePlugin @Inject()
(
	sourceDirectorySetFactory: SourceDirectorySetFactory
) extends APlugin {

	override def newSourceSet(kind: String, theDisplayName: String, theSourceDirectorySet: SourceDirectorySet): APlugin.TSourceSet =
		kind match {
			case "tin" =>
				new APlugin.TSourceSet {
					override val displayName: String = theDisplayName
					override val src: SourceDirectorySet = theSourceDirectorySet
					val tin: SourceDirectorySet = theSourceDirectorySet

					def tin(configureClosure: Closure[_]): APlugin.TSourceSet = {
						ConfigureUtil.configure(configureClosure, theSourceDirectorySet)
						this
					}
				}
		}

	// apply the base plugin
	plugin[peterlavalle.cgc.ThePlugin]

	// create our source sets
	sourceSet("tin")(".tin")

	def sourceSet(name: String)(extensions: String*): Unit =
		configure {
			println("upstream this")
			addSourceSet(name, sourceDirectorySetFactory, project) {
				(sourceSet: SourceSet, theDirectorySet: SourceDirectorySet) =>
					extensions.foreach {
						extension: String =>
							extension.head match {
								case '.' =>
									theDirectorySet.include(
										s"**/*$extension"
									)
							}
					}
			}
		}

	// create the tasks
	install[TinCompressTask]
	install[TinHeaderTask]

}
