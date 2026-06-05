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
src/main/kotlin/concerrox/minecraft/emiplusplus/
├── EmiPlusPlus.kt                         # @Mod entrypoint
├── EmiPlusPlusPlugin.kt                   # @EmiEntrypoint, screen bounds
├── Identifier.kt                          # typealias + id() helper
├── config/
│   ├── EditorButtonEntry.kt               # Edit Groups button in EMI config
│   ├── EmiPlusPlusConfig.kt               # JSON-backed config
│   ├── EmiPlusPlusKeyMappings.kt          # collapseGroup EmiBind
│   └── GroupStateManager.kt              # per-group expand/collapse state
├── group/
│   ├── GroupConfig.kt                     # JSON data class (name, id, includes, color)
│   ├── GroupSelector.kt                   # IdSelector + TagSelector
│   ├── StackGroups.kt                     # singleton: load/bake/expand/collapse/saveAll
│   ├── GroupAssembler.kt                  # baked stack-to-group map + search()
│   ├── EmiGroupStack.kt                   # EmiStack: 3-stacked icon, border/fill
│   └── GroupedEmiStackWrapper.kt          # delegates to real EmiStack
├── editor/
│   ├── StackGroupEditorScreen.kt          # RecipeScreen-style group editor (render/input)
│   ├── EditorActions.kt                   # CRUD, ingredient creation, tag lookup
│   ├── EditorLayout.kt                    # bakePages, cardHeight, hit testing
│   ├── EditMode.kt                        # sealed class NONE/AddById/AddByTag
│   └── TagSelectionOverlay.kt            # card-style tag picker

src/main/java/concerrox/minecraft/emiplusplus/mixin/
├── ConfigScreenMixin.java                 # EMI++ config section
├── EmiReloadWorkerMixin.java              # trigger StackGroups.reload()
├── EmiScreenManagerMixin.java             # dirty-flag sync before render
├── EmiSidebarsMixin.java                  # redirect INDEX to grouped list
├── ScreenSpaceMixin.java                  # border rendering (shadows widths[])
├── SearchWorkerMixin.java                 # inject group headers into search
└── StackInteractionMixin.java             # click: toggle groups, collapse keybind, drag unwrap
```

## Build & Run

| Command               | What                    |
|-----------------------|-------------------------|
| `./gradlew build`     | Compile                 |
| `./gradlew runClient` | Launch Minecraft client |

## Repositories

- Maven Central
- KotlinForForge (`thedarkcolour.github.io/KotlinForForge`)
- CurseMaven (`cursemaven.com`)
- Modrinth (`api.modrinth.com/maven`)
- TerraformersMC (`maven.terraformersmc.com/releases`)

## Features (All Complete)

- [x] Collapsible item grouping (3-stacked icon, +/- badge, expand/collapse)
- [x] JSON config per group: `item:`, `fluid:`, `#item:`, `#fluid:` selectors
- [x] Per-group border color (`color`: `#RRGGBB` or `#AARRGGBB`)
- [x] Show Group Border / Show Group Fill display toggles
- [x] EMI config screen integration (EMI++ section with toggles, keybind, edit button)
- [x] Keybinding: Alt+LeftClick collapses group, configurable in EMI settings
- [x] In-game group editor: RecipeScreen-style UI, add by ID/Tag, selection, Delete key
- [x] Drag-and-drop to favorites works with grouped members
- [x] Fluid support + generic type support via EmiIngredientSerializer
- [x] Performance: dirty flag, shadowed widths, fast-path hasGroupStacks
- [x] 12-selector sub-pagination per group (EmiIngredientRecipe pattern)
- [x] Editor: 220×310 centered panel, card-style with 9-patch backgrounds
- [x] Select group → expand in EMI sidebar, auto-select new groups
- [x] 8 Mixins total: render, click, drag, search, config, reload, sidebar, screen space

## Editor Architecture

| File | Responsibility |
|------|---------------|
| `StackGroupEditorScreen.kt` | Screen lifecycle, render, input handling, widget building |
| `EditorActions.kt` | CRUD operations, ingredient creation, tag lookup, add mode handlers |
| `EditorLayout.kt` | bakePages (RecipeTab-style), cardHeight, findGroupAtPos |
| `TagSelectionOverlay.kt` | Card-style popup for tag selection with 9-patch background |
| `EditMode.kt` | NONE / AddById(groupId) / AddByTag(groupId) sealed class |

## Coding Style

- **No semicolons** in Kotlin — never use `;` to put multiple statements on one line. Each statement on its own line.
- **No fully-qualified class names** in code body — always import at the top of the file
- **No unnecessary defensive code** — remove empty try-catch blocks, redundant guards
- Single-letter variable names only for common abbreviations: `i`/`idx`, `k`/`v`, `x`/`y`
- Prefer method references over lambdas when possible
- Use `@Unique` on all non-override methods added to Mixin classes
- `Identifier` = typealias for `ResourceLocation`; `id("path")` for mod-namespaced IDs
- Config directory: `config/emixx/`, not `config/emi-plus-plus/`
- EMI is not an acronym — never expand it
- Always run `./gradlew build` before committing
- Run `./gradlew runClient` and let user confirm before pushing

## Sample Config

```json
{
    "name": "Boats",
    "id": "minecraft:boats",
    "description": "Boats are a type of transportation...",
    "includes": [
        "#item:minecraft:boats"
    ],
    "color": "#66FFAA00"
}
```
