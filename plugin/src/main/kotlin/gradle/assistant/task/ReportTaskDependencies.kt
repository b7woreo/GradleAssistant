package gradle.assistant.task

import gradle.assistant.graphic.Graphic
import gradle.assistant.graphic.MermaidGraphic
import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

abstract class ReportTaskDependencies : DefaultTask() {

    @get:Input
    @get:Optional
    abstract val taskName: Property<String>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @Option(
        option = "task",
        description = "指定要输出依赖关系的任务名，如果不设置则输出当前项目下所有任务的依赖关系",
    )
    fun taskName(value: String) {
        taskName.set(value)
    }

    @TaskAction
    fun report() {
        val taskName = this.taskName.orNull
        val outputFile = this.outputDir.file("${taskName ?: "dependencies"}.html").get().asFile

        val graphic = MermaidGraphic()
        graphic.render(outputFile) {
            if (taskName == null) {
                project.tasks.forEach { task ->
                    buildGraph(task)
                }
            } else {
                val targetTask = checkNotNull(project.tasks.findByName(taskName)) {
                    "can not found task: ${this@ReportTaskDependencies.taskName}"
                }
                buildGraph(targetTask)
            }
        }
    }

    private fun Graphic.Builder.buildGraph(
        targetTask: Task,
    ) {
        node(targetTask.content, Graphic.Shape.Box)

        targetTask.taskDependencies
            .getDependencies(targetTask)
            .filter { it.project == project }
            .forEach {
                buildGraph(it)
                edge(targetTask.content, it.content)
            }
    }

    private val Task.content: String
        get() {
            return """
            <b>${name}</b>
            ${this::class.java.superclass.name}
            """.trimIndent()
        }

}