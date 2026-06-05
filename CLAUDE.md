# EMI++ (emi-plus-plus-v2)

> Minecraft NeoForge mod, Kotlin, built with ModDevGradle.

## Project Identity

| Key          | Value                                                     |
|--------------|-----------------------------------------------------------|
| Mod Name     | **EMI++**                                                 |
| Mod ID       | `emixx`                                                   |
| Version      | `2.0.0`                                                   |
| Author       | **ConcerroX**                                             |
| Minecraft    | `1.21.1`                                                  |
| NeoForge     | `21.1.90`                                                 |
| Language     | Kotlin `2.2.20`, Java `21`                                |
| Build System | Gradle + **ModDevGradle** (`net.neoforged.moddev:2.0.78`) |
| Mappings     | Yarn (via EMI transitive) / Mojmap                        |

## Dependencies

| Dependency           | Artifact                                | Version         | Purpose                              |
|----------------------|-----------------------------------------|-----------------|--------------------------------------|
| **EMI**              | `dev.emi:emi-neoforge`                  | `1.1.24+1.21.1` | Recipe/items viewer this mod extends |
| **KotlinForForge**   | `thedarkcolour:kotlinforforge-neoforge` | `5.10.0`        | Shared Kotlin runtime at runtime     |
| Kotlin Serialization | plugin                                  | `2.2.20`        | JSON config parsing                  |

Version catalog: `gradle/libs.versions.toml`

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

| Command               | What                    |
|-----------------------|-------------------------|
| `./gradlew build`     | Compile                 |
| `./gradlew runClient` | Launch Minecraft client |
| `./gradlew runServer` | Launch dedicated server |
| `./gradlew runData`   | Run data generators     |

## Repositories

- Maven Central
- KotlinForForge (`thedarkcolour.github.io/KotlinForForge`)
- CurseMaven (`cursemaven.com`)
- Modrinth (`api.modrinth.com/maven`)
- TerraformersMC (`maven.terraformersmc.com/releases`)

## Feature Plan

### Collapsible Item Grouping

Items in EMI sidebar are grouped into collapsible categories.
Groups are defined by JSON config files (one per group).

**Config format** (see Sample Config below):

- `name` — display name
- `id` — namespaced group id (e.g. `minecraft:boats`)
- `description` — tooltip text
- `includes` — list of selectors: `item:namespace:id` or `#item:namespace:tag`

**UI behavior**:

- Group icon: 3 stacked items, click to expand
- Expanded: all group members shown inline in grid
- Click icon again to collapse

**Config loading**: JSON files in `config/emi-plus-plus/groups/` directory

# Sample Config

```json
{
  "name": "Boats",
  "id": "minecraft:boats",
  "description": "Boats are a type of transportation that can be used to travel across water. They can be crafted using wood planks and a wooden shovel.",
  "includes": [
    "item:minecraft:boat",
    "item:minecraft:chest_boat",
    "#item:minecraft:planks"
  ]
}
```