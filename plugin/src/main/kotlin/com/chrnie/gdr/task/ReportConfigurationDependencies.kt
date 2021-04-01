package com.chrnie.gdr.task

import com.chrnie.gdr.dot.DotScope
import com.chrnie.gdr.dot.Graphviz
import com.chrnie.gdr.dot.Shape
import com.chrnie.gdr.dot.buildDot
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import java.io.File

abstract class ReportConfigurationDependencies : DefaultTask() {

    @Option(
        option = "configuration",
        description = "指定要输出依赖关系的配置名，如果不设置则输出当前项目下所有配置的依赖关系"
    )
    var configurationName: String? = null

    @Option(option = "verbose", description = "输出附加信息")
    var verbose: Boolean = false

    @OutputDirectory
    lateinit var outputDir: File

    @get:OutputFile
    val outputFile: File
        get() = outputDir.resolve("${configurationName ?: "dependencies"}.png")

    @TaskAction
    fun report() {
        val dot = buildDot {
            val configurationName = configurationName
            if (configurationName == null) {
                project.configurations.forEach { configuration ->
                    buildGraph(configuration, verbose)
                }
            } else {
                val targetConfiguration =
                    checkNotNull(project.configurations.findByName(configurationName)) {
                        "Can not found configuration: $configurationName"
                    }
                buildGraph(targetConfiguration, verbose)
            }
        }
        Graphviz.render(project, dot, outputFile)
    }

    private fun DotScope.buildGraph(
        configuration: Configuration,
        verbose: Boolean
    ) {
        node(configuration.name) {
            val role = configuration.role
            color = role.color
            shape = Shape.Box
            if (verbose) {
                val description = role.description
                val attributes = configuration.attributes.run {
                    keySet().joinToString(separator = "\n") { "${it.name}: ${getAttribute(it)}" }
                }
                label = """
                    ${configuration.name}
                    [$description]
                    $attributes
                """.trimIndent()
            }
        }

        configuration.extendsFrom.forEach {
            edge(configuration.name, it.name)
            buildGraph(it, verbose)
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

    private enum class Role(val description: String, val color: Int) {
        Dependencies("Bucket of dependencies", 0xf44336),
        Elements("Exposed to consumers", 0x42a5f5),
        Classpath("Resolve for certain usage", 0x4caf50),
        Legacy("Legacy", 0x9e9e9e)
    }
}