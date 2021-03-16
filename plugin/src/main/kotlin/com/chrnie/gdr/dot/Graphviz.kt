package com.chrnie.gdr.dot

import org.gradle.api.Project
import java.io.ByteArrayInputStream
import java.io.File

object Graphviz {
    private const val ENV_GRAPHVIZ_HOME = "GRAPHVIZ_HOME"
    
    fun render(
        project: Project,
        dot: String,
        outputFile: File
    ) {
        val graphvizHome = System.getenv(ENV_GRAPHVIZ_HOME)
            ?: throw IllegalStateException("Please set env: GRAPHVIZ_HOME")
        
        project.exec { spec ->
            spec.commandLine(File(graphvizHome).resolve("bin/dot").absolutePath)
            spec.args("-T", "png", "-o", outputFile.absolutePath)
            spec.standardInput = ByteArrayInputStream(dot.toByteArray())
        }
    }
}
