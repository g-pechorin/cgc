package peterlavalle.dcm

import java.io.{File, InputStream}

import org.gradle.api.artifacts.{Dependency, ProjectDependency}
import peterlavalle.gbt.TProperTask
import peterlavalle.{DynamicsFS, Later, cgc}

import scala.beans.BeanProperty

class ModuleTask extends TProperTask.TTaskPhased(
	"build setup",
	"collects lists of source code files for either the test or main"
) {
	connect {
		dependsOn(
			findPhasedTask[cgc.Generate]
		)
	}

	lazy val lib: List[DCMake.Module] =
		depsImediate {
			deps: List[Dependency] =>
				deps.map {
					case projectDependency: ProjectDependency =>
						projectDependency.getDependencyProject.findTasks[ModuleTask].filter((_: ModuleTask).phase == TProperTask.Phase.Main) match {
							case List(task: ModuleTask) =>
								DCMake.Module(
									task.getProject.getName,
									task.getProject.getProjectDir
								)
						}

					case localPort: Config.LocalPort =>
						localPort

					case dep =>
						sys.error(
							s"do something to process ${dep.getClass.getName.replaceAll("_Decorated$", "")}"
						)
				}
		}
	val inc: Later[Stream[File]] =
		consume("cgc") {
			sources: Iterable[TProperTask.Source] =>
				sources.map {
					case (root: File, _) =>
						root.getAbsoluteFile
				}.distinctBy((_: File).AbsolutePath)
		}

	val dfs: Later[DynamicsFS] =
		consume("cgc") {
			sources: Iterable[TProperTask.Source] =>
				new DynamicsFS {
					/**
						* list all files in the fs
						*/
					override def * : Stream[String] =
						sources.toStream.map {
							case (root: File, path: String) =>
								getProject.getProjectDir.walkTo(root / path)
						}

					/**
						* open the named path, return null iff it's not there
						*/
					override def ?(path: String): InputStream =
						sys.error("need some magic to unmap this")
				}
		}
	@BeanProperty
	var standalone: Boolean = false
}
