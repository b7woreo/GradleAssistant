include(":sample:android-application")
include(":sample:android-library")
include(":sample:java-library")

includeBuild("plugin") {
    dependencySubstitution {
        substitute(module("com.chrnie.gdr:plugin")).with(project(":"))
    }
}
