package com.chrnie.gdr.task

import com.chrnie.gdr.dot.DotScope
import com.chrnie.gdr.dot.Graphviz
import com.chrnie.gdr.dot.Shape
import com.chrnie.gdr.dot.buildDot
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
    
    @Input
    lateinit var variantName: String

    @Input
    lateinit var configurationName: String

    @Option(option = "type", description = "指定要输出依赖的类型，可选值：all、project、external，默认值：all")
    var typeName: String = Type.All.value

    @OutputDirectory
    lateinit var outputDir: File

    @get:OutputFile
    val outputFile: File
        get() = outputDir.resolve("${variantName}.png")

    @TaskAction
    fun report() {
        val configuration = checkNotNull(project.configurations.findByName(configurationName)) {
            "Can not found configuration: $configurationName"
        }
        val type = checkNotNull(Type.find(typeName)) { "Please set correct args: --type" }

        val dot = buildDot { buildGraph(configuration.incoming.resolutionResult.root, type) }
        Graphviz.render(project, dot, outputFile)
    }

    private fun DotScope.buildGraph(
        root: ResolvedComponentResult,
        filter: (ResolvedComponentResult) -> Boolean
    ) {
        node(root.id.displayName) {
            shape = when (root.id) {
                is ProjectComponentIdentifier -> Shape.Oval
                is ModuleComponentIdentifier -> Shape.Box
                else -> throw IllegalStateException("Unknown component type: ${root.id::class}")
            }
        }

        root.dependencies
            .mapNotNull {
                if (it !is ResolvedDependencyResult) null
                else it.selected
            }
            .filter { filter(it) }
            .forEach {
                edge(root.id.displayName, it.id.displayName)
                buildGraph(it, filter)
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