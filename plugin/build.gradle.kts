plugins {
    id("com.gradle.plugin-publish") version "1.0.0"
    id("org.jetbrains.kotlin.jvm") version "1.7.0"
}

group = "io.github.knownitwhy"
version = findProperty("gradle.publish.version") as String? ?: "snapshot"

pluginBundle {
    website = "https://github.com/knownitwhy/gdr"
    vcsUrl = "https://github.com/knownitwhy/gdr.git"
    tags = listOf("dependency","dependencies")
}

gradlePlugin {
    plugins.create("gdr") {
        id = "io.github.knownitwhy.gdr"
        implementationClass = "io.github.knownitwhy.gdr.GdrPlugin"
        displayName = "Plugin for report dependencies of project, task, configuration"
        description = "A plugin that helps you analyze dependencies of project, task, configuration"
    }
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation("com.android.tools.build:gradle:4.0.2")
}
