plugins {
    id("java")
    id("io.github.knownitwhy.gdr")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies{
    implementation("io.reactivex.rxjava3:rxjava:3.0.11")
}