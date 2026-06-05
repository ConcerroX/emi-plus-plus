# EMI++ (emi-plus-plus-v2)

> Minecraft NeoForge mod, Kotlin, built with ModDevGradle.

## Project Identity

| Key | Value |
|---|---|
| Mod Name | **EMI++** |
| Mod ID | `emixx` |
| Version | `2.0.0` |
| Author | **ConcerroX** |
| Minecraft | `1.21.1` |
| NeoForge | `21.1.90` |
| Language | Kotlin `2.0.21`, Java `21` |
| Build System | Gradle + **ModDevGradle** (`net.neoforged.moddev:2.0.78`) |
| Mappings | Yarn (via EMI transitive) / Mojmap |

## Dependencies

- **EMI** (`dev.emi:emi-neoforge:1.1.18+1.21.1`) — the recipe/items viewer this mod extends
- `kotlin.plugin.serialization` — available for data formats

## Source Layout

```
src/main/kotlin/concerrox/minecraft/emiplusplus/EmiPlusPlus.kt   ← main mod class
src/main/resources/META-INF/neoforge.mods.toml
src/main/resources/pack.mcmeta
```

## Current State

- [x] Gradle scaffolding (ModDevGradle, Kotlin, NeoForge runs: client/server/data)
- [x] EMI dependency wired
- [x] `@Mod` entrypoint logging `"EMI++ initialized!"`
- [ ] **No gameplay features yet** — the mod is an empty shell

## Build & Run

| Command | What |
|---|---|
| `./gradlew build` | Compile |
| `./gradlew runClient` | Launch Minecraft client |
| `./gradlew runServer` | Launch dedicated server |
| `./gradlew runData` | Run data generators |

## Repositories

- CurseMaven (`cursemaven.com`)
- Modrinth (`api.modrinth.com/maven`)
- TerraformersMC (`maven.terraformersmc.com/releases`)

## Feature Plan

> *To be defined. EMI++ extends EMI — likely additions include new recipe categories, transfer handlers, or widget enhancements.*

