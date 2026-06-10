
import io.github.themrmilchmann.gradle.publish.curseforge.ChangelogFormat
import io.github.themrmilchmann.gradle.publish.curseforge.GameVersion
import io.github.themrmilchmann.gradle.publish.curseforge.ReleaseType

plugins {
    alias(libs.plugins.moddev)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.minotaur)
    alias(libs.plugins.curseforge.publish)
}

val modId: String by project
val modName: String by project
val modVersion: String by project
val minecraftVersionRange: String by project
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
                "--mod", modId,
                "--all",
                "--output", file("src/generated/resources/").absolutePath,
                "--existing", file("src/main/resources/").absolutePath
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
                    "minecraftVersionRange" to minecraftVersionRange,
                    "neoVersionRange" to neoVersionRange,
                    "authorName" to authorName
                )
            )
        }
    }
}

fun getLatestChangelog(): String {
    val file = rootProject.file("CHANGELOG.md")
    if (!file.exists()) return "No changelog provided."
    val lines = file.readLines()
    val entries = mutableListOf<String>()
    var inCurrent = false
    for (line in lines) {
        if (line.startsWith("## [") && !inCurrent) { inCurrent = true; continue }
        if (line.startsWith("## [") && inCurrent) break
        if (inCurrent) entries.add(line)
    }
    return entries.joinToString("\n").trim().ifEmpty { "No changelog provided." }
}

modrinth {
    token.set(providers.gradleProperty("modrinthToken"))

    projectId.set("emixx")
    versionNumber.set(modVersion)
    versionName.set("NeoForge $modVersion")
    versionType.set("alpha")
    uploadFile.set(tasks.jar)
    changelog.set(getLatestChangelog())

    gameVersions.addAll("1.21.1")
    loaders.addAll("neoforge")
    dependencies {
        required.project("emi")
        required.project("kotlinforforge")
    }
}

curseforge {
    apiToken.set(providers.gradleProperty("curseforgeApiToken"))

    publications {
        register("curseForge") {
            projectId.set("1335150")
            gameVersions.add(GameVersion("minecraft-1-21", "1.21.1"))

            artifacts {
                register("neoForge") {
                    displayName.set("NeoForge $modVersion")
                    releaseType = ReleaseType.ALPHA

                    changelog {
                        format = ChangelogFormat.MARKDOWN
                        from(file("CHANGELOG.md"))
                    }

                    relations {
                        requiredDependency("emi")
                        requiredDependency("kotlin-for-forge")
                    }
                }
            }
        }
    }
}
