package gradle.assistant.task

import gradle.assistant.graphic.Graphic
import gradle.assistant.graphic.MermaidGraphic
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

abstract class ReportProjectDependencies : DefaultTask() {

    @get:Input
    abstract val variantName: Property<String>

    @get:Input
    abstract val configurationName: Property<String>

    @get:Input
    @get:Optional
    abstract val type: Property<Type>

    @Option(
        option = "type",
        description = "指定要输出依赖的类型，可选值：all、project、external，默认值：all"
    )
    fun typeName(value: String) {
        val type = checkNotNull(Type.find(value)) { "unknown type: $value" }
        this.type.set(type)
    }

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun report() {
        val variantName = variantName.get()
        val configurationName = this.configurationName.get()
        val type = this.type.getOrElse(Type.All)
        val outputFile = this.outputDir.file("${variantName}.html").get().asFile

        val configuration = checkNotNull(project.configurations.findByName(configurationName)) {
            "Can not found configuration: $configurationName"
        }

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