# Changelog

## [2.0.0] - 2026-06-10

### Added
- Collapsible item grouping in EMI sidebar (3-stacked icon, +/- badge, expand/collapse)
- JSON config per group: `item:`, `fluid:`, `#item:`, `#fluid:` selectors
- Per-group border color (`color`: `#RRGGBB` or `#AARRGGBB`)
- Show Group Border / Show Group Fill display toggles with EMI config integration
- Keybinding: Alt+LeftClick collapses group, configurable in EMI settings
- In-game group editor: RecipeScreen-style UI, add by ID/Tag, selection, Delete key
- Group creation/edit dialog with ICU4J transliteration and suggestion hints
- 12-selector sub-pagination per group (EmiIngredientRecipe pattern)
- Drag-and-drop to favorites with grouped members
- Fluid support + generic type support via EmiIngredientSerializer
- Modrinth and CurseForge publish plugin integration

### Fixed
- Search: solo group unwrap, dedup reprocess to prevent member doubling
- IdentityHashMap for correct stack matching (avoids EmiStack's loose equals)
- Pagination: delete orphan files on save, use bakeOnly instead of reload
- BackgroundHeight drift: only compute on first init
- Del button overlap, page arrow UV, suggestion text behavior
- Expand in search results: sync expand state, flatten group icons to members

### Changed
- Tooltip cleanup: emiplusplus→emixx keys, white text, optional collapse hint toggle
- EMI is not an acronym — removed all expansions
- Mod and EMI dependency both set to CLIENT side
