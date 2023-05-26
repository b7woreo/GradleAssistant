package gradle.assistant.task

import gradle.assistant.graphic.Graphic
import gradle.assistant.graphic.MermaidGraphic
import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

abstract class TaskDependencies : DefaultTask() {

    @get:Input
    @get:Optional
    abstract val taskName: Property<String>

    @get:OutputFile
    abstract val dependenciesInfoFile: RegularFileProperty

    @Option(
        option = "task",
        description = "task name, output all task dependencies by default",
    )
    fun taskName(value: String) {
        taskName.set(value)
    }

    @TaskAction
    fun report() {
        val taskName = this.taskName.orNull
        val outputFile = dependenciesInfoFile.asFile.get()

        val graphic = MermaidGraphic()
        graphic.render(outputFile) {
            if (taskName == null) {
                project.tasks.forEach { task ->
                    buildGraph(task)
                }
            } else {
                val targetTask = checkNotNull(project.tasks.findByName(taskName)) {
                    "can not found task: ${this@TaskDependencies.taskName}"
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