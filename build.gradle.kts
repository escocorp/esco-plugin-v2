plugins {
    java
    kotlin("jvm") version "2.4.0"
    kotlin("plugin.serialization") version "2.4.0"
}

version = "1.0"

val javaVersion = 25

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(javaVersion))
}

kotlin {
    jvmToolchain(javaVersion)
}

sourceSets.main {
    java.srcDirs("src")
}

repositories {
    mavenCentral()

    maven("https://maven.xpdustry.com/releases")

    ivy {
        url = uri("https://github.com/")
        patternLayout {
            artifact("/[organisation]/[module]/releases/download/[revision]/dependencies.jar")
        }
        metadataSources {
            artifact()
        }
    }

    ivy {
        url = uri("https://github.com/")
        patternLayout {
            artifact("/[organisation]/[module]/releases/download/master/[revision].jar")
        }
        metadataSources {
            artifact()
        }
    }
}

val mindustryVersion = "v159"
val jabelVersion = "93fde537c7"
var nohornyVersion = "4.0.0-beta.7"

val useLatest = false

dependencies {
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:1.11.0")

    compileOnly(
        if (useLatest)
            "Anuken:MindustryBuilds:latest"
        else
            "Anuken:Mindustry:$mindustryVersion"
    )

    implementation("org.postgresql:postgresql:42.7.12")
    implementation("com.zaxxer:HikariCP:7.1.0")

    implementation("org.slf4j:slf4j-api:2.0.18")
    implementation("org.slf4j:slf4j-simple:2.0.18")

    implementation("com.fasterxml.jackson.core:jackson-databind:2.22.0")

    implementation("io.github.cdimascio:dotenv-java:3.2.0")

    implementation("net.dv8tion:JDA:6.5.0") {
        exclude(module = "opus-java")
    }

    implementation(kotlin("stdlib"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")

    //compileOnly(files("nohorny-client.jar"))
    compileOnly("com.xpdustry:nohorny-common:$nohornyVersion")
    compileOnly("com.xpdustry:nohorny-client:$nohornyVersion")

    implementation(platform("software.amazon.awssdk:bom:2.47.5"))
    implementation("software.amazon.awssdk:s3")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget(javaVersion.toString()))
    }
}

val versionGitDir = layout.buildDirectory.dir("version-git")

val writeVersionFile = tasks.register("writeVersionFile") {
    val gitDir = file("${rootDir}/.git")

    if (gitDir.exists()) {
        inputs.file("${gitDir}/HEAD")

        val packed = file("${gitDir}/packed-refs")
        if (packed.exists()) {
            inputs.file(packed)
        }

        val refs = file("${gitDir}/refs")
        if (refs.exists()) {
            inputs.dir(refs)
        }
    }

    outputs.dir(versionGitDir)

    doLast {
        val dir = versionGitDir.get().asFile
        dir.mkdirs()

        val versionFile = dir.resolve("version")

        val proc = ProcessBuilder(
            "git",
            "rev-parse",
            "--short",
            "HEAD"
        )
            .directory(rootDir)
            .start()

        proc.waitFor()

        versionFile.writeText(
            if (proc.exitValue() == 0)
                proc.inputStream.bufferedReader().readText().trim()
            else
                "unknown"
        )
    }
}

tasks.jar {
    dependsOn(writeVersionFile)

    archiveFileName.set("${project.name}.jar")

    from({
        configurations.runtimeClasspath.get()
            .map { if (it.isDirectory) it else zipTree(it) }
    })

    from(rootDir) {
        include("plugin.json")
        include("block_colors.png")
    }

    from(versionGitDir) {
        include("version")
    }

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
