plugins {
    `java-library`
    `maven-publish`
    idea
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

repositories {
    mavenLocal()
    maven("https://repo.maven.apache.org/maven2/")
    maven("https://jitpack.io")
    maven("https://repo.opencollab.dev/maven-releases/")
    maven("https://repo.opencollab.dev/maven-snapshots/")
}

group = "cn.powernukkitx"
version = "1.0.0-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

dependencies {
    implementation("com.squareup:javapoet:1.13.0")
    implementation("com.github.AllayMC:NBT:3.0.8")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("it.unimi.dsi:fastutil:8.5.12")
    implementation("org.jetbrains:annotations:24.1.0")
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
}

//Automatically download dependencies source code
idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = false
    }
}

java {
    withSourcesJar()
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

sourceSets {
    main {
        resources {
            srcDirs("src/main/resources")
        }
    }
}

tasks.compileJava {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-Xpkginfo:always")
    java.sourceCompatibility = JavaVersion.VERSION_17
    java.targetCompatibility = JavaVersion.VERSION_17
}

tasks.test {
    useJUnitPlatform()
    jvmArgs(listOf("--add-opens", "java.base/java.lang=ALL-UNNAMED"))
    jvmArgs(listOf("--add-opens", "java.base/java.io=ALL-UNNAMED"))
}

tasks.withType<AbstractCopyTask>() {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}
