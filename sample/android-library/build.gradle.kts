plugins {
    id("com.android.library")
    id("io.github.knownitwhy.gdr")
}

android {
    compileSdkVersion(30)
    buildToolsVersion("30.0.3")

    defaultConfig {
        minSdkVersion(21)
        targetSdkVersion(30)
    }
}

dependencies{
    implementation(project(":sample:java-library"))
}