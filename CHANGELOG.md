# Changelog

All notable changes to TipSign will be documented in this file.

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
