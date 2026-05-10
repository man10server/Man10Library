plugins {
    kotlin("jvm") version "2.4.0-Beta2"
    id("com.gradleup.shadow") version "9.4.1"
}

val pluginVersion: String by project.ext

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:$pluginVersion-R0.1-SNAPSHOT")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    compileOnly("com.mojang:brigadier:1.0.18")
}

kotlin {
    jvmToolchain(21)
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}
