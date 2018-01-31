package peterlavalle.dcm

import java.io.File

import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency

import scala.beans.BeanProperty

class Config(project: Project) {
	@BeanProperty
	var regen: String = "dcmLists"

	def localPort(groupNameVersion: String, root: File): Dependency =
		groupNameVersion.split(":").toList match {
			case List(group: String, name: String, version: String) =>
				Config.LocalPort(
					group,
					name,
					version
				)(root)
		}


}


object Config {

	case class LocalPort(@BeanProperty group: String, @BeanProperty name: String, @BeanProperty version: String)(val home: File)
		extends Dependency
			with DCMake.Module {


		override def contentEquals(dependency: Dependency): Boolean = ???


		override def copy(): Dependency = ???
	}

}
