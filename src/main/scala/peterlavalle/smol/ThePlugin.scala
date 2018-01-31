package peterlavalle.smol

import javax.inject.Inject

import groovy.lang.Closure
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.internal.file.SourceDirectorySetFactory
import org.gradle.api.tasks.SourceSet
import org.gradle.util.ConfigureUtil
import peterlavalle.gbt.APlugin

import scala.beans.BeanProperty

class ThePlugin @Inject()
(
	sourceDirectorySetFactory: SourceDirectorySetFactory
) extends APlugin {

	// need dcm to build stuff
	plugin[peterlavalle.dcm.ThePlugin]

	// need tin to compress the names
	plugin[peterlavalle.tin.ThePlugin]

	// create our source-set
	configure {
		addSourceSet("smol", sourceDirectorySetFactory, project) {
			(sourceSet: SourceSet, sourceDirectorySet: SourceDirectorySet) =>
				sourceDirectorySet.include(
					"**/**.smol"
				)
		}
	}

	// install our task(s)
	install[SmolLdrTask]


	override def newSourceSet(kind: String, theDisplayName: String, theSourceDirectorySet: SourceDirectorySet) =
		if (kind != "smol")
			sys.error("lolwut")
		else {
			new APlugin.TSourceSet {
				override val displayName: String = theDisplayName
				override val src: SourceDirectorySet = theSourceDirectorySet

				@BeanProperty
				val smol: SourceDirectorySet = theSourceDirectorySet

				def smol(configureClosure: Closure[_]): APlugin.TSourceSet = {
					ConfigureUtil.configure(configureClosure, theSourceDirectorySet)
					this
				}
			}
		}

}
