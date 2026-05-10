plugins {
    kotlin("jvm") version "2.4.0-Beta2"
    id("com.gradleup.shadow") version "9.4.1"
    `maven-publish`
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation(kotlin("stdlib"))

    compileOnly(libs.paper.api)
    compileOnly(libs.brigadier)
}

kotlin {
    jvmToolchain(21)
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("gpr") {
            artifactId = "man10-library"
            from(components["java"])
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/man10server/Man10Library")
            credentials {
                username = System.getenv("GITHUB_USERNAME") ?: project.findProperty("gpr.user").toString()
                password = System.getenv("GITHUB_TOKEN") ?: project.findProperty("gpr.key").toString()
            }
        }
    }
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}
