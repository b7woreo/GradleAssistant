plugins {
    id("java-gradle-plugin")
    id("org.jetbrains.kotlin.jvm") version "1.4.30"
    id("maven-publish")
}

publishing {
    publications {
        repositories {
            maven {
                url = uri("https://maven.pkg.github.com/renjie-c/gdr")
                credentials {
                    username = System.getenv("USERNAME")
                    password = System.getenv("TOKEN")
                }
            }
        }

        create<MavenPublication>("main") {
            groupId = "com.chrnie.gdr"
            artifactId = "plugin"
            version = System.getenv("VERSION")

            from(components["java"])
        }
    }
}

repositories {
    google()
    mavenCentral()
}

gradlePlugin {
    val main by plugins.creating {
        id = "com.chrnie.gdr"
        implementationClass = "com.chrnie.gdr.GdrPlugin"
    }
}

dependencies{
    implementation("com.android.tools.build:gradle:3.4.0")
}