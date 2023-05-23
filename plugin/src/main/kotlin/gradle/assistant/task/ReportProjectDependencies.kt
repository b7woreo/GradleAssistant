package gradle.assistant.task

import gradle.assistant.graphic.Graphic
import gradle.assistant.graphic.MermaidGraphic
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import java.io.File

abstract class ReportProjectDependencies : DefaultTask() {

    @get:Input
    lateinit var variantName: String

    @get:Input
    lateinit var configurationName: String

    @get:Input
    @Option(
        option = "type",
        description = "指定要输出依赖的类型，可选值：all、project、external，默认值：all"
    )
    var typeName: String = Type.All.value

    @get:OutputDirectory
    lateinit var outputDir: File

    @get:OutputFile
    val outputFile: File
        get() = outputDir.resolve("${variantName}.html")

    @TaskAction
    fun report() {
        val configuration = checkNotNull(project.configurations.findByName(configurationName)) {
            "Can not found configuration: $configurationName"
        }
        val type = checkNotNull(Type.find(typeName)) { "Please set correct args: --type" }

        val graphic = MermaidGraphic()
        graphic.render(outputFile) {
            val root = configuration.incoming.resolutionResult.root
            buildGraph(root, type)
        }
    }

    private fun Graphic.Builder.buildGraph(
        root: ResolvedComponentResult,
        filter: (ResolvedComponentResult) -> Boolean
    ) {
        node(root.content, root.shape)

        root.dependencies
            .mapNotNull {
                if (it !is ResolvedDependencyResult) null
                else it.selected
            }
            .filter { filter(it) }
            .forEach {
                buildGraph(it, filter)
                edge(root.content, it.content)
            }
    }

    private val ResolvedComponentResult.content: String
        get() {
            return when (val id = this.id) {
                is ProjectComponentIdentifier -> id.projectPath
                else -> id.displayName
            }
        }

    private val ResolvedComponentResult.shape: Graphic.Shape
        get() {
            return when (this.id) {
                is ProjectComponentIdentifier -> Graphic.Shape.Box
                else -> Graphic.Shape.Oval
            }
        }

    enum class Type(val value: String) : (ResolvedComponentResult) -> Boolean {
        All("all") {
            override fun invoke(result: ResolvedComponentResult): Boolean = true
        },

        Project("project") {
            override fun invoke(result: ResolvedComponentResult): Boolean {
                return result.id is ProjectComponentIdentifier
            }
        },

        External("external") {
            override fun invoke(result: ResolvedComponentResult): Boolean {
                return result.id is ModuleComponentIdentifier
            }
        };

        companion object {

            fun find(value: String): Type? {
                return values().firstOrNull { value == it.value }
            }
        }
    }
}