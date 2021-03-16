plugins {
    id("com.android.application")
    id("com.chrnie.gdr")
}

android {
    compileSdkVersion(30)
    buildToolsVersion("30.0.3")

    defaultConfig {
        minSdkVersion(21)
        targetSdkVersion(30)
    }
}

dependencies {
    implementation(project(":sample:android-library"))
}