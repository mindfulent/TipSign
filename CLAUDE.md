# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

TipSign is a Fabric-only Minecraft Java mod that adds a configurable sign-post block with long-form text, supporter links (Ko-fi / Patreon), and an external discovery API (JSON snapshot + webhook push). A single codebase targets **18 Minecraft versions** (1.20.1–1.21.11) via a multi-project Gradle build with version-band adapters.

Design docs: `docs/PRD.md` (product requirements), `docs/TDD.md` (technical design), `docs/MOD-REFERENCE.md` (Fabric API reference for all 18 versions).

## Commit Workflow

**CRITICAL: Before every commit, you MUST:**

1. **Version bump** — increment the version in `gradle.properties` (patch for fixes, minor for features)
2. **Update CHANGELOG.md** — add a new entry for the version with date and description of changes
3. **Include both files** in the commit alongside the actual changes

Never commit code changes without a corresponding version bump and changelog entry.

## Build Commands

```bash
# Build all version bands
./gradlew buildAll

# Build a specific band
./gradlew :versions:1.20.1:build

# Run client for a specific band
./gradlew :versions:1.21:runClient
```

Java 17 required for Band A (1.20.1–1.20.4), Java 21 for all others. Use Prism Launcher's bundled JDK:

```bash
# Band A (Java 17) — use system Java 17 or equivalent
# Band B+ (Java 21):
JAVA_HOME="/c/Users/slash/AppData/Roaming/PrismLauncher/java/java-runtime-delta" PATH="$JAVA_HOME/bin:$PATH" ./gradlew build
```

Output JARs: `tipsign-<version>+mc<mcversion>.jar`

## Architecture

### Multi-Project Structure

```
tipsign/
├── common/          # Pure Java library — no Minecraft imports
│                    # NightConfig (TOML config), Gson (JSON snapshot),
│                    # java.net.http (webhook), URL validation, link parsing
├── shared-mc/       # Fabric Loom mod core — block, block entity, screens,
│                    # discovery manager, BER. Codes against VersionAdapter interface.
├── versions/        # One subproject per compatibility band (A–G)
│   ├── 1.20.1/      # Band A: Java 17, legacy NBT items, FriendlyByteBuf networking
│   ├── 1.20.5/      # Band B: Item Components, CustomPayload + PacketCodec
│   ├── 1.21/        # Band C: Stable 1.21 baseline, singularized data paths
│   ├── 1.21.2/      # Band D: RegistryKeys, unified ActionResult, recipe rewrite
│   ├── 1.21.4/      # Band E: Item model definitions, Optional NBT getters
│   ├── 1.21.6/      # Band F: ValueOutput/ValueInput serialization
│   └── 1.21.9/      # Band G: Queue-based BER, final obfuscated series
└── data/            # Shared data packs (recipes, loot tables, tags)
```

Dependency flow: `common` ← `shared-mc` ← `versions/*`

Each version band provides a `VersionAdapterImpl` loaded via `ServiceLoader` that handles all version-specific API calls (serialization, networking, item data, identifiers).

### Key Abstractions

- **VersionAdapter** — central interface isolating all version-specific Minecraft API calls. Each band implements this.
- **TipSignData** — plain Java record in `:common` (no MC deps): id, title, pages, kofiUrl, patreonUrl, ownerUuid, timestamps.
- **Client-only Screens** — Reader and Author UIs are `Screen` subclasses (not `AbstractContainerScreen`), no inventory slots. Data synced via custom packets (`OpenSignS2C`, `UpdateSignC2S`).
- **DiscoveryManager** — bridges block entity events to snapshot file writes and webhook pushes, with 5-second debounce.

### Version-Specific API Differences (Mojang Mapping names)

| Concern | Band A (1.20.1–1.20.4) | Band B–E (1.20.5–1.21.5) | Band F–G (1.21.6–1.21.11) |
|---------|----------------------|--------------------------|--------------------------|
| BlockEntity ser/deser | `saveAdditional(CompoundTag)` | `saveAdditional(CompoundTag, HolderLookup.Provider)` | `saveAdditional(ValueOutput)` |
| Item data | `BlockEntityTag` NBT | `DataComponentType` | `DataComponentType` |
| Networking | `FriendlyByteBuf` | `CustomPayload` + `PacketCodec` | same |
| Identifiers | `new ResourceLocation(...)` | `ResourceLocation.fromNamespaceAndPath(...)` | same |
| BER render | `render(be, ...)` with `MultiBufferSource` | `render(state, ...)` with `MultiBufferSource` (1.21.2+: EntityRenderState) | `render(state, ...)` with `OrderedRenderCommandQueue` (1.21.9+) |

### Mappings

Uses **Mojang Mappings** (`officialMojangMappings()`) — not Yarn. This ensures smooth migration to post-1.21.11 unobfuscated releases.

## Configuration

Server-side `config/tipsign.toml` parsed by NightConfig. Key options: `ownerOnlyBreak`, `maxPages`, `allowInlineLinks`, `allowedLinkDomains`, `craftingEnabled`, discovery/webhook settings.

## Package Structure

```
dev.blockacademy.tipsign.common.*     # :common module
dev.blockacademy.tipsign.*            # :shared-mc module (block/, screen/, render/, discovery/, compat/)
dev.blockacademy.tipsign.compat.v1_20_1.*  # Band-specific adapters
```
