include(":sample:android-application")
include(":sample:android-library")
include(":sample:java-library")

includeBuild("plugin") {
    dependencySubstitution {
        substitute(module("io.github.knownitwhy:plugin")).using(project(":"))
    }
}
