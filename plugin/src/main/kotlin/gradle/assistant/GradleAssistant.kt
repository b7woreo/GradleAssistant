package gradle.assistant

import com.android.build.api.variant.AndroidComponentsExtension
import gradle.assistant.task.ConfigurationDependencies
import gradle.assistant.task.ProjectDependencies
import gradle.assistant.task.TaskDependencies
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer

abstract class GradleAssistant : Plugin<Project> {

    override fun apply(project: Project) {
        project.tasks.register(
            "configurationDependencies",
            ConfigurationDependencies::class.java
        ) {
            it.group = TASK_GROUP
            it.dependenciesInfoFile.set(project.buildDir.resolve("reports/configurationDependencies.html"))
            it.outputs.upToDateWhen { false }
        }

        project.tasks.register(
            "projectDependencies",
            ProjectDependencies::class.java
        ) {
            it.group = TASK_GROUP
            it.dependenciesInfoFile.set(project.buildDir.resolve("reports/projectDependencies.html"))
            it.outputs.upToDateWhen { false }
        }

        project.tasks.register(
            "taskDependencies",
            TaskDependencies::class.java
        ) {
            it.group = TASK_GROUP
            it.dependenciesInfoFile.set(project.buildDir.resolve("reports/taskDependencies.html"))
            it.outputs.upToDateWhen { false }
        }
    }

    companion object {
        private const val TASK_GROUP = "assistant"
    }
}