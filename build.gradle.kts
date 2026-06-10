import groovy.json.JsonSlurper

plugins {
    alias(libs.plugins.moddev)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.modPublish)
}

val modId: String by project
val modName: String by project
val modVersion: String by project
val minecraftVersion: String by project
val neoVersionRange: String by project
val neoforgeVersion: String by project
val authorName: String by project

version = modVersion
group = "concerrox.minecraft.emiplusplus"

base {
    archivesName = modId
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

kotlin {
    jvmToolchain(21)
}

neoForge {
    version = neoforgeVersion

    runs {
        configureEach {
            systemProperty("forge.logging.markers", "REGISTRIES")
            logLevel = org.slf4j.event.Level.DEBUG
        }

        register("client") {
            client()
            systemProperty("neoforge.enabledGameTestNamespaces", modId)
        }

        register("server") {
            server()
            programArgument("--nogui")
            systemProperty("neoforge.enabledGameTestNamespaces", modId)
        }

        register("data") {
            data()
            programArguments.addAll(
                "--mod",
                modId,
                "--all",
                "--output",
                file("src/generated/resources/").absolutePath,
                "--existing",
                file("src/main/resources/").absolutePath
            )
        }
    }

    mods {
        register(modId) {
            sourceSet(sourceSets.main.get())
        }
    }
}

repositories {
    mavenCentral()
    maven {
        name = "KotlinForForge"
        url = uri("https://thedarkcolour.github.io/KotlinForForge/")
    }
    maven {
        name = "CurseMaven"
        url = uri("https://cursemaven.com")
    }
    maven {
        name = "Modrinth"
        url = uri("https://api.modrinth.com/maven")
    }
    maven {
        name = "TerraformersMC"
        url = uri("https://maven.terraformersmc.com/releases")
    }
}

dependencies {
    // EMI
    implementation(libs.emi.neoforge)

    // Kotlin for Forge - shared Kotlin runtime (no jarJar conflicts)
    implementation(libs.kff)

    // Mixin annotation processor (Mixin 0.8.7 shipped by NeoForge)
    compileOnly("org.spongepowered:mixin:0.8.7:processor")
    annotationProcessor("org.spongepowered:mixin:0.8.7:processor")
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release = 21
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
            freeCompilerArgs.add("-Xjvm-default=all")
        }
    }

    processResources {
        filesMatching("META-INF/neoforge.mods.toml") {
            expand(
                mapOf(
                    "modId" to modId,
                    "modName" to modName,
                    "modVersion" to modVersion,
                    "minecraftVersionRange" to "[$minecraftVersion,)",
                    "neoVersionRange" to neoVersionRange,
                    "authorName" to authorName
                )
            )
        }
    }
}

fun getLatestChangelog(): String {
    val file = rootProject.file("update.json")
    val json = JsonSlurper().parse(file) as Map<*, *>
    val versions = json[minecraftVersion] as Map<*, *>
    val text = versions[modVersion] as String
    return text.lines().joinToString("\n") { "- $it" }
}

publishMods {
    file = tasks.jar.get().archiveFile
    changelog = getLatestChangelog()
    type = ALPHA

    version = modVersion
    displayName = "NeoForge $modVersion"
    modLoaders.add("neoforge")

    curseforge {
        accessToken = providers.gradleProperty("curseforgeApiToken")

        projectId = "1335150"
        projectSlug = modId
        client = true
        server = false
        minecraftVersions.add(minecraftVersion)
        requires("emi", "kotlin-for-forge")
    }

    modrinth {
        accessToken = providers.gradleProperty("modrinthToken")

        projectId = "AWMWYMwC"
        minecraftVersions.add(minecraftVersion)
        requires("emi", "kotlin-for-forge")
    }
}