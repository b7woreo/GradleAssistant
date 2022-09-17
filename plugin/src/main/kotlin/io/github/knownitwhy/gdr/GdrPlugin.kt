package io.github.knownitwhy.gdr

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.BaseVariant
import io.github.knownitwhy.gdr.ext.getTaskName
import io.github.knownitwhy.gdr.task.ReportConfigurationDependencies
import io.github.knownitwhy.gdr.task.ReportProjectDependencies
import io.github.knownitwhy.gdr.task.ReportTaskDependencies
import org.gradle.api.DomainObjectSet
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.SourceSetContainer

class GdrPlugin : Plugin<Project> {

    companion object{
        private const val TASK_GROUP = "help"
    }

    override fun apply(project: Project) {
        project.plugins.all {
            when (it) {
                is JavaPlugin -> {
                    project.extensions.getByType(SourceSetContainer::class.java)
                        .configureReportDependenciesTask(project)
                }
                is AppPlugin -> {
                    project.extensions.getByType(AppExtension::class.java)
                        .applicationVariants
                        .configureReportDependenciesTask(project)
                }
                is LibraryPlugin -> {
                    project.extensions.getByType(LibraryExtension::class.java)
                        .libraryVariants
                        .configureReportDependenciesTask(project)
                }
            }
        }

        project.tasks.create(
            "reportTaskDependencies",
            ReportTaskDependencies::class.java
        ) {
            it.group = TASK_GROUP
            it.outputDir = project.buildDir.resolve("reports/taskDependencies")
            it.outputs.upToDateWhen { false }
        }

        project.tasks.create(
            "reportConfigurationDependencies" ,
            ReportConfigurationDependencies::class.java
        ){
            it.group = TASK_GROUP
            it.outputDir = project.buildDir.resolve("reports/configurationDependencies")
            it.outputs.upToDateWhen { false }
        }
    }

    private fun SourceSetContainer.configureReportDependenciesTask(project: Project) {
        all { sourceSet ->
            val taskName = sourceSet.getTaskName("report", "projectDependencies")
            project.tasks.create(taskName, ReportProjectDependencies::class.java) {
                it.group = TASK_GROUP
                it.variantName = sourceSet.name
                it.configurationName = sourceSet.runtimeClasspathConfigurationName
                it.outputDir = project.buildDir.resolve("reports/projectDependencies")
                it.outputs.upToDateWhen { false }
            }
        }
    }

    private fun DomainObjectSet<out BaseVariant>.configureReportDependenciesTask(project: Project) {
        all { variant ->
            val taskName = variant.getTaskName("report", "projectDependencies")
            project.tasks.create(taskName, ReportProjectDependencies::class.java) {
                it.group = TASK_GROUP
                it.variantName = variant.name
                it.configurationName = variant.runtimeConfiguration.name
                it.outputDir = project.buildDir.resolve("reports/projectDependencies")
                it.outputs.upToDateWhen { false }
            }
        }
    }

}