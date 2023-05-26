package gradle.assistant.task

import gradle.assistant.graphic.Graphic
import gradle.assistant.graphic.MermaidGraphic
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

abstract class ConfigurationDependencies : DefaultTask() {

    @get:Input
    @get:Optional
    abstract val configurationName: Property<String>

    @get:OutputFile
    abstract val dependenciesInfoFile: RegularFileProperty

    @Option(
        option = "configuration",
        description = "configuration name，output all configuration dependencies by default"
    )
    fun configurationName(value: String) {
        configurationName.set(value)
    }

    @TaskAction
    fun report() {
        val configurationName = this.configurationName.orNull
        val outputFile = dependenciesInfoFile.asFile.get()

        val graphic = MermaidGraphic()
        graphic.render(outputFile) {
            if (configurationName == null) {
                project.configurations.forEach { configuration ->
                    buildGraph(configuration)
                }
            } else {
                val configuration = project.configurations.findByName(configurationName)
                checkNotNull(configuration) { "can not found configuration: $configurationName" }
                buildGraph(configuration)
            }
        }
    }

    private fun Graphic.Builder.buildGraph(
        configuration: Configuration,
    ) {
        node(configuration.content, configuration.shape)
        configuration.extendsFrom.forEach {
            buildGraph(it)
            edge(configuration.content, it.content)
        }
    }

    private val Configuration.content: String
        get() {
            return """
            <b>$name</b>
            <em>${role.description}</em>
            ${
                attributes.keySet()
                    .map { attr -> "${attr.name}:${attributes.getAttribute(attr)}" }
                    .sorted()
                    .joinToString("\n")
            }
            """.trimIndent()
        }

    private val Configuration.shape: Graphic.Shape
        get() {
            return when (this.role) {
                Role.Dependencies -> Graphic.Shape.Box
                Role.Elements -> Graphic.Shape.Oval
                Role.Classpath -> Graphic.Shape.Diamond
                Role.Legacy -> Graphic.Shape.Box
            }
        }

    private val Configuration.role: Role
        get() = when (isCanBeConsumed) {
            true -> when (isCanBeResolved) {
                true -> Role.Legacy
                false -> Role.Elements
            }

            false -> when (isCanBeResolved) {
                true -> Role.Classpath
                false -> Role.Dependencies
            }
        }

    private enum class Role(val description: String) {
        Dependencies("Bucket of dependencies"),
        Elements("Exposed to consumers"),
        Classpath("Resolve for certain usage"),
        Legacy("Legacy")
    }
}