plugins {
    id("java-gradle-plugin")
    id("org.jetbrains.kotlin.jvm") version "1.4.30"
    id("maven-publish")
}

gradlePlugin {
    val main by plugins.creating {
        id = "com.chrnie.gdr"
        implementationClass = "com.chrnie.gdr.GdrPlugin"
    }
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation("com.android.tools.build:gradle:3.4.0")
}

publishing {
    publications {
        repositories {
            maven {
                name = "ossrh"
                url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
                credentials {
                    username = System.getenv("OSSRH_USERNAME")
                    password = System.getenv("OSSRH_PASSWORD")
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