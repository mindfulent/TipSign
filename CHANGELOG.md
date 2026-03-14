# Changelog

All notable changes to TipSign will be documented in this file.

## [0.15.0] - 2026-03-14

### Fixed
- **BER title word-wrapping** — Long titles (e.g. "Build by slashdaemon") no longer overflow the sign board edges. Uses `Font.split()` to wrap text across multiple centered lines within the board face.
- **Delete confirmation text wrapping** — Author screen's "delete all" confirmation message now word-wraps within the panel instead of overflowing on a single line.

---

## [0.14.0] - 2026-03-14

### Fixed
- **BER title not updating** — In-world sign always showed "Notice" instead of custom title. Root cause: `getUpdateTag()` sync tags lack `Id` field, causing all VersionAdapters to skip client-side data load. `loadAdditional()`/`load()` now handles lightweight sync tags directly before delegating to VersionAdapter.
- **Responsive reader screen** — Reader panel was fixed at 220px height with wasted blank space. Now dynamically sizes based on content (text lines, supporter buttons, page nav), clamped between 80–300px. Close button placed inside panel bounds.

### Added
- **In-world block color tinting** — Board faces now tint to match the selected color theme using Minecraft's `BlockColorProvider` + `tintindex` system. Dedicated `BLOCK_TINT_PRESETS` calibrated for multiplicative blending against oak_planks texture.

---

## [0.13.0] - 2026-03-14

### Added
- **Wall-mounted placement** — Signs placed on the side of a block now mount flush against the wall without the vertical post or cap. Standing post form preserved when placed on ground.
- **Background color themes** — 8 wood/stone theme presets (Oak, Dark Oak, Spruce, Birch, Crimson, Warped, Stone, Obsidian) selectable via "Theme" button in the editor. Affects both reader and editor panel backgrounds.
- **Default title uses player name** — Newly placed signs default to "Build by <username>" instead of "Notice".

### Changed
- **Wider supporter buttons** — Ko-fi and Patreon buttons increased from 80px to 120px to prevent text truncation.
- **Larger body text field** — Editor body input increased from 1 line to 3 lines (40px) for easier editing.
- **In-world text color** — BER title text changed from light cream (0xFFEEDDCC) to dark brown (0xFF1A1008) for better readability.
- **Delete confirmation fits panel** — Confirmation message now renders within the panel bounds instead of overflowing.
- **Max title length** — Increased from 32 to 48 characters to accommodate "Build by" prefix with longer usernames.
- **Panel height** — Editor panel increased from 260px to 290px to accommodate new theme button and taller body field.

---

## [0.12.0] - 2026-03-14

### Fixed
- Server crash on startup: VersionAdapterImpl referenced client-only classes (Screen, ClientPlayNetworking) directly, causing NoClassDefFoundError on dedicated servers
- Extracted client networking into separate ClientNetworkHandler class to isolate client imports from ServiceLoader class scanning

## [0.11.0] - 2026-03-14

### Fixed
- Band B: ResourceLocation.fromNamespaceAndPath() → new ResourceLocation() (1.20.5 API)
- Shared-mc: Use VersionAdapter.createId() instead of direct ResourceLocation factory
- Shared-mc: Use FabricLoader.getConfigDir() for discovery init (works across all MC versions)
- Shared-mc: DirectionProperty → EnumProperty<Direction> (removed in 1.21.2)
- Shared-mc: BlockEntityType.Builder → FabricBlockEntityTypeBuilder (removed in 1.21.2)
- Bands F and G excluded from build — require Fabric Loom 1.10+ (stub subprojects remain)

### Changed
- buildAll now produces 5 JARs (Bands A–E); Bands F/G deferred until Loom upgrade
- Build requires `--no-parallel` or `GRADLE_OPTS="-Xmx4g"` to avoid OOM on remapSourcesJar

## [0.10.0] - 2026-03-14

### Added
- Band B (MC 1.20.5–1.20.6) version subproject with DataComponentType, CustomPayload networking
- Band D (MC 1.21.2–1.21.3) version subproject with unified ActionResult support
- Band E (MC 1.21.4–1.21.5) version subproject with item model definition file
- Band F (MC 1.21.6–1.21.8) version subproject (stub — needs ValueOutput/ValueInput adaptation)
- Band G (MC 1.21.9–1.21.11) version subproject (stub — needs ValueOutput/ValueInput + queue-based BER)
- Item model definition (`assets/tipsign/items/sign_post.json`) for Bands E, F, G (1.21.4+)
- All 7 version bands now included in settings.gradle and buildAll task

## [0.9.0] - 2026-03-14

### Changed
- Custom sign-post block model with vertical post, mounted board, and decorative cap
- Uses stripped_oak_log for post and oak_planks for board (placeholder textures)
- BER title text repositioned and rescaled for new board geometry
- VoxelShape updated to match sign post silhouette
- Blockstate rotations for all 4 facings

## [0.8.0] - 2026-03-14

### Added
- DiscoveryManager — bridges block entity events to snapshot writes and webhook pushes
- DebounceTimer — 5-second debounce window for rapid edits
- Background interval timer (ScheduledExecutorService) for periodic snapshots
- Server ID auto-generation (UUID in tipsign.toml)
- Wired SnapshotWriter + WebhookDispatcher to live block entity data
- Graceful executor shutdown on server stop
- Sign tracking registry for efficient snapshot collection

## [0.7.0] - 2026-03-14

### Added
- Custom DataComponentType (tipsign:sign_data) for item stack data persistence
- Item data serialization: breaking serializes all data into dropped item, re-placing restores it
- Item tooltip shows sign title via custom_name component
- TipSignPermissions — owner check, op-level-2 admin
- TipSignBlockEntityRenderer — renders title text on block face, rotated per facing
- Shaped crafting recipe: Gold Nugget + Plank + Sticks
- Loot table with copy_components for data preservation
- Axe mineable tag
- Owner-only break protection (configurable via ownerOnlyBreak)

## [0.6.0] - 2026-03-14

### Added
- Full Reader screen with weathered-plank background, bold title, separator, page navigation
- Ko-fi (#FF5E5B) and Patreon (#FF424D) supporter buttons in Reader with browser confirmation
- Full Author screen with title, body, Ko-fi/Patreon URL fields, validation, formatting reference
- Multi-page editor with add/delete page, config-aware maxPages limit
- Delete All Content with two-step confirmation flow
- Inline [link](url) parsing in Reader (rendered as plain text for now)
- URL validation for Ko-fi/Patreon inputs in Author screen

## [0.5.0] - 2026-03-14

### Added
- Custom networking: OpenSignPayload (S2C) and UpdateSignPayload (C2S)
- TipSignReaderScreen — minimal reader with page navigation and close button
- TipSignAuthorScreen — minimal editor with title field, body field, page nav, save/cancel
- TipSignDataCodec — JSON serializer for networking payloads
- Client packet receiver opens appropriate screen based on authorMode flag
- Server packet receiver validates ownership before applying updates
- Right-click opens Reader, shift+right-click by owner opens Author

## [0.4.0] - 2026-03-14

### Added
- TipSignBlock — BaseEntityBlock with FACING property, placement stores owner UUID
- TipSignBlockEntity — holds TipSignData, delegates serialization to VersionAdapter
- Block/item/block entity registration in TipSignMod
- Band C VersionAdapterImpl with CompoundTag serialization (save/load)
- Client sync via getUpdatePacket/getUpdateTag for title rendering
- Blockstate, block model (placeholder oak_planks), item model, en_us.json lang
- Creative tab entry in Functional Blocks

## [0.3.0] - 2026-03-14

### Added
- TipSignConfig — NightConfig TOML parser with all config fields, defaults, and validation
- UrlValidator — scheme/domain whitelist, Ko-fi/Patreon URL construction from usernames
- LinkParser — `[text](url)` markdown parser with link extraction and validation
- TipSignSnapshot — JSON model matching US-702 schema with Gson serialization
- SnapshotWriter — atomic file write (tmp + rename) for discovery snapshots
- WebhookDispatcher — async HTTP POST with HMAC-SHA256 signing and 3-retry exponential backoff

## [0.2.0] - 2026-03-14

### Added
- Gradle multi-project build infrastructure (common, shared-mc, versions/1.21)
- Root build.gradle with `buildAll` task
- Common module with TipSignData record and NightConfig/Gson dependencies
- Shared-mc module with TipSignMod/TipSignModClient stubs and VersionAdapter interface
- Band C (MC 1.21) version subproject with VersionAdapterImpl and ServiceLoader
- fabric.mod.json, mixin config, .gitignore

## [0.1.0] - 2026-03-14

### Added
- Project initialized with design documentation
- Product Requirements Document (PRD v0.1)
- Technical Design Document (TDD v0.2)
- Fabric Modding Reference covering all 18 target versions (1.20.1–1.21.11)
- README with project overview, build instructions, and configuration reference
- CLAUDE.md with development workflow and architecture guide
