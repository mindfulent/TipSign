# PRD: TipSign Mod — Minecraft Java Edition

**Document Type:** Product Requirements Document
**Format:** User Stories & Acceptance Criteria
**Status:** Draft v0.1
**Author:** Slash
**Mod Name:** TipSign
**Mod ID:** `tipsign`

---

## Overview

TipSign is a Fabric/Forge Minecraft Java mod that introduces a richly configurable **Tip Sign** block. It allows server operators, map makers, and content creators to embed long-form text and one-click supporter links (Ko-fi and/or Patreon) directly in the world — bridging in-game exploration with real-world community support.

---

## Epics

| ID | Epic |
|----|------|
| E1 | Block Placement & World Interaction |
| E2 | Long-Form Text Authoring |
| E3 | Supporter Link Configuration |
| E4 | Sign Post UI (Read View) |
| E5 | Permissions & Access Control |
| E6 | Persistence & Data Integrity |
| E7 | External Discovery API |

---

## User Stories & Acceptance Criteria

---

### E1 — Block Placement & World Interaction

---

#### US-101 — Place a Tip Sign

> **As a** player with build permissions,
> **I want to** place a Tip Sign block in the world,
> **so that** I can configure it to share content and supporter links with other players.

**Acceptance Criteria:**

- A new block item called `tipsign:sign_post` exists in the game and can be obtained via creative inventory or a crafting recipe.
- The block can be placed on any solid surface (top, sides) following standard Minecraft block placement rules.
- The block renders as a visually distinct sign post (custom model) that is recognizable as different from a vanilla sign.
- Placing the block immediately opens the **Author UI** (edit mode) for the placing player.
- The block stores the UUID of the player who placed it as its **owner**.

---

#### US-102 — Interact With a Placed Tip Sign

> **As a** player,
> **I want to** right-click an existing Tip Sign,
> **so that** I can read its contents and access supporter links.

**Acceptance Criteria:**

- Right-clicking the block opens the **Reader UI** for any player.
- The Reader UI is read-only for non-owners by default.
- Shift + right-click by the **owner** opens the **Author UI** (edit mode) instead.
- The block does not require the player to hold any specific item to interact with it.

---

#### US-103 — Craft a Tip Sign

> **As a** player in survival mode,
> **I want to** craft a Tip Sign from accessible materials,
> **so that** obtaining one feels intentional and meaningful.

**Acceptance Criteria:**

- The Tip Sign is craftable via a shaped recipe in a standard crafting table.
- Recipe shape:
  ```
  [ Gold Nugget ] [ Any Plank ] [ Gold Nugget ]
  [             ] [   Stick   ] [             ]
  [             ] [   Stick   ] [             ]
  ```
- The `Any Plank` slot accepts any plank type via the vanilla `minecraft:planks` item tag — oak, birch, spruce, jungle, acacia, dark oak, mangrove, bamboo, cherry, or any modded plank that registers to the tag.
- The recipe yields **1 Tip Sign**.
- The recipe is registered in the mod's `data/tipsign/recipes/` directory and is visible in REI/JEI recipe viewers.
- The recipe can be disabled via a `craftingEnabled` boolean in `tipsign.toml` (default: `true`); when disabled, the item is obtainable only via `/give`.

---

### E2 — Long-Form Text Authoring

---

#### US-201 — Write Multi-Page Text Content

> **As a** Tip Sign owner,
> **I want to** write long-form text across multiple pages,
> **so that** I can share detailed information, lore, announcements, or instructions with players.

**Acceptance Criteria:**

- The Author UI presents a book-style text editor with a minimum of **10 pages**.
- Each page supports at minimum **14 lines** of text.
- Each line supports at minimum **80 characters**.
- The author can navigate between pages using **Next** and **Previous** buttons.
- New pages can be added up to the maximum page limit.
- Pages can be deleted (with a confirmation prompt) as long as at least one page remains.

---

#### US-202 — Format Text

> **As a** Tip Sign owner,
> **I want to** apply basic text formatting,
> **so that** my sign content is visually organized and readable.

**Acceptance Criteria:**

- The author can apply Minecraft-standard color codes (`§` + color code) to text.
- The author can apply bold, italic, underline, and strikethrough formatting.
- A formatting toolbar or reference panel is visible within the Author UI showing available codes.
- Formatted text renders correctly in both the Author UI preview and the Reader UI.

---

#### US-203 — Set a Title for the Tip Sign

> **As a** Tip Sign owner,
> **I want to** set a visible title for the sign,
> **so that** players can identify its purpose before opening it.

**Acceptance Criteria:**

- A dedicated **Title** field exists, separate from the body pages.
- The title renders on the sign block face in the world (visible without opening the UI).
- Title has a maximum length of **32 characters**.
- Title supports color codes.
- If no title is set, the block renders a default label such as `"Notice"`.

---

### E3 — Supporter Link Configuration

---

#### US-301 — Configure a Ko-fi Link

> **As a** Tip Sign owner,
> **I want to** link my Ko-fi page to the sign,
> **so that** players can support me directly from within the game.

**Acceptance Criteria:**

- The Author UI contains a **Ko-fi** configuration field that accepts a Ko-fi username or full URL (e.g., `ko-fi.com/username`).
- Input is validated: the field must resolve to a well-formed `ko-fi.com/` URL before saving.
- If only a username is entered, the mod constructs the full URL automatically (`https://ko-fi.com/<username>`).
- An invalid or empty Ko-fi URL does not block saving the sign (the button simply does not appear in Reader UI).

---

#### US-302 — Configure a Patreon Link

> **As a** Tip Sign owner,
> **I want to** link my Patreon page to the sign,
> **so that** players can become patrons directly from within the game.

**Acceptance Criteria:**

- The Author UI contains a **Patreon** configuration field that accepts a Patreon username or full URL.
- Input is validated: must resolve to a well-formed `patreon.com/` URL before saving.
- If only a username is entered, the mod constructs the full URL automatically (`https://www.patreon.com/<username>`).
- An invalid or empty Patreon URL does not block saving (button simply does not appear in Reader UI).

---

#### US-303 — Display Supporter Buttons in Reader UI

> **As a** player reading a Tip Sign,
> **I want to** see clearly labeled Ko-fi and/or Patreon buttons,
> **so that** I can easily visit the creator's support page.

**Acceptance Criteria:**

- If a valid Ko-fi URL is configured, a **"Support on Ko-fi"** button is rendered in the Reader UI using Ko-fi brand colors (`#FF5E5B` background, white text).
- If a valid Patreon URL is configured, a **"Become a Patron"** button is rendered using Patreon brand colors (`#FF424D` background, white text).
- Buttons appear only for platforms that have a valid URL configured.
- Clicking a button opens the URL in the player's **default system browser** via `Util.getPlatform().openUri()` or equivalent Fabric API.
- A confirmation modal appears before opening the browser: *"This will open [url] in your browser. Continue?"* with **Yes / Cancel** options.
- Buttons are rendered at the bottom of the Reader UI, below the text content.

---

### E4 — Sign UI (Read View)

---

#### US-401 — Reader UI Layout

> **As a** player,
> **I want** the Reader UI to look and feel like a weathered wooden signpost,
> **so that** reading it feels immersive and grounded in the world.

**Acceptance Criteria:**

- The Reader UI renders a **signpost-style GUI** — a weathered wood plank texture background evoking a roadside post or trail marker, distinct from any book or parchment aesthetic.
- The UI background uses a worn, desaturated plank texture with visible wood grain and subtle aging detail (nail holes, faded paint edges).
- The title is displayed prominently at the top of the panel in a bold, carved or painted font style.
- Body text renders in a legible contrasting color (off-white or light cream) against the wood background.
- Pages are navigated via **Next / Previous** arrow buttons styled as carved wooden arrows or worn metal brackets, not book-style page turns.
- Page numbers are displayed (e.g., `Page 2 / 5`) in a small label at the bottom center of the panel.
- Supporter buttons appear anchored to the bottom of the UI on every page, styled to complement the wood aesthetic (e.g., slightly rounded, muted border).
- The UI can be closed with `Escape` or a dedicated **Close** button.
- The GUI width and height are sized to feel like a notice board — wider than tall — rather than a portrait book layout.

---

#### US-402 — Clickable URLs in Body Text

> **As a** Tip Sign owner,
> **I want to** embed hyperlinks in my text body,
> **so that** I can reference external resources beyond just Ko-fi and Patreon.

**Acceptance Criteria:**

- The Author UI supports a `[link text](url)` syntax for embedding inline hyperlinks.
- Inline links render as underlined, colored text in the Reader UI.
- Clicking an inline link triggers the same browser confirmation modal as the supporter buttons.
- Only `https://` URLs are accepted; `http://` and other schemes are rejected with an inline error.

---

### E5 — Permissions & Access Control

---

#### US-501 — Owner-Only Editing

> **As a** server operator,
> **I want** only the Tip Sign owner to be able to edit it,
> **so that** players cannot vandalize each other's content.

**Acceptance Criteria:**

- Only the player whose UUID matches the stored owner UUID can open the Author UI.
- Attempting to shift + right-click as a non-owner produces a chat message: *"You are not the owner of this Tip Sign."*
- The owner UUID is stored in the block's NBT/BlockEntityData and persists through server restarts and chunk unloads.

---

#### US-502 — Operator Override

> **As a** server operator,
> **I want** to be able to edit or remove any Tip Sign,
> **so that** I can moderate content on my server.

**Acceptance Criteria:**

- Players with **op-level 2 or above** (or the `tipsign.admin` permission node if a permission mod is present) can open the Author UI on any Tip Sign regardless of ownership.
- Breaking a Tip Sign as an op drops the item even if they are not the owner.
- Admin edits do not change the stored owner UUID.

---

#### US-503 — Sign Destruction

> **As a** Tip Sign owner,
> **I want to** be able to break and reclaim my sign without losing its data,
> **so that** I can move or reposition it freely.

**Acceptance Criteria:**

- The owner can break the block and receive the `tipsign:sign_post` item drop.
- All configured data (title, pages, Ko-fi URL, Patreon URL, owner UUID) is **serialized into the item's NBT** when the block is broken, and fully restored when the item is placed again.
- The item tooltip displays the sign's title (if set) so the owner can identify it in their inventory.
- Non-owners cannot break the sign (the block has hardness set to match obsidian-level resistance, or server-side break protection can be toggled via config).
- A config option `ownerOnlyBreak: true/false` allows server operators to toggle this protection.

---

#### US-504 — Explicit Data Deletion

> **As a** Tip Sign owner,
> **I want** a dedicated delete action within the Author UI,
> **so that** data destruction is always intentional and never accidental.

**Acceptance Criteria:**

- The Author UI contains a clearly labeled **"Delete All Content"** button, visually separated from editing controls (e.g., bottom corner, distinct destructive color).
- Clicking the button triggers a two-step confirmation modal: *"This will permanently delete all text, links, and settings on this Tip Sign. This cannot be undone. Continue?"* with **Delete / Cancel** options.
- Confirming the deletion clears all pages, title, Ko-fi URL, and Patreon URL, resetting the sign to a blank state.
- The owner UUID is **not** cleared by this action — ownership is preserved.
- Breaking and picking up a Tip Sign is **never** destructive to data under any circumstances — the only path to data loss is explicit confirmation through this flow.
- Server ops with `tipsign.admin` can also trigger this deletion flow on any sign they do not own.

---

### E6 — Persistence & Data Integrity

---

#### US-601 — Data Persistence Across Sessions

> **As a** server operator,
> **I want** Tip Sign data to persist reliably in all scenarios,
> **so that** content is never lost unintentionally.

**Acceptance Criteria:**

- All Tip Sign data (title, pages, Ko-fi URL, Patreon URL, owner UUID) is stored in a `BlockEntity` and serialized to NBT while placed in the world.
- Data survives server restarts, chunk unloads/reloads, and `/reload`.
- When a Tip Sign is broken by its owner, all data is written into the dropped item's NBT and restored in full when the item is placed again.
- Data survives the block being pushed by a piston (sign moves with data intact, or is destroyed and drops the item with data preserved if unmovable is configured).
- The **only** way data can be permanently destroyed is via the explicit "Delete All Content" confirmation flow in the Author UI (see US-504).
- No server event, crash, chunk error, or block interaction outside of US-504 should result in data loss; any such occurrence is considered a bug.

---

#### US-602 — Config File

> **As a** server operator,
> **I want** a server-side config file,
> **so that** I can tune Tip Sign behavior for my server.

**Acceptance Criteria:**

- A `tipsign.toml` config file is generated in the `config/` directory on first launch.
- Config exposes the following options:
  - `ownerOnlyBreak` (boolean, default: `true`)
  - `requireConfirmBeforeBrowserOpen` (boolean, default: `true`)
  - `maxPages` (integer, default: `10`, min: `1`, max: `50`)
  - `allowedUrlSchemes` (list, default: `["https"]`)
  - `allowInlineLinks` (boolean, default: `true`) — toggles whether body text hyperlinks are clickable for all players
  - `allowedLinkDomains` (list, default: `["ko-fi.com", "patreon.com"]`) — whitelist of permitted domains for both supporter buttons and inline links; ops may add additional domains
- Invalid config values fall back to defaults with a logged warning.
- If `allowInlineLinks` is `false`, inline `[link text](url)` syntax still renders as styled text but is non-clickable, with a tooltip: *"Hyperlinks are disabled on this server."*
- URLs pointing to domains not in `allowedLinkDomains` are rejected at save time in the Author UI with an inline error message.

---

### E7 — External Discovery API

---

#### US-701 — Write Snapshot to Disk

> **As a** server operator,
> **I want** TipSign to write a JSON snapshot of all signs to disk,
> **so that** I have a reliable, always-current local record of sign data independent of any network dependency.

**Acceptance Criteria:**

- The mod writes a `tipsigns.json` file to the server's `config/tipsign/` directory.
- The file is written on every TipSign write event (place, save in Author UI, delete, break) and additionally on a configurable background interval.
- A `discoveryIntervalSeconds` integer in `tipsign.toml` controls the background write frequency (default: `300` / 5 min, min: `60`, max: `3600`).
- The file is written atomically — the mod writes to a `.tmp` file first then renames it, preventing partial reads by external processes.
- If the write fails (e.g. disk full), the mod logs a warning and retries on the next interval; it does not crash or affect gameplay.
- A `discoveryEnabled` boolean in `tipsign.toml` (default: `true`) allows operators to disable all discovery output entirely.

---

#### US-702 — JSON Snapshot Schema

> **As an** external application developer,
> **I want** the snapshot file to follow a well-defined schema,
> **so that** I can build reliable consumers without guessing at the data shape.

**Acceptance Criteria:**

- The root of `tipsigns.json` is an object with the following structure:
  ```json
  {
    "server_id": "string",
    "generated_at": "ISO-8601 timestamp",
    "signs": [
      {
        "id": "uuid-v4-string",
        "title": "string",
        "world": "minecraft:overworld | minecraft:the_nether | minecraft:the_end | <custom>",
        "x": 0,
        "y": 0,
        "z": 0,
        "owner_username": "string",
        "owner_uuid": "uuid-v4-string",
        "kofi_url": "string | null",
        "patreon_url": "string | null",
        "pages": ["string", "string"],
        "placed_at": "ISO-8601 timestamp",
        "last_edited_at": "ISO-8601 timestamp"
      }
    ]
  }
  ```
- `server_id` is a stable UUID generated once on first launch and persisted in `tipsign.toml`; it allows the relay to distinguish between multiple servers posting to the same endpoint.
- `pages` is an ordered array of strings, one entry per page, with Minecraft formatting codes intact.
- `kofi_url` and `patreon_url` are `null` if not configured.
- The schema version is not embedded in v1.0 but reserved for a future `schema_version` field.

---

#### US-703 — Webhook Push to External Relay

> **As a** server operator,
> **I want** TipSign to push the snapshot to a configurable webhook URL,
> **so that** my external website receives updates without polling the game server or requiring filesystem access.

**Acceptance Criteria:**

- A `webhookUrl` string in `tipsign.toml` (default: `""`, disabled) configures the push target.
- When set, the mod POSTs the full JSON snapshot payload (identical schema to US-702) to the webhook URL on every write event and on the background interval.
- The request sets headers `Content-Type: application/json` and `X-TipSign-Server-ID: <server_id>`.
- A `webhookSecret` string in `tipsign.toml` (default: `""`) optionally enables HMAC-SHA256 request signing — when set, the mod adds a `X-TipSign-Signature: sha256=<hmac>` header so the relay can verify authenticity.
- The webhook secret is never logged, even at debug level.
- The mod uses a fire-and-forget async dispatch for webhook pushes — delivery is non-blocking and never stalls the game server thread.
- The mod retries failed webhook deliveries up to **3 times** with exponential backoff (2s, 4s, 8s) before logging a warning and giving up until the next trigger.
- If `webhookUrl` is empty, webhook pushing is silently skipped; the file write still occurs normally.
- On first launch with a `webhookUrl` configured, the mod logs: `"TipSign webhook configured → <url>"`.

---

#### US-704 — Webhook Trigger Behavior

> **As a** server operator,
> **I want** to understand exactly when pushes fire,
> **so that** I can reason about data freshness on my website.

**Acceptance Criteria:**

- A webhook push (and file write) is triggered immediately on any of the following events:
  - A TipSign is placed
  - The Author UI is saved
  - A TipSign's content is deleted via the Delete All Content flow (US-504)
  - A TipSign is broken by its owner or an admin
- A background interval push fires independently of events on the `discoveryIntervalSeconds` cadence — this acts as a reconciliation heartbeat.
- Both triggers share the same code path and produce identical payloads — there is no distinction between an event-driven push and an interval push in the payload itself, only in timing.
- Rapid successive edits within a 5-second window are debounced into a single push to avoid flooding the relay during bulk authoring sessions.

---

## Appendix A — Relay API Contract

The relay is a lightweight service hosted on Digital Ocean App Platform. It receives pushes from the mod and serves sign data to the website frontend. This appendix defines the interface contract between the mod (producer) and the relay (consumer) — relay implementation is out of scope for this PRD.

### Inbound endpoint (mod → relay)

```
POST /api/tipsigns
Content-Type: application/json
X-TipSign-Server-ID: <server_id>
X-TipSign-Signature: sha256=<hmac>   (if webhookSecret is set)

Body: full tipsigns.json payload (see US-702 schema)
```

**Expected relay response:**
- `200 OK` or `204 No Content` — delivery acknowledged, mod considers push successful
- `401 Unauthorized` — signature mismatch or missing; mod logs warning, does not retry
- `5xx` — relay error; mod retries per US-703 backoff policy

### Outbound endpoint (relay → website)

```
GET /api/tipsigns
GET /api/tipsigns/:id
```

These endpoints are served by the relay to the website frontend. Schema matches US-702 per-sign object. Implementation, auth, and caching strategy are at the relay developer's discretion.

---

## Out of Scope (v1.0)

- Multiplayer co-authoring of a single Tip Sign
- Image embedding in the text body
- Analytics or click tracking on supporter links
- Custom sign skins/themes beyond the default
- Bedrock Edition support
- Embedded HTTP server on the Minecraft server (replaced by webhook push model)
- Relay API implementation (defined by contract in Appendix A, built separately on DO App Platform)
- Per-sign webhook filtering (all signs are always included in the full snapshot payload)

---

## Decisions Log

| # | Question | Decision | Notes |
|---|----------|----------|-------|
| OQ-1 | Crafting recipe materials | **Themed recipe: Any Plank + Stick + Gold Nugget** | Uses `minecraft:planks` tag for wood-type flexibility; gold nugget implies value/tipping; book removed as it belongs to the UI metaphor, not the physical object |
| OQ-2 | URL restrictions for body text hyperlinks | **Server-configurable whitelist** | Default whitelist: `ko-fi.com`, `patreon.com`; ops can extend via `tipsign.toml` |
| OQ-3 | Mod loader target | **Undecided — revisit** | Fabric 1.21.1 assumed for Block Academy; NeoForge/Architectury TBD |
| OQ-4 | Inline body text hyperlinks server toggle | **Yes — server config toggle** | `allowInlineLinks` boolean in `tipsign.toml`, default `true` |

---

## Open Questions

| # | Question | Owner |
|---|----------|-------|
| OQ-3 | Fabric vs. NeoForge target — or both via Architectury? | Slash |
