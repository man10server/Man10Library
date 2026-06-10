plugins {
    kotlin("jvm") version "2.4.0-Beta2"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation(kotlin("stdlib"))

    compileOnly(libs.paper.api)
    implementation(rootProject)
}