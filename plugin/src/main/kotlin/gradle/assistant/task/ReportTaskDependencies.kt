package gradle.assistant.task

import gradle.assistant.dot.DotScope
import gradle.assistant.dot.Graphviz
import gradle.assistant.dot.Shape
import gradle.assistant.dot.buildDot
import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import java.io.File

abstract class ReportTaskDependencies : DefaultTask() {

    @get:Input
    @Option(option = "task", description = "指定要输出依赖关系的任务名，如果不设置则输出当前项目下所有任务的依赖关系")
    var taskName: String? = null

    @get:Input
    @Option(option = "includeDependentProject", description = "是否包含依赖项目的任务")
    var includeDependentProject: Boolean = false

    @get:Input
    @Option(option = "verbose", description = "输出附加信息")
    var verbose: Boolean = false

    @get:OutputDirectory
    lateinit var outputDir: File

    @get:OutputFile
    val outputFile: File
        get() = outputDir.resolve("${taskName ?: "dependencies"}.png")

    @TaskAction
    fun report() {
        val filter: (Task) -> Boolean = {
            if (includeDependentProject) true
            else it.project == project
        }

        val dot = buildDot {
            val taskName = taskName
            if (taskName == null) {
                project.tasks.forEach { task ->
                    buildGraph(task, verbose, filter)
                }
            } else {
                val targetTask = checkNotNull(project.tasks.findByName(taskName)) {
                    "Can not found task: ${this@ReportTaskDependencies.taskName}"
                }
                buildGraph(targetTask, verbose, filter)
            }
        }
        Graphviz.render(project, dot, outputFile)
    }

    private fun DotScope.buildGraph(targetTask: Task, verbose: Boolean, filter: (Task) -> Boolean) {
        node(targetTask.path) {
            shape = if (targetTask.project == project) Shape.Oval else Shape.Box
            if (verbose) {
                label = "${targetTask.path}\n[${targetTask::class.java.superclass.name}]"
            }
        }

        targetTask.taskDependencies
            .getDependencies(targetTask)
            .filter { filter(it) }
            .forEach {
                edge(targetTask.path, it.path)
                buildGraph(it, verbose, filter)
            }
    }

}