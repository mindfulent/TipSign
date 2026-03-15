# Changelog

All notable changes to TipSign will be documented in this file.

## [0.21.0] - 2026-03-14

### Fixed
- **Sign duplication bug** — Root cause: `useWithoutItem` returned `SUCCESS` on the client for ALL right-clicks, which short-circuited the interaction pipeline and prevented `BlockItem.useOn()` from running. The item was never consumed on placement, so the player kept the sign in their inventory after placing it. Fix: return `PASS` when the player is holding a TipSign item, allowing the `BlockItem` placement pipeline to consume the item normally. Also removed the `playerDestroy` no-op workaround (unnecessary with empty loot tables).

---

## [0.20.4] - 2026-03-14

### Fixed
- **Survival sign duplication bug (take 3)** — Override `playerDestroy()` as a no-op to completely block the `Block.dropResources()` → loot table drop path. Combined with the manual `popResource` in `playerWillDestroy()` and empty loot tables, this eliminates all possible duplicate drop sources. The sign is now dropped exclusively by our code in `playerWillDestroy`, before the block is removed.

---

## [0.20.3] - 2026-03-14

### Fixed
- **East/West text on wrong side** — BER text was rendering on the opposite face of the block when placed facing east or west. Root cause: `Axis.YP.rotationDegrees()` rotates counterclockwise (right-hand rule) while blockstate `"y"` rotates clockwise. For 0°/180° (north/south) both directions produce identical results, but 90°/270° (east/west) are opposite. Swapped EAST↔WEST rotation values in all 4 BER files (shared-mc, Band A, Band F, Band G) for both standing and wall signs, plus the "Right-click me!" indicator.

---

## [0.20.2] - 2026-03-14

### Fixed
- **Survival sign duplication bug (take 2)** — Emptied all 7 loot tables (`pools: []`) and restored the manual `Block.popResource()` in `playerWillDestroy()` as the sole drop source. The loot table was producing a second (blank) item alongside the manual data-bearing drop. Now only the manual drop fires, guaranteeing exactly one sign with preserved data.

---

## [0.20.0] - 2026-03-14

### Improved
- **Tight standing sign outline** — Replaced the near-full-cube VoxelShape with a compound shape (`Shapes.or()`) matching the actual model geometry (post + cap + board). The block targeting outline now hugs the sign instead of rendering as a large black box. Per-facing board shapes rotate correctly for N/S/E/W. Wall sign shapes unchanged.
- **"Right-click me!" indicator** — Pulsing red text with ▼ arrow floats above the sign board only when the player's crosshair targets the block. Self-illuminating (fullBright), sign-facing rotation, smooth alpha pulse (~2s cycle). Supports standing and wall signs across all 7 version bands.

---

## [0.19.0] - 2026-03-14

### Fixed
- **Author Screen fits small windows** — Panel height is now responsive instead of fixed at 290px. At Minecraft's default 854×480 window (GUI scale 2), all elements (title, body, nav, links, theme, save/cancel/delete) are now visible. Spacing compacts automatically when the window is small, and expands to full comfort when space allows.

---

## [0.18.0] - 2026-03-14

### Added
- **"Right-click me!" indicator** — Animated gold text with bobbing ▼ arrow renders above TipSign blocks, making interactivity obvious to new players. Billboard-rendered to always face the camera. Supports both standing and wall-mounted signs across all 7 version bands.

---

## [0.17.0] - 2026-03-14

### Fixed
- **BER text position and orientation** — Text was floating far from the board and facing wrong directions. Empirically calibrated via in-game debug keybinds: standing signs use blockstate-matching rotation with Z=-0.132/Y=0.646, wall signs use +180° rotation with Z=0.436/Y=0.490. Standing and wall signs now use separate rotation mappings.

---

## [0.16.0] - 2026-03-14

### Added
- **Band F (MC 1.21.6–1.21.8)** — Full version band with ValueOutput/ValueInput serialization. BER adapted for new Vec3 render parameter. Builds produce `tipsign-0.16.0+mc1.21.6.jar`.
- **Band G (MC 1.21.9–1.21.11)** — Full version band with submit-based BER (SubmitNodeCollector + TipSignRenderState), ValueOutput/ValueInput serialization, and authlib 7.x GameProfile record compatibility. Builds produce `tipsign-0.16.0+mc1.21.9.jar`.

### Changed
- **7 JARs** — `buildAll` now produces 7 version band JARs covering all 18 MC versions (1.20.1–1.21.11), up from 5 (1.20.1–1.21.5).
- **Gradle upgraded to 8.14.1** (was 8.10.2) — required for Loom 1.11 and MC 1.21.9+ builds.
- **Fabric Loom upgraded to 1.11-SNAPSHOT** (was 1.7-SNAPSHOT) — backwards-compatible with all existing bands.
- Band-specific source override pattern uses Gradle Copy tasks to filter shared-mc sources into build directory, avoiding Gradle exclude conflicts across source directories.

---

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
