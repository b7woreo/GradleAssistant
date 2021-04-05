plugins {
    id("java-gradle-plugin")
    id("org.jetbrains.kotlin.jvm") version "1.4.30"
    id("maven-publish")
    id("signing")
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

tasks.create("javadocJar", Jar::class.java) {
    archiveClassifier.set("javadoc")
    from("javadoc")
}

tasks.create("sourcesJar", Jar::class.java) {
    archiveClassifier.set("sources")
    from(sourceSets["main"].allSource)
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
            version = System.getenv("VERSION")?:"0.0.2"

            from(components["java"])
            artifact(tasks["javadocJar"])
            artifact(tasks["sourcesJar"])
            
            pom { 
                name.set("gdr")
                description.set("Gradle 图形化依赖关系导出工具")
                url.set("https://github.com/renjie-c/gdr")
                
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                
                scm {
                    url.set("https://github.com/renjie-c/gdr")
                }
                
                developers {
                    developer {
                        id.set("chrnie")
                        name.set("ChenRenJie")
                        email.set("chrnie@foxmail.com")
                    }
                }
            }
        }
    }
}

signing {
    useInMemoryPgpKeys(System.getenv("SIGNING_KEY"), System.getenv("SIGNING_PASSWORD"))
    sign(publishing.publications["main"])
}
