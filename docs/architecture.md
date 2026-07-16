# Materia Reborn Architecture

Materia Reborn separates gameplay, persistence, presentation and optional integrations
so individual systems can evolve without turning the table into one large class.

## Main Areas

- `api`: Stable value, identity, storage, knowledge and table contracts.
- `registry`: NeoForge registrations for blocks, items, fluids, effects, menus and particles.
- `block` and `blockentity`: Materia Table, Essence Ore, Liquid Essence and persistent inventories.
- `menu` and `client`: Server-authoritative container behavior and the client interface.
- `essence`: Item catalog loading, player Essence and permanent analysis knowledge.
- `progression`: Player-owned slots, upgrade levels, settings and filter state.
- `ritual`: Table upgrade validation, preparation, animation and state transfer.
- `fluid` and `event`: Essence creation, exposure effects, Soulbound and interaction handlers.
- `config`: Common TOML settings and editable JSON item catalogs.
- `compat`: Optional JEI and Just Enough Resources integrations.

## Persistence

Materia Table inventories and active table state are stored in the block entity. Essence,
knowledge, purchased slots, upgrades and selected settings are stored per player. Preserved
table items use block-entity data so supported inventory state can survive breaking and
replacement.

## Server Authority

Purchases, Essence changes, crafting actions, smelting, item collection, rituals and XP
claims are validated on the logical server. The menu synchronizes the state required by
the client interface.

## Data And Configuration

Recipes, loot tables, tags, world generation and visual assets live under
`src/main/resources`. Runtime balance settings are generated under
`config/materia_reborn`; item catalog files are kept separate by Materia Table level.

## Optional Integrations

JEI displays custom Essence creation recipes. Just Enough Resources displays the custom
ore distribution. Neither dependency is required to run the mod.
