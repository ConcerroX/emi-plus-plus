import net.neoforged.moddevgradle.dsl.RunModel
import org.slf4j.event.Level

plugins {
    id("idea")
    id("maven-publish")
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.modDevGradle)
}

val modId = "emixx"
val modName = "EMI++"
version = "1.3.0"
group = "concerrox.$modId"
base.archivesName = "$modId-neoforge-${libs.versions.minecraft.get()}"

java.toolchain.languageVersion = JavaLanguageVersion.of(21)
sourceSets.main.get().resources { srcDir("src/generated/resources") }
tasks.withType<JavaCompile>().configureEach { options.encoding = "UTF-8" }

neoForge {

    version = libs.versions.neoForge.get()

    parchment {
        minecraftVersion = libs.versions.minecraft.get()
        mappingsVersion = libs.versions.parchment.get()
    }

    mods {
        create(modId) {
            sourceSet(sourceSets.main.get())
        }
    }

    runs {
        create("client", Action<RunModel> {
            client()
        })
        create("server", Action<RunModel> {
            server()
        })
        create("data", Action<RunModel> {
            data()
            programArguments.addAll(
                "--mod", modId, "--all",
                "--output", file("src/generated/resources/").absolutePath,
                "--existing", file("src/main/resources/").absolutePath,
            )
        })
        configureEach {
            systemProperty("forge.logging.markers", "REGISTRIES")
            systemProperty("terminal.ansi", "true")

            val hotSwapAgentPath =
                "D:/Packages/gradle/jdks/jetbrains_s_r_o_-21-amd64-windows.2/lib/hotswap/hotswap-agent-core.jar"
            if (file(hotSwapAgentPath).exists()) {
                jvmArgument("-XX:+AllowEnhancedClassRedefinition")
                jvmArgument("-XX:HotswapAgent=external")
                jvmArgument("-javaagent:$hotSwapAgentPath=disablePlugin=log4j2,disablePlugin=proxy")
            }
            logLevel = Level.DEBUG
        }
    }

}

val localRuntime by configurations.creating
configurations {
    runtimeClasspath.get().extendsFrom(localRuntime)
}

repositories {
    mavenLocal() // Blueberry Lib
    maven("https://thedarkcolour.github.io/KotlinForForge") // Kotlin for Forge
    maven("https://maven.terraformersmc.com") // EMI
    maven("https://maven.firstdark.dev/snapshots") // LowDragLib 2
    maven("https://modmaven.dev") // Mekanism
}

dependencies {
    implementation(libs.emi.neoForge)
    implementation(libs.blueberryLib) { isTransitive = false }
    implementation(libs.lowDragLib2) { isTransitive = false }
    implementation(libs.mekanism)
    compileOnly(libs.yoga)
    compileOnly(libs.kotlinx.coroutines)
    compileOnly(project(":stub"))
}

val generateModMetadata = tasks.register<ProcessResources>("generateModMetadata") {
    val replaceProperties = mapOf(
        "minecraft_version" to libs.versions.minecraft.get(),
        "minecraft_version_range" to "[${libs.versions.minecraft.get()},)",
        "neo_version" to "[21.1,)",
        "loader_version_range" to "[4,)",
        "mod_id" to modId,
        "mod_name" to modName,
        "mod_license" to "MIT",
        "mod_version" to version,
        "mod_authors" to "ConcerroX",
        "mod_description" to "Lorem ipsum dolor amet. ",
        "blueberry_version_range" to "[${libs.blueberryLib.get().version},)",
    )
    inputs.properties(replaceProperties)
    expand(replaceProperties)
    from("src/main/templates")
    into("build/generated/sources/modMetadata")
    filesMatching("$modId.mixins.json") { expand(replaceProperties) }
}

sourceSets.main.get().resources.srcDir(generateModMetadata)
neoForge.ideSyncTask(generateModMetadata)

idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}

configure<PublishingExtension> {
    publications {
        register<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = modId
        }
    }
    repositories {
        maven {
            url = uri(File(project.projectDir, "repo"))
        }
    }
}

tasks.build {
    dependsOn(tasks.publishToMavenLocal)
}