package peterlavalle.cgc

import org.gradle.api.{GradleException, Task}
import peterlavalle.gbt.TProperTask
import peterlavalle.gbt.TProperTask.Phase

/**
	* task that's run before C/++ compilers; should run any generation tasks
	*
	* ... doing this as its own class is *slight* overkill, but, it does make other things easier and cleaner
	*/
class Generate extends TProperTask.TTaskPhased(
	"build",
	"pre-build task theoretically used for c/++ generation"
) {
	connect {
		val compileName: String =
			phase match {
				case Phase.Main => "classes"
				case Phase.Test => "testClasses"
				case _ =>
					sys.error(s"build-$phase-artifact must depends on this!")
			}
		val compileTask: Task =
			getProject.getTasks.findByName(compileName)

		requyre[GradleException](null != compileTask, s"Couldn't find task `$compileName`")

		compileTask.dependsOn(this)
	}
}
