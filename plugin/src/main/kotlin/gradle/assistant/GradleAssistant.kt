package gradle.assistant

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import gradle.assistant.task.ReportConfigurationDependencies
import gradle.assistant.task.ReportProjectDependencies
import gradle.assistant.task.ReportTaskDependencies
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.configurationcache.extensions.capitalized

class GradleAssistant : Plugin<Project> {

    companion object {
        private const val TASK_GROUP = "GradleAssistant"
    }

    override fun apply(project: Project) {
        project.plugins.all {
            when (it) {
                is JavaPlugin -> {
                    project.extensions.getByType(SourceSetContainer::class.java)
                        .configureReportDependenciesTask(project)
                }

                is AppPlugin -> {
                    project.extensions.getByType(AndroidComponentsExtension::class.java)
                        .configureReportDependenciesTask(project)
                }

                is LibraryPlugin -> {
                    project.extensions.getByType(AndroidComponentsExtension::class.java)
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
            "reportConfigurationDependencies",
            ReportConfigurationDependencies::class.java
        ) {
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

    private fun AndroidComponentsExtension<*, *, *>.configureReportDependenciesTask(project: Project) {
        onVariants { variant ->
            project.tasks.create(
                "report${variant.name.capitalized()}ProjectDependencies",
                ReportProjectDependencies::class.java
            ) {
                it.group = TASK_GROUP
                it.variantName = variant.name
                it.configurationName = variant.runtimeConfiguration.name
                it.outputDir = project.buildDir.resolve("reports/projectDependencies")
                it.outputs.upToDateWhen { false }
            }
        }
    }

}