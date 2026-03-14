# TDD: TipSign Mod — Technical Design Document

**Document Type:** Technical Design Document
**Status:** Draft v0.2
**Author:** Slash
**PRD Reference:** TipSign PRD v0.1
**Mod ID:** `tipsign`
**License:** TBD

---

## 1. Executive Summary

TipSign is a Fabric-only Minecraft Java mod that adds a configurable sign-post block with long-form text, supporter links (Ko-fi / Patreon), and an external discovery API (JSON snapshot + webhook). This TDD defines the multi-version project structure, compatibility strategy, component architecture, and data flow required to ship a single codebase across **18 Minecraft versions** spanning 1.20.1 through 1.21.11.

---

## 2. Version Compatibility Matrix

### 2.1 Target Versions

| Era | Versions | Java | Loom Plugin ID | Loom | Key Breaking Changes |
|-----|----------|------|----------------|------|---------------------|
| **1.20.1–1.20.4** | 1.20.1, 1.20.2, 1.20.3, 1.20.4 | 17 | `fabric-loom` | 1.5 | Baseline — NBT-only items, `FriendlyByteBuf` networking, `saveAdditional(CompoundTag)` / `load(CompoundTag)` on BlockEntity |
| **1.20.5–1.20.6** | 1.20.5, 1.20.6 | **21** | `fabric-loom` | 1.6 | **Item Components** replace NBT on stacks; `saveAdditional` / `loadAdditional` gain `HolderLookup.Provider` param; `CustomPayload` + `PacketCodec` networking; `ResourceLocation.fromNamespaceAndPath()` replaces constructor |
| **1.21.0–1.21.1** | 1.21, 1.21.1 | 21 | `fabric-loom` | 1.7 | `ResourceLocation.fromNamespaceAndPath()` is canonical; data pack paths singularized (`recipes/` → `recipe/`, `loot_tables/` → `loot_table/`); data-driven enchantments; vertex rendering overhaul |
| **1.21.2–1.21.3** | 1.21.2, 1.21.3 | 21 | `fabric-loom` | 1.8 | **RegistryKeys required** for block/item settings; `ActionResult` unified (replaces `TypedActionResult`/`ItemActionResult`); recipe system rewrite (server-side only); `EntityRenderState` for renderers; registry method mass renames |
| **1.21.4–1.21.5** | 1.21.4, 1.21.5 | 21 | `fabric-loom` | 1.10 | Item model definitions (`assets/<ns>/items/<item>.json`); `BlockRenderType.ENTITYBLOCK_ANIMATED` removed; `CompoundTag` getters return `Optional` (1.21.5); `HudRenderCallback` deprecated |
| **1.21.6–1.21.8** | 1.21.6, 1.21.7, 1.21.8 | 21 | `fabric-loom` | 1.10 | **BlockEntity `saveAdditional(ValueOutput)` / `loadAdditional(ValueInput)`** replaces CompoundTag serialization; rendering pipeline overhaul; several Fabric API modules removed |
| **1.21.9–1.21.11** | 1.21.9, 1.21.10, 1.21.11 | 21 | `net.fabricmc.fabric-loom-remap` | 1.14 | **Queue-based rendering** (`OrderedRenderCommandQueue` replaces `MultiBufferSource` in BER); keybinding overhaul; `Entity#level()` renamed to `Entity#getEntityWorld()`; last obfuscated versions; **Gradle 9.2+** required for 1.21.11 |

### 2.2 Version Grouping Strategy

Rather than maintaining 18 separate builds, we group versions into **compatibility bands** — sets of versions where the Minecraft API surface relevant to TipSign is identical or near-identical. Each band produces one JAR that declares compatibility with its member versions via `fabric.mod.json`.

| Band | Versions | Gradle Subproject | Notes |
|------|----------|-------------------|-------|
| **A** | 1.20.1, 1.20.2, 1.20.3, 1.20.4 | `versions/1.20.1` | Java 17, legacy NBT items, old networking |
| **B** | 1.20.5, 1.20.6 | `versions/1.20.5` | Java 21, item components, new networking |
| **C** | 1.21.0, 1.21.1 | `versions/1.21` | Stable 1.21 baseline, singularized data paths |
| **D** | 1.21.2, 1.21.3 | `versions/1.21.2` | RegistryKeys, unified ActionResult, recipe rewrite |
| **E** | 1.21.4, 1.21.5 | `versions/1.21.4` | Item model definitions, Optional NBT getters |
| **F** | 1.21.6, 1.21.7, 1.21.8 | `versions/1.21.6` | ValueOutput/ValueInput serialization |
| **G** | 1.21.9, 1.21.10, 1.21.11 | `versions/1.21.9` | Queue-based BER rendering, final obfuscated series |

Each band compiles against a single representative Minecraft version (the first in its range) and declares `"minecraft": ">=X.Y.Z"` compatibility for its siblings via `fabric.mod.json`.

---

## 3. Project Structure

### 3.1 Gradle Multi-Project Layout

```
tipsign/
├── build.gradle              # Root — shared plugin config, version catalog
├── settings.gradle           # Declares all subprojects
├── gradle.properties         # Shared properties (mod version, group, etc.)
│
├── common/                   # Version-independent logic (pure Java, no MC imports)
│   ├── build.gradle          # Java library, no Loom
│   └── src/main/java/
│       └── dev/blockacademy/tipsign/common/
│           ├── TipSignData.java            # POJO: title, pages, kofiUrl, patreonUrl, ownerUuid
│           ├── TipSignConfig.java          # Config POJO + TOML parser (NightConfig)
│           ├── TipSignSnapshot.java        # JSON snapshot model (E7)
│           ├── SnapshotWriter.java         # Atomic file write logic
│           ├── WebhookDispatcher.java      # Async HTTP POST + retry logic
│           ├── UrlValidator.java           # URL scheme/domain whitelist validation
│           └── LinkParser.java             # [text](url) markdown-style parser
│
├── shared-mc/                # Minecraft-dependent but version-stable code
│   ├── build.gradle          # Fabric Loom, compiles against a reference MC version
│   └── src/
│       ├── main/java/
│       │   └── dev/blockacademy/tipsign/
│       │       ├── TipSignMod.java                # Main entrypoint (common init)
│       │       ├── TipSignModClient.java           # Client entrypoint
│       │       ├── block/
│       │       │   ├── TipSignBlock.java           # Block behavior, interaction dispatch
│       │       │   └── TipSignBlockEntity.java     # Abstract — delegates ser/deser to compat layer
│       │       ├── screen/
│       │       │   └── TipSignDataSync.java        # Payload definitions (abstract)
│       │       ├── render/
│       │       │   ├── TipSignBlockEntityRenderer.java  # Title text on block face
│       │       │   ├── TipSignReaderScreen.java         # Reader UI (client)
│       │       │   └── TipSignAuthorScreen.java         # Author UI (client)
│       │       ├── discovery/
│       │       │   ├── DiscoveryManager.java       # Coordinates snapshot writes + webhook pushes
│       │       │   └── DebounceTimer.java          # 5-second debounce for rapid edits
│       │       └── compat/
│       │           └── VersionAdapter.java         # Interface — version-specific implementations
│       └── main/resources/
│           ├── fabric.mod.json
│           ├── tipsign.mixins.json
│           └── assets/tipsign/
│               ├── blockstates/sign_post.json
│               ├── models/block/sign_post.json
│               ├── models/item/sign_post.json
│               ├── items/sign_post.json            # Item model definition (Band E+)
│               └── textures/
│                   ├── block/sign_post.png
│                   ├── gui/reader_bg.png         # Weathered plank texture
│                   ├── gui/author_bg.png
│                   └── gui/buttons.png           # Ko-fi/Patreon button sprites
│
├── versions/
│   ├── 1.20.1/              # Band A adapter
│   │   ├── build.gradle     # Loom 1.5 targeting 1.20.1, depends on :common + :shared-mc
│   │   └── src/main/java/
│   │       └── dev/blockacademy/tipsign/compat/v1_20_1/
│   │           ├── VersionAdapterImpl.java      # Implements VersionAdapter
│   │           ├── BlockEntitySerializer.java   # saveAdditional(CompoundTag) / load(CompoundTag)
│   │           ├── ItemNbtBridge.java           # BlockEntityTag NBT on item stacks
│   │           ├── NetworkingBridge.java        # FriendlyByteBuf-based custom packets
│   │           └── RecipeBridge.java            # JSON recipe registration
│   │
│   ├── 1.20.5/              # Band B adapter
│   │   ├── build.gradle     # Loom 1.6
│   │   └── src/main/java/
│   │       └── dev/blockacademy/tipsign/compat/v1_20_5/
│   │           ├── VersionAdapterImpl.java
│   │           ├── BlockEntitySerializer.java   # saveAdditional(CompoundTag, HolderLookup.Provider)
│   │           ├── ItemComponentBridge.java     # DataComponentType for TipSign data on items
│   │           ├── NetworkingBridge.java        # CustomPayload + PacketCodec
│   │           └── RecipeBridge.java
│   │
│   ├── 1.21/                # Band C adapter (Loom 1.7)
│   ├── 1.21.2/              # Band D adapter (Loom 1.8)
│   ├── 1.21.4/              # Band E adapter (Loom 1.10)
│   ├── 1.21.6/              # Band F adapter (Loom 1.10)
│   │   └── .../
│   │       └── BlockEntitySerializer.java       # saveAdditional(ValueOutput) / loadAdditional(ValueInput)
│   │
│   └── 1.21.9/              # Band G adapter (Loom 1.14, fabric-loom-remap plugin)
│       └── .../
│           ├── BlockEntitySerializer.java       # Same as Band F (ValueOutput/ValueInput)
│           └── BlockEntityRendererBridge.java   # OrderedRenderCommandQueue-based BER
│
└── data/                     # Shared data packs (loot tables, recipes, tags)
    └── src/main/resources/
        └── data/tipsign/
            ├── recipe/sign_post.json             # Band C+: singularized path
            ├── loot_table/blocks/sign_post.json  # Band C+: singularized path
            └── tags/                             # Tag references for planks, etc.
```

**Note on data pack paths:** Minecraft 1.21 singularized data pack paths (`recipes/` → `recipe/`, `loot_tables/` → `loot_table/`, `advancements/` → `advancement/`). Band A and B use the old plural paths; Band C+ uses the new singular paths. Each version subproject overrides the shared data resources as needed.

### 3.2 Dependency Graph

```
┌──────────┐
│  common   │  Pure Java — no Minecraft dependency
│ (library) │  NightConfig, Gson, java.net.http
└─────┬─────┘
      │ implementation
      ▼
┌────────────┐
│  shared-mc  │  Fabric Loom — references MC classes via VersionAdapter interface
│  (mod core) │  Fabric API, Fabric Loader
└─────┬───────┘
      │ implementation
      ▼
┌───────────────────┐
│ versions/1.20.1   │  Band-specific adapter — provides VersionAdapterImpl
│ versions/1.20.5   │  Each subproject produces the final remapped JAR
│ versions/1.21     │  Includes :common and :shared-mc as jar-in-jar or shadow
│ ...               │
└───────────────────┘
```

### 3.3 Build Strategy

Each `versions/*` subproject is an independent Fabric mod build that:

1. Depends on `:common` (pure Java library, included via `include` for jar-in-jar)
2. Depends on `:shared-mc` (compiled against the band's MC version)
3. Provides its own `VersionAdapterImpl` loaded via Java `ServiceLoader`
4. Produces a standalone JAR: `tipsign-<modversion>+mc<mcversion>.jar`

The root `build.gradle` defines a `buildAll` task that invokes `remapJar` across all version subprojects.

---

## 4. Component Architecture

### 4.1 VersionAdapter Interface

This is the central abstraction that isolates all version-specific API calls. The `shared-mc` module codes against this interface; each band provides an implementation.

```java
public interface VersionAdapter {

    // --- Block Entity Serialization ---
    void saveBlockEntityData(TipSignBlockEntity be, Object tagOrView);
    void loadBlockEntityData(TipSignBlockEntity be, Object tagOrView);

    // --- Item Stack Data ---
    void writeToItemStack(ItemStack stack, TipSignData data);
    TipSignData readFromItemStack(ItemStack stack);
    void setItemTooltipTitle(ItemStack stack, String title);

    // --- Networking ---
    void registerPayloads();
    void sendSignDataToClient(ServerPlayer player, BlockPos pos, TipSignData data);
    void sendSignUpdateToServer(BlockPos pos, TipSignData data);

    // --- Recipe ---
    void registerRecipe();  // No-op if craftingEnabled=false

    // --- Screen Opening ---
    void openAuthorScreen(ServerPlayer player, TipSignBlockEntity be);
    void openReaderScreen(LocalPlayer player, TipSignData data, BlockPos pos);

    // --- Identifier Construction ---
    ResourceLocation createId(String namespace, String path);
}
```

### 4.2 Block & BlockEntity

**TipSignBlock** extends `BaseEntityBlock` and handles:

- Placement → stores owner UUID, opens Author UI
- Right-click → opens Reader UI (via networking: server sends data to client, client opens screen)
- Shift+right-click by owner → opens Author UI
- Break behavior → serializes data to dropped item (delegates to `VersionAdapter`)
- Hardness / blast resistance configurable for `ownerOnlyBreak` mode

**Important version note:** In 1.20.5+, `AbstractBlock` overridable methods (`useWithoutItem`, `useItemOn`, etc.) became `protected`. In 1.21.2+, `ActionResult` was unified — `TypedActionResult` and `ItemActionResult` were merged into a single `ActionResult`. The version adapters handle these signature differences.

**TipSignBlockEntity** holds the runtime `TipSignData` object and delegates serialization to the active `VersionAdapter`.

```
TipSignBlockEntity
├── TipSignData data          # title, pages[], kofiUrl, patreonUrl, ownerUuid, timestamps
├── setChanged()              # Triggers DiscoveryManager event
├── saveAdditional / load     # Delegates to VersionAdapter
├── getUpdatePacket()         # Client sync (title for BER rendering)
└── getUpdateTag()            # Initial chunk load sync
```

### 4.3 TipSignData Model

Defined in the `:common` module — a plain Java record with no Minecraft dependencies.

```java
public record TipSignData(
    String id,                // UUID v4 — unique per sign instance
    String title,             // Max 32 chars, nullable (defaults to "Notice")
    List<String> pages,       // Ordered, Minecraft formatting codes intact
    String kofiUrl,           // Nullable — full validated URL or null
    String patreonUrl,        // Nullable — full validated URL or null
    UUID ownerUuid,           // Placing player's UUID
    String ownerUsername,     // Resolved at placement time
    Instant placedAt,         // First placement timestamp
    Instant lastEditedAt      // Updated on every Author UI save
) {}
```

### 4.4 Serialization Compatibility Layer

The most significant API surface that changes across versions is BlockEntity serialization and item stack data storage. Here's how each band handles it:

#### Band A (1.20.1–1.20.4) — Legacy NBT

```java
// BlockEntity
@Override
protected void saveAdditional(CompoundTag tag) {
    super.saveAdditional(tag);
    tag.putString("Title", data.title());
    ListTag pages = new ListTag();
    for (String page : data.pages()) pages.add(StringTag.valueOf(page));
    tag.put("Pages", pages);
    if (data.kofiUrl() != null) tag.putString("KofiUrl", data.kofiUrl());
    if (data.patreonUrl() != null) tag.putString("PatreonUrl", data.patreonUrl());
    tag.putUUID("OwnerUuid", data.ownerUuid());
    // ... timestamps as ISO-8601 strings
}

// Item stack: BlockEntityTag compound
stack.getOrCreateTagElement("BlockEntityTag").merge(blockEntityTag);
```

#### Band B–E (1.20.5–1.21.5) — HolderLookup.Provider + Item Components

```java
// BlockEntity — same tag keys, new signature
@Override
protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.saveAdditional(tag, registries);
    // Same CompoundTag structure as Band A
}

@Override
protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.loadAdditional(tag, registries);
    // Same CompoundTag structure as Band A
}

// Item stack: Custom DataComponentType
// Register TIPSIGN_DATA component type during mod init
// Use DataComponentType.builder().persistent(CODEC).build()
```

**Band E note (1.21.5):** `CompoundTag` getter methods return `Optional<T>` instead of raw values. Use `tag.getStringOr("Title", "Notice")` pattern or handle `Optional` returns.

#### Band F (1.21.6–1.21.8) — ValueOutput / ValueInput

```java
@Override
protected void saveAdditional(ValueOutput view) {
    super.saveAdditional(view);
    view.putInt("SomeInt", someInt);
    view.putNullable("KofiUrl", Codec.STRING, data.kofiUrl());
    // ... using Codec-based serialization for complex fields
}

@Override
protected void loadAdditional(ValueInput view) {
    super.loadAdditional(view);
    String title = view.getIntOr("SomeInt", 0);
    view.read("KofiUrl", Codec.STRING).ifPresent(url -> this.kofiUrl = url);
    // ...
}
```

#### Band G (1.21.9–1.21.11) — Same as Band F

Band G uses the same `ValueOutput`/`ValueInput` serialization as Band F. The primary difference is the BER rendering API (see Section 4.7).

### 4.5 Networking Layer

TipSign uses custom packets for two flows:

| Packet | Direction | Purpose |
|--------|-----------|---------|
| `OpenSignS2C` | Server → Client | Sends full `TipSignData` + block pos to open Reader or Author screen |
| `UpdateSignC2S` | Client → Server | Author UI save: sends updated `TipSignData` to server |

#### Band A (1.20.1–1.20.4) — FriendlyByteBuf

```java
// Registration
ServerPlayNetworking.registerGlobalReceiver(UPDATE_SIGN_ID, (server, player, handler, buf, sender) -> {
    TipSignData data = TipSignData.fromBuf(buf);
    // ... validate ownership, apply update
});

// Sending
FriendlyByteBuf buf = PacketByteBufs.create();
data.writeToBuf(buf);
ServerPlayNetworking.send(player, OPEN_SIGN_ID, buf);
```

#### Band B+ (1.20.5+) — CustomPayload + PacketCodec

```java
public record OpenSignPayload(BlockPos pos, TipSignData data, boolean authorMode)
        implements CustomPayload {
    public static final CustomPayload.Id<OpenSignPayload> ID =
        new CustomPayload.Id<>(ResourceLocation.fromNamespaceAndPath("tipsign", "open_sign"));
    public static final PacketCodec<RegistryFriendlyByteBuf, OpenSignPayload> CODEC =
        PacketCodec.tuple(
            BlockPos.STREAM_CODEC, OpenSignPayload::pos,
            TipSignData.PACKET_CODEC, OpenSignPayload::data,
            PacketCodecs.BOOL, OpenSignPayload::authorMode,
            OpenSignPayload::new
        );
    @Override
    public Id<? extends CustomPayload> getId() { return ID; }
}
```

### 4.6 Screen Architecture

TipSign uses a **client-only Screen** (not a `HandledScreen`/`AbstractContainerScreen`) for both Reader and Author UIs. This simplifies the architecture significantly — we don't need a `ScreenHandler`/`AbstractContainerMenu` with slot synchronization since TipSign has no inventory. Instead:

1. **Server** validates interactions and sends `OpenSignS2C` packet with the sign's data
2. **Client** receives the packet and opens the appropriate `Screen` subclass
3. **Author UI save** sends `UpdateSignC2S` packet back to server for validation and persistence

```
Player right-clicks block
        │
        ▼ (Server)
TipSignBlock.useWithoutItem() / useItemOn()
  ├─ Checks permissions (owner? op? reader?)
  ├─ Reads TipSignData from BlockEntity
  └─ Sends OpenSignS2C packet
        │
        ▼ (Client)
ClientPlayNetworking receiver
  ├─ authorMode=true  → new TipSignAuthorScreen(data, pos)
  └─ authorMode=false → new TipSignReaderScreen(data, pos)
        │
        ▼ (Client, on Author save)
TipSignAuthorScreen.onSave()
  └─ Sends UpdateSignC2S packet
        │
        ▼ (Server)
ServerPlayNetworking receiver
  ├─ Validates ownership / op
  ├─ Validates URLs against config whitelist
  ├─ Updates BlockEntity data
  ├─ Calls setChanged()
  └─ Triggers DiscoveryManager.onSignChanged()
```

**Version note on interaction methods:** In 1.20.4 and earlier, the method is `use()` (public). In 1.20.5+, `AbstractBlock` overridable methods became `protected`. In 1.21.2+, `ActionResult` was unified — return `ActionResult.SUCCESS` instead of `ItemActionResult.success()`.

**TipSignReaderScreen** (client-only):
- Renders weathered-plank-texture background (landscape-oriented, wider than tall)
- Title rendered prominently at top in bold/carved font style
- Body text in off-white/cream on wood background
- Page navigation with carved arrow buttons
- Ko-fi / Patreon buttons anchored at bottom (brand colors per PRD)
- Inline `[link](url)` rendered as underlined colored text
- All external URL clicks show confirmation modal before `Util.getPlatform().openUri()`

**TipSignAuthorScreen** (client-only):
- Multi-page text editor with page nav
- Title field (32-char max)
- Ko-fi / Patreon URL fields with validation feedback
- Formatting toolbar / reference panel
- Delete All Content button with two-step confirmation
- Save / Cancel buttons

### 4.7 Block Entity Renderer (BER)

A `BlockEntityRenderer<TipSignBlockEntity>` renders the sign title on the block face in the world. This requires the client to have the title synced via `getUpdatePacket()` / `getUpdateTag()`.

The BER only renders the title text — the full content is loaded on-demand when the player opens the UI.

**Band G breaking change (1.21.9+):** Block entity renderers take `OrderedRenderCommandQueue` instead of `MultiBufferSource` (`VertexConsumerProvider` in Yarn). Band G needs its own BER implementation:

```java
// Band A–F:
public void render(TipSignBlockEntity be, float partialTick, PoseStack poseStack,
                   MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
    // Traditional rendering with buffer source
}

// Band G (1.21.9+):
public void render(BlockEntityRenderState state, PoseStack poseStack,
                   OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
    queue.submitText(poseStack, 0, 0, text, false,
                     Font.DisplayMode.NORMAL, state.lightmapCoordinates,
                     Colors.WHITE, 0, Colors.BLACK);
}
```

The VersionAdapter includes a `createBlockEntityRenderer()` factory method so each band can provide the correct BER implementation.

### 4.8 Configuration System

TipSign uses **NightConfig** (TOML) bundled as a jar-in-jar dependency in the `:common` module. Config is loaded once at mod initialization and lives in `config/tipsign.toml`.

```toml
# TipSign Configuration

# Whether only the owner can break a placed Tip Sign
ownerOnlyBreak = true

# Show confirmation dialog before opening external URLs in browser
requireConfirmBeforeBrowserOpen = true

# Maximum number of pages per sign (1–50)
maxPages = 10

# Whether the crafting recipe is enabled (false = /give only)
craftingEnabled = true

# Allowed URL schemes for links
allowedUrlSchemes = ["https"]

# Whether inline [text](url) hyperlinks are clickable
allowInlineLinks = true

# Whitelist of permitted domains for supporter buttons and inline links
allowedLinkDomains = ["ko-fi.com", "patreon.com"]

# --- Discovery API ---

# Enable/disable all discovery output (file + webhook)
discoveryEnabled = true

# Background snapshot write interval in seconds (60–3600)
discoveryIntervalSeconds = 300

# Stable server ID (auto-generated UUID on first launch, do not edit)
serverId = ""

# Webhook URL for push notifications (empty = disabled)
webhookUrl = ""

# HMAC-SHA256 secret for webhook signature (empty = unsigned)
webhookSecret = ""
```

### 4.9 Discovery API (E7)

The discovery system lives entirely in the `:common` module (no MC dependencies) except for the event triggers in `DiscoveryManager` which bridges between block entity events and the snapshot/webhook logic.

```
Block Entity event (place/save/delete/break)
        │
        ▼
DiscoveryManager.onSignChanged()
  ├─ Debounce (5-second window for rapid edits)
  └─ Enqueue snapshot job
        │
        ▼ (async, off game thread)
SnapshotWriter.writeAtomically()
  ├─ Collects all TipSignBlockEntities across loaded worlds
  ├─ Serializes to TipSignSnapshot (matches US-702 schema)
  ├─ Writes to config/tipsign/tipsigns.json.tmp
  └─ Atomic rename to tipsigns.json
        │
        ▼ (if webhookUrl configured)
WebhookDispatcher.push()
  ├─ POST to webhookUrl with JSON payload
  ├─ Headers: Content-Type, X-TipSign-Server-ID, X-TipSign-Signature (if secret set)
  ├─ HMAC-SHA256 signing with webhookSecret
  └─ Retry: 3 attempts, exponential backoff (2s, 4s, 8s)
```

The background interval timer runs on a dedicated `ScheduledExecutorService` (single thread) and fires independently of event-driven pushes.

---

## 5. Data Flow Diagrams

### 5.1 Sign Placement

```
Player places TipSign item
        │
        ▼
TipSignBlock.setPlacedBy()
  ├─ Creates TipSignBlockEntity at pos
  ├─ Sets ownerUuid = player.getUUID()
  ├─ Sets ownerUsername = player.getName()
  ├─ If item has stored data (picked-up sign):
  │     └─ VersionAdapter.readFromItemStack() → restores TipSignData
  ├─ Else: initializes blank TipSignData with new UUID, "Notice" title
  ├─ setChanged()
  ├─ Triggers DiscoveryManager.onSignChanged()
  └─ Sends OpenSignS2C (authorMode=true) to placing player
```

### 5.2 Sign Break & Item Drop

```
Player breaks TipSign block
        │
        ▼
TipSignBlock.playerWillDestroy() / loot table
  ├─ Permission check:
  │   ├─ Owner? → allow
  │   ├─ Op with tipsign.admin? → allow
  │   └─ ownerOnlyBreak=true & not owner? → deny (block resists)
  ├─ Create ItemStack of tipsign:sign_post
  ├─ VersionAdapter.writeToItemStack(stack, blockEntity.getData())
  │   ├─ Band A: writes full NBT to BlockEntityTag
  │   └─ Band B+: writes to custom DataComponentType
  ├─ VersionAdapter.setItemTooltipTitle(stack, data.title())
  ├─ Drop item in world
  └─ Triggers DiscoveryManager.onSignChanged()
```

### 5.3 NBT / Component Key Map

All bands use the same logical key names, just different storage mechanisms:

| Key | Type | Description |
|-----|------|-------------|
| `Id` | String (UUID) | Unique sign instance ID |
| `Title` | String | Sign title (max 32 chars) |
| `Pages` | List\<String\> | Ordered page content with formatting codes |
| `KofiUrl` | String (nullable) | Validated Ko-fi URL |
| `PatreonUrl` | String (nullable) | Validated Patreon URL |
| `OwnerUuid` | UUID | Placing player's UUID |
| `OwnerUsername` | String | Player name at placement time |
| `PlacedAt` | String (ISO-8601) | First placement timestamp |
| `LastEditedAt` | String (ISO-8601) | Last Author UI save timestamp |

---

## 6. Resource & Data Pack Structure

### 6.1 Assets (Client)

```
assets/tipsign/
├── blockstates/
│   └── sign_post.json           # facing property → model rotation
├── models/
│   ├── block/sign_post.json     # Custom sign post model (JSON block model)
│   └── item/sign_post.json      # Inventory item model (Band A–D)
├── items/
│   └── sign_post.json           # Item model definition (Band E+, required from 1.21.4)
├── textures/
│   ├── block/sign_post.png      # Block texture atlas entry
│   ├── gui/reader_bg.png        # Weathered plank 9-slice background
│   ├── gui/author_bg.png        # Author UI background
│   └── gui/widgets.png          # Buttons, arrows, icons sprite sheet
└── lang/
    └── en_us.json               # Block name, UI strings, chat messages
```

**Band E+ (1.21.4+) item model definitions:** Starting in 1.21.4, items require a definition file at `assets/<namespace>/items/<item>.json` that specifies how the item renders. The old `models/item/` approach still works for the block model reference, but the item definition file is mandatory.

### 6.2 Data (Server)

Data pack paths were singularized in 1.21. Band A–B use plural paths; Band C+ use singular paths.

```
# Band C+ (1.21+) paths shown — Band A–B use recipes/, loot_tables/, advancements/
data/tipsign/
├── recipe/
│   └── sign_post.json           # Shaped recipe (Gold Nugget + Plank + Sticks)
├── loot_table/blocks/
│   └── sign_post.json           # Drops with copy_components (B+) or copy_nbt (A)
├── advancement/recipe/
│   └── sign_post.json           # Recipe unlock advancement
└── tags/
    └── blocks/
        └── mineable/
            └── axe.json         # Sign post is axe-mineable
```

### 6.3 Loot Table Version Split

The loot table must differ between Band A and Band B+ because item data storage changed:

**Band A (1.20.1–1.20.4):** Uses `copy_nbt` function with `BlockEntityTag.*` targets.

**Band B+ (1.20.5+):** Uses `copy_components` function with the custom `tipsign:sign_data` component type.

Each version subproject includes its own `data/` resources that override the shared ones.

### 6.4 Recipe Definition

```json
{
  "type": "minecraft:crafting_shaped",
  "pattern": [
    "GPG",
    " S ",
    " S "
  ],
  "key": {
    "G": { "item": "minecraft:gold_nugget" },
    "P": { "tag": "minecraft:planks" },
    "S": { "item": "minecraft:stick" }
  },
  "result": {
    "item": "tipsign:sign_post",
    "count": 1
  }
}
```

Note: For Band D+ (1.21.2+), the recipe system was fundamentally rewritten — recipes are now server-side only, clients receive `RecipeDisplayEntry` for display. The `RecipeBridge` in each version adapter handles registration differences. The JSON format above works for Band A–C; Band D+ may require programmatic registration via the new `RecipeGenerator` API.

---

## 7. Permissions Model

### 7.1 Permission Checks

```java
public class TipSignPermissions {

    public static boolean canEdit(ServerPlayer player, TipSignBlockEntity be) {
        return isOwner(player, be) || isAdmin(player);
    }

    public static boolean canBreak(ServerPlayer player, TipSignBlockEntity be) {
        if (!TipSignConfig.get().ownerOnlyBreak) return true;
        return isOwner(player, be) || isAdmin(player);
    }

    private static boolean isOwner(ServerPlayer player, TipSignBlockEntity be) {
        return player.getUUID().equals(be.getData().ownerUuid());
    }

    private static boolean isAdmin(ServerPlayer player) {
        // Check op level first (works without permission mods)
        if (player.hasPermissions(2)) return true;
        // Check Fabric Permissions API if available (soft dependency)
        return checkFabricPermission(player, "tipsign.admin");
    }
}
```

### 7.2 Fabric Permissions API Integration

TipSign uses the `fabric-permissions-api` (`me.lucko:fabric-permissions-api:0.6.1`) as an **optional** soft dependency. If present, the `tipsign.admin` node is checked via `Permissions.check(player, "tipsign.admin")`. If absent, only op-level 2+ is considered admin. This avoids a hard dependency while supporting LuckPerms and similar permission mods.

```groovy
// In version subproject build.gradle — compile-only, not bundled
modCompileOnly "me.lucko:fabric-permissions-api:0.6.1"
```

At runtime, check mod presence before calling:

```java
private static boolean checkFabricPermission(ServerPlayer player, String node) {
    if (FabricLoader.getInstance().isModLoaded("fabric-permissions-api-v0")) {
        return Permissions.check(player, node);
    }
    return false;
}
```

---

## 8. Thread Safety & Async Concerns

| Operation | Thread | Safety Strategy |
|-----------|--------|-----------------|
| Block entity read/write | Server thread | Single-threaded, no concerns |
| Author UI save (C2S packet) | Netty → Server thread | `server.execute()` wraps all world mutations |
| Reader UI open (S2C packet) | Netty → Client thread | `client.execute()` wraps screen creation |
| Snapshot file write | Dedicated executor | Atomic write (tmp + rename); collects data on server thread, serializes off-thread |
| Webhook POST | Dedicated executor | Fire-and-forget with retry; `java.net.http.HttpClient` async |
| Config load | Server startup thread | Single load, immutable config object thereafter |
| Debounce timer | `ScheduledExecutorService` | Thread-safe `AtomicReference<ScheduledFuture>` for cancel/reschedule |

---

## 9. Build & CI

### 9.1 Gradle Configuration

**Root `gradle.properties`:**

```properties
mod_version=1.0.0
maven_group=dev.blockacademy
archives_base_name=tipsign

# Fabric Loader — minimum version that supports all bands
loader_version=0.18.4

# NightConfig version (bundled in :common)
nightconfig_version=3.8.3

# Gson version (bundled in :common, for JSON snapshot)
gson_version=2.10.1

# Fabric Permissions API (soft dependency)
permissions_api_version=0.6.1
```

**Each `versions/*/gradle.properties`:**

```properties
# Example: versions/1.20.1/gradle.properties
minecraft_version=1.20.1
fabric_api_version=0.92.2+1.20.1
loom_version=1.5
java_version=17
```

```properties
# Example: versions/1.20.5/gradle.properties
minecraft_version=1.20.5
fabric_api_version=0.97.8+1.20.5
loom_version=1.6
java_version=21
```

```properties
# Example: versions/1.21/gradle.properties
minecraft_version=1.21
fabric_api_version=0.102.0+1.21
loom_version=1.7
java_version=21
```

```properties
# Example: versions/1.21.2/gradle.properties
minecraft_version=1.21.2
fabric_api_version=0.106.1+1.21.2
loom_version=1.8
java_version=21
```

```properties
# Example: versions/1.21.4/gradle.properties
minecraft_version=1.21.4
fabric_api_version=0.114.0+1.21.4
loom_version=1.10
java_version=21
```

```properties
# Example: versions/1.21.6/gradle.properties
minecraft_version=1.21.6
fabric_api_version=0.128.1+1.21.6
loom_version=1.10
java_version=21
```

```properties
# Example: versions/1.21.9/gradle.properties
minecraft_version=1.21.9
fabric_api_version=0.134.1+1.21.9
loom_version=1.14
loom_plugin_id=net.fabricmc.fabric-loom-remap
java_version=21
# Note: 1.21.11 requires Gradle 9.2+
```

**Each `versions/*/build.gradle` uses Mojang Mappings:**

```groovy
// In each version subproject's build.gradle
plugins {
    id "${project.hasProperty('loom_plugin_id') ? project.loom_plugin_id : 'fabric-loom'}" version "${project.loom_version}"
}
dependencies {
    minecraft "com.mojang:minecraft:${project.minecraft_version}"
    mappings loom.officialMojangMappings()
    modImplementation "net.fabricmc:fabric-loader:${rootProject.loader_version}"
    modImplementation "net.fabricmc.fabric-api:fabric-api:${project.fabric_api_version}"
    modCompileOnly "me.lucko:fabric-permissions-api:${rootProject.permissions_api_version}"
}
tasks.withType(JavaCompile).configureEach { it.options.release = Integer.parseInt(project.java_version) }
```

### 9.2 Build Commands

```bash
# Build all version bands
./gradlew buildAll

# Build a specific band
./gradlew :versions:1.20.1:build

# Run client for a specific band (development)
./gradlew :versions:1.21:runClient
```

### 9.3 Output Artifacts

```
build/libs/
├── tipsign-1.0.0+mc1.20.1.jar     # Band A
├── tipsign-1.0.0+mc1.20.5.jar     # Band B
├── tipsign-1.0.0+mc1.21.jar       # Band C
├── tipsign-1.0.0+mc1.21.2.jar     # Band D
├── tipsign-1.0.0+mc1.21.4.jar     # Band E
├── tipsign-1.0.0+mc1.21.6.jar     # Band F
└── tipsign-1.0.0+mc1.21.9.jar     # Band G
```

---

## 10. Mappings Strategy

All development uses **Mojang Mappings** (`officialMojangMappings()` in Loom). Rationale:

1. Yarn will not be maintained past 1.21.11 as Minecraft transitions to unobfuscated releases
2. Mojang mappings provide the smoothest migration path to post-1.21.11 (26.x) unobfuscated versions
3. Cross-referencing vanilla source during development is simpler with official names
4. Loom handles the remapping to intermediary at build time regardless of dev mappings

**Note:** Fabric API class names (e.g., `ServerPlayNetworking`, `PayloadTypeRegistry`, `FabricLoader`) are the same regardless of mappings — only vanilla Minecraft classes differ between Yarn and Mojang mappings.

---

## 11. Testing Strategy

### 11.1 Unit Tests (`:common` module)

- `UrlValidator` — scheme/domain whitelist, edge cases (trailing slashes, case insensitivity)
- `LinkParser` — markdown link extraction, malformed input, nested brackets
- `TipSignConfig` — TOML parsing, default fallbacks, invalid value handling
- `TipSignSnapshot` — JSON serialization round-trip, schema compliance
- `WebhookDispatcher` — retry logic (mock HTTP client), HMAC-SHA256 signatures

### 11.2 Integration Tests (per version band)

- Place sign, verify BlockEntity data persists through chunk unload/reload
- Break sign, verify item NBT/component contains full data
- Re-place item, verify data restores
- Author UI save → verify server-side data update
- Config toggle: `ownerOnlyBreak`, `allowInlineLinks`, `craftingEnabled`
- Webhook push content matches snapshot schema

### 11.3 Manual QA Matrix

Each release requires manual testing on:
- One version per band (representative)
- Singleplayer + dedicated server
- With and without Fabric API
- With and without LuckPerms

---

## 12. Risk Register

| Risk | Severity | Mitigation |
|------|----------|------------|
| Minecraft API breakage in hotfix versions within a band | Medium | CI builds against all versions in each band; pin to latest known-good Fabric API per version |
| NightConfig TOML parser edge cases (blank config file bug) | Low | Validate on load, regenerate defaults on parse failure; log warning |
| Webhook delivery failure flooding logs | Low | Cap retry at 3 attempts; exponential backoff; single warning log per failure cycle |
| Item component system changes in future MC versions | Medium | Abstracted behind `VersionAdapter`; new band required only if component API changes |
| Player UUID resolution for `ownerUsername` | Low | Resolve at placement time from `ServerPlayer.getName()`; stale if player renames, but acceptable for display-only field |
| Post-1.21.11 unobfuscated MC (26.x) | High | Mojang mappings strategy minimizes migration effort; new Band H will be needed but VersionAdapter pattern isolates changes |
| Gradle 9.2 requirement for Band G | Medium | Band G subproject can use Gradle 9.2+ while other bands use 8.x, or entire project upgrades to 9.2 (Loom 1.14 supports it) |
| Per-band Loom version complexity | Medium | Each version subproject declares its own Loom plugin version; root build.gradle only configures shared concerns |

---

## 13. Open Technical Decisions

| # | Question | Options | Recommendation | Status |
|---|----------|---------|----------------|--------|
| TD-1 | Custom block model format | JSON block model vs. BBER (built-in block entity renderer) | JSON block model for sign post shape; BER only for dynamic title text | **Decided** |
| TD-2 | Config library | NightConfig (TOML) vs. hand-rolled JSON vs. Cloth Config | NightConfig — lightweight, no Fabric API dependency, TOML supports comments, proven in ecosystem | **Decided** |
| TD-3 | ServiceLoader vs. direct instantiation for VersionAdapter | ServiceLoader (clean SPI) vs. compile-time constant | ServiceLoader — cleaner separation, each band JAR registers its impl | **Decided** |
| TD-4 | Shared-mc compilation target | Compile against oldest (1.20.1) vs. middle version | Compile against 1.21 (middle of range) using only stable APIs; version adapters handle all divergent calls | **Decided** |
| TD-5 | Screen approach | HandledScreen (server-synced) vs. client-only Screen | Client-only Screen — TipSign has no inventory; data synced via custom packets | **Decided** |
| TD-6 | Snapshot trigger — full scan vs. incremental | Full snapshot every time vs. tracking dirty signs | Full snapshot (simpler, sign count expected to be small per server); revisit if perf issues arise | **Decided** |
| TD-7 | Piston behavior | Immovable vs. moves with data | Immovable (return `PushReaction.BLOCK`) — simpler; drops as item if pushed | Open |

---

## 14. Appendix: Version-Specific API Cheat Sheet

All names below use **Mojang Mappings**.

### A. BlockEntity Serialization Signatures

| Version Range | Write Method | Read Method |
|---------------|-------------|-------------|
| 1.20.1–1.20.4 | `saveAdditional(CompoundTag tag)` | `load(CompoundTag tag)` |
| 1.20.5–1.21.5 | `saveAdditional(CompoundTag tag, HolderLookup.Provider registries)` | `loadAdditional(CompoundTag tag, HolderLookup.Provider registries)` |
| 1.21.6–1.21.11 | `saveAdditional(ValueOutput view)` | `loadAdditional(ValueInput view)` |

### B. Item Data Storage

| Version Range | Mechanism | Key/Type |
|---------------|-----------|----------|
| 1.20.1–1.20.4 | `BlockEntityTag` NBT compound on ItemStack | `stack.getTagElement("BlockEntityTag")` |
| 1.20.5–1.21.11 | `DataComponentType<TipSignData>` | `stack.get(TipSignComponents.SIGN_DATA)` |

### C. Networking Registration

| Version Range | Registration Pattern |
|---------------|---------------------|
| 1.20.1–1.20.4 | `ServerPlayNetworking.registerGlobalReceiver(ResourceLocation, handler)` — handler receives `FriendlyByteBuf` |
| 1.20.5–1.21.11 | `PayloadTypeRegistry.playS2C().register(ID, CODEC)` + `ServerPlayNetworking.registerGlobalReceiver(ID, handler)` — handler receives typed `CustomPayload` |

### D. Identifier Construction

| Version Range | Pattern |
|---------------|---------|
| 1.20.1–1.20.4 | `new ResourceLocation("tipsign", "sign_post")` |
| 1.20.5–1.21.11 | `ResourceLocation.fromNamespaceAndPath("tipsign", "sign_post")` |

### E. Chunk Data Sync

| Version Range | Method |
|---------------|--------|
| 1.20.1–1.20.4 | `getUpdateTag()` returns `CompoundTag` |
| 1.20.5–1.21.5 | `getUpdateTag(HolderLookup.Provider)` returns `CompoundTag` |
| 1.21.6–1.21.11 | `getUpdateTag(HolderLookup.Provider)` — adapted from `saveAdditional(ValueOutput)` internally |

### F. Java Version Requirements

| Minecraft | Minimum Java |
|-----------|-------------|
| 1.20.1–1.20.4 | Java 17 |
| 1.20.5+ | Java 21 |

### G. Block/Item Registration (1.21.2+ requires RegistryKeys)

```java
// Band A–C: simple registration
Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath("tipsign", "sign_post"), block);

// Band D+ (1.21.2+): RegistryKey required in settings
ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("tipsign", "sign_post"));
Block block = new TipSignBlock(BlockBehaviour.Properties.of().registryKey(key));
Registry.register(BuiltInRegistries.BLOCK, key, block);
```

### H. BER Rendering Signatures

| Version Range | Render Method |
|---------------|--------------|
| 1.20.1–1.21.1 | `render(T be, float partialTick, PoseStack, MultiBufferSource, int light, int overlay)` |
| 1.21.2–1.21.8 | `render(S state, PoseStack, MultiBufferSource, int light, int overlay)` — uses `EntityRenderState` |
| 1.21.9–1.21.11 | `render(S state, PoseStack, OrderedRenderCommandQueue, CameraRenderState)` |

### I. ActionResult Changes

| Version Range | Return Type from Block Interaction |
|---------------|------------------------------------|
| 1.20.1–1.21.1 | `ActionResult` (with `TypedActionResult<ItemStack>` and `ItemActionResult` as separate types) |
| 1.21.2–1.21.11 | Unified `ActionResult` — `ActionResult.SUCCESS`, `ActionResult.PASS`, `ActionResult.FAIL` |

### J. Loom Plugin & Gradle Requirements

| Band | Loom Plugin ID | Loom Version | Minimum Gradle |
|------|---------------|-------------|----------------|
| A | `fabric-loom` | 1.5 | 8.1 |
| B | `fabric-loom` | 1.6 | 8.6 |
| C | `fabric-loom` | 1.7 | 8.6 |
| D | `fabric-loom` | 1.8 | 8.10 |
| E | `fabric-loom` | 1.10 | 8.12 |
| F | `fabric-loom` | 1.10 | 8.12 |
| G | `net.fabricmc.fabric-loom-remap` | 1.14 | 9.2 |

---

*End of document. This TDD is a living document and will be updated as implementation progresses and open decisions are resolved.*
