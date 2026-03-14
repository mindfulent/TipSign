# Changelog

All notable changes to TipSign will be documented in this file.

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
