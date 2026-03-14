# TipSign

A Fabric Minecraft Java mod that introduces a richly configurable **Tip Sign** block. Server operators, map makers, and content creators can embed long-form text and one-click supporter links (Ko-fi and Patreon) directly in the world — bridging in-game exploration with real-world community support.

## Features

- **Tip Sign block** — place a custom sign post in the world with a distinct visual style
- **Long-form text** — write multi-page content (up to 50 pages) with Minecraft formatting codes
- **Supporter links** — configure Ko-fi and Patreon buttons that open in the player's browser
- **Inline hyperlinks** — embed `[link text](url)` in body text with server-configurable domain whitelist
- **Owner permissions** — only the sign owner (or ops) can edit; configurable break protection
- **Discovery API** — JSON snapshot file + webhook push for external website integration
- **Crafting recipe** — Gold Nugget + Any Plank + Sticks (toggleable via config)

## Version Support

TipSign targets **18 Minecraft versions** (1.20.1–1.21.11) via 7 compatibility bands:

| Band | Versions | Java |
|------|----------|------|
| A | 1.20.1–1.20.4 | 17 |
| B | 1.20.5–1.20.6 | 21 |
| C | 1.21–1.21.1 | 21 |
| D | 1.21.2–1.21.3 | 21 |
| E | 1.21.4–1.21.5 | 21 |
| F | 1.21.6–1.21.8 | 21 |
| G | 1.21.9–1.21.11 | 21 |

Each band produces a standalone JAR: `tipsign-<version>+mc<mcversion>.jar`

## Building

```bash
# Build all bands
./gradlew buildAll

# Build a specific band
./gradlew :versions:1.21:build

# Run client for development
./gradlew :versions:1.21:runClient
```

Java 17 for Band A, Java 21 for all others.

## Configuration

Server-side config at `config/tipsign.toml`:

| Option | Default | Description |
|--------|---------|-------------|
| `ownerOnlyBreak` | `true` | Only owner/ops can break a placed sign |
| `maxPages` | `10` | Max pages per sign (1–50) |
| `craftingEnabled` | `true` | Enable/disable crafting recipe |
| `allowInlineLinks` | `true` | Allow clickable hyperlinks in body text |
| `allowedLinkDomains` | `["ko-fi.com", "patreon.com"]` | Domain whitelist for all links |
| `discoveryEnabled` | `true` | Enable JSON snapshot + webhook output |
| `webhookUrl` | `""` | Webhook endpoint for push notifications |

## Architecture

Multi-project Gradle build with a **VersionAdapter** pattern:

- `common/` — pure Java library (config, data models, webhook, URL validation)
- `shared-mc/` — Fabric mod core (block, screens, BER, discovery manager)
- `versions/` — one subproject per band, each providing a `VersionAdapterImpl`

Uses **Mojang Mappings** for smooth migration to post-1.21.11 unobfuscated releases.

## Documentation

- [Product Requirements (PRD)](docs/PRD.md)
- [Technical Design (TDD)](docs/TDD.md)
- [Fabric Modding Reference](docs/MOD-REFERENCE.md)

## License

TBD
