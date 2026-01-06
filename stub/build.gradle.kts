plugins {
    alias(libs.plugins.modDevGradle)
}

neoForge {
    version = libs.versions.neoForge.get()
    parchment {
        minecraftVersion = libs.versions.minecraft.get()
        mappingsVersion = libs.versions.parchment.get()
    }
}

repositories {
    maven("https://maven.terraformersmc.com") // EMI
}

dependencies {
    compileOnly(libs.emi.neoForge)
}