# Fabric modding master reference: 1.20.1 through 1.21.11

**Every Minecraft Java Edition version from 1.20.1 to 1.21.11 introduced changes that affect Fabric mod development, ranging from trivial hotfixes to the largest API overhaul in modern modding history.** This document covers all 18 versions—including the item component revolution in 1.20.5, the recipe system rewrite in 1.21.2, and the rendering pipeline overhaul in 1.21.6/1.21.9—so that a developer can build or port a Fabric mod for any version in this range without additional research. After 1.21.11, Mojang switched to unobfuscated releases under a new versioning scheme (26.x), making this the final chapter of obfuscated Minecraft modding.

---

## Ecosystem compatibility at a glance

The table below captures the critical build-time dependencies for every version. **Java jumped from 17 to 21 at 1.20.5**, the single most disruptive toolchain change in this range.

| MC Version | Release Date | Fabric Loader | Fabric API | Loom | Yarn Mappings | Java | Gradle |
|---|---|---|---|---|---|---|---|
| **1.20.1** | Jun 12 2023 | ≥0.14.21 | 0.92.2+1.20.1 | 1.2 | 1.20.1+build.10 | 17 | 8.1+ |
| **1.20.2** | Sep 21 2023 | ≥0.14.22 | 0.91.6+1.20.2 | 1.3 | 1.20.2+build.4 | 17 | 8.1+ |
| **1.20.3** | Dec 5 2023 | ≥0.15.0 | 0.91.3+1.20.3 | 1.4 | 1.20.3+build.1 | 17 | 8.3+ |
| **1.20.4** | Dec 7 2023 | ≥0.15.1 | 0.97.3+1.20.4 | 1.4 | 1.20.4+build.3 | 17 | 8.3+ |
| **1.20.5** | Apr 23 2024 | ≥0.15.10 | 0.97.8+1.20.5 | 1.6 | 1.20.5+build.1 | **21** | 8.6+ |
| **1.20.6** | Apr 29 2024 | ≥0.15.10 | 0.100.8+1.20.6 | 1.6 | 1.20.6+build.3 | 21 | 8.6+ |
| **1.21** | Jun 13 2024 | ≥0.15.11 | 0.102.0+1.21 | 1.6 | 1.21+build.9 | 21 | 8.6+ |
| **1.21.1** | Aug 8 2024 | ≥0.15.11 | 0.116.9+1.21.1 | 1.7 | 1.21.1+build.3 | 21 | 8.6+ |
| **1.21.2** | Oct 2024 | ≥0.16.7 | 0.106.1+1.21.2 | 1.8 | 1.21.2+build.1 | 21 | 8.10+ |
| **1.21.3** | Oct 2024 | ≥0.16.7 | 0.114.1+1.21.3 | 1.8 | 1.21.3+build.2 | 21 | 8.10+ |
| **1.21.4** | Dec 3 2024 | ≥0.16.9 | 0.114.0+1.21.4 | 1.9 | 1.21.4+build.8 | 21 | 8.11+ |
| **1.21.5** | Mar 25 2025 | ≥0.16.10 | 0.119.5+1.21.5 | 1.10 | 1.21.5+build.1 | 21 | 8.12+ |
| **1.21.6** | Jun 17 2025 | ≥0.16.14 | 0.128.1+1.21.6 | 1.10 | 1.21.6+build.1 | 21 | 8.12+ |
| **1.21.7** | Jun 30 2025 | ≥0.16.14 | 0.129.0+1.21.7 | 1.10 | 1.21.7+build.1 | 21 | 8.12+ |
| **1.21.8** | Jul 17 2025 | ≥0.16.14 | 0.132.0+1.21.8 | 1.10 | 1.21.8+build.1 | 21 | 8.12+ |
| **1.21.9** | Sep 30 2025 | ≥0.17.2 | 0.134.1+1.21.9 | 1.11 | 1.21.9+build.1 | 21 | 8.14+ |
| **1.21.10** | Oct 7 2025 | ≥0.17.2 | 0.138.4+1.21.10 | 1.11 | 1.21.10+build.1 | 21 | 8.14+ |
| **1.21.11** | Dec 9 2025 | ≥0.18.1 | 0.141.3+1.21.11 | 1.14 | 1.21.11+build.4 | 21 | **9.2+** |

### Common library version matrix

| Library | 1.20.1 | 1.20.4 | 1.20.6 | 1.21.1 | 1.21.2 | 1.21.4 | 1.21.5 | 1.21.9 | 1.21.11 |
|---|---|---|---|---|---|---|---|---|---|
| Cloth Config | 11.1.136 | 13.0.121 | 14.0.126 | 15.0.140 | 16.0.141 | 17.0.142 | 18.0.145 | 20.0.149 | 21.11.153 |
| ModMenu | 7.1.0 | 9.2.0 | 10.0.0 | 11.0.3 | 12.0.x | 13.0.3 | 14.0.0 | 16.0.0 | — |
| Trinkets | 3.7.2 | 3.8.1 | 3.9.0 | 3.10.0 | *(EOL)* | — | — | — | — |
| Cardinal Comp. | 5.2.2 | 5.4.x | 6.0.x | 6.1.3 | 6.2.x | 6.2.x | 6.3.1 | 7.1.0β | 7.3.0 |
| GeckoLib | 4.2.4 | 4.4.4 | 4.5.4 | 4.6.6 | — | 4.8.2 | 5.1.0 | — | 5.4.5 |
| REI | 12.1.725 | 14.0.680 | 15.0.787 | 16.0.x | 17.0.794 | 18.0.796 | 19.0.x | 21.9.813 | 21.11.814 |
| Architectury | v9.x | v11.x | v12.x | v13.x | v14.x | v15.x | v16.x | v18.x | v19.x |
| Sodium | 0.4.10 | 0.5.5 | 0.5.8 | 0.6.0 | — | 0.6.x | — | — | 0.8.0 |

Trinkets development by emilyploszaj ended at 1.21.1. For 1.21.2+ use community forks ("Trinkets Updated") or migrate to the **Accessories** API.

---

## 1.20.1 → 1.20.2: Configuration networking and codec migration begin

**Severity: MODERATE.** The configuration networking phase and recipe codec changes mark the start of Mojang's multi-version migration toward codecs and structured networking.

### Networking gained a configuration phase

A new **configuration stage** between login and play was introduced. Mods using `ServerLoginNetworking` for pre-join communication should migrate to `ServerConfigurationNetworking`. The server executes sequential "tasks" during configuration; failure to call `handler.completeTask()` leaves clients stuck.

```java
// Server: register a configuration task
ServerConfigurationConnectionEvents.CONFIGURE.register((handler, server) -> {
    if (ServerConfigurationNetworking.canSend(handler, ConfigS2CPacket.TYPE)) {
        handler.addTask(new ConfigTask("config"));
    }
});

// Server: handle client response — MUST complete the task
ServerConfigurationNetworking.registerGlobalReceiver(ConfigC2SPacket.TYPE, (packet, handler, sender) -> {
    handler.completeTask(ConfigTask.KEY);
});
```

`ClientPlayNetworking#createC2SPacket` now returns `Packet<ServerCommonPacketListener>` instead of `Packet<ServerPlayPacketListener>`.

### Registry and data-driven changes

`RegistryAttribute#PERSISTED` was removed. Custom registry tag paths changed from `/tags/registry_path/` to `/tags/registry_namespace/registry_path/`. A new Dynamic Registry API was added. `BlockSetTypeRegistry` and `WoodTypeRegistry` were deprecated in favor of builders:

```java
// Old
BlockSetTypeRegistry.registerWood(TEAL_TYPE_ID);
// New
BlockSetTypeBuilder.copyOf(BlockSetType.OAK).build(TEAL_TYPE_ID);
```

Recipes and advancements were refactored to use records (`RecipeEntry`, `AdvancementEntry`). `RecipeExporter` replaced `Consumer<RecipeJsonProvider>`. **`CustomIngredientSerializer` now requires a `codec()` method** returning a Codec instead of JSON read/write methods. Status effects are stored by string ID instead of integer.

### Rendering changes

`drawTexture` became `drawGuiTexture` (no UV coordinates needed—GUI sprites are now individual textures under `textures/gui/sprites`). `Screen#renderBackground` gained mouse position and `tickDelta` parameters. `Screen#onMouseScrolled` takes both horizontal and vertical scroll amounts.

### Removed Fabric API modules

`fabric-networking-v0`, `fabric-loot-tables-v1`, `LootEntryTypeRegistry`, and `CriterionRegistry` were removed. Deprecated modules now ship by default in dev environments.

---

## 1.20.2 → 1.20.3: Block codecs, text serialization overhaul, tick system

**Severity: MODERATE-HIGH.** Text goes to NBT over the wire, blocks require codecs, and the tick system is redesigned.

### Block codecs introduced

All `Block` subclasses must implement `getCodec()`. While currently unused at runtime, returning `null` or throwing is acceptable for now. This lays groundwork for future data-driven block serialization.

### Text serialization overhauled

**Text is now sent as NBT over the network** (was JSON). `Text#translatable` arguments must be numbers, booleans, strings, or other `Text` objects only—use `Text.stringifiedTranslatable()` for `BlockPos`, `Identifier`, etc. `Codecs.TEXT` moved to `TextCodecs.CODEC`. `Text.Serializer` split into `Serializer` (Gson adapter) and `Serialization` (static methods); `toJson` was renamed to `toJsonString`.

### Major block class changes

`GravelBlock` and `SandBlock` → **`ColoredFallingBlock`**. `AbstractGlassBlock`/`GlassBlock` removed and replaced by `TransparentBlock`/`TranslucentBlock`. `FernBlock` → `ShortPlantBlock`. `SaplingGenerator` individual classes removed, replaced with static fields. `AbstractBlock#randomTick` no longer calls `scheduledTick`.

### Tick system

`TickManager` introduced. In server/world tick events, check `getTickManager().shouldTick()`. Not needed inside entity/block entity ticks. `TickManager#getMillisPerTick()` returns the intended MSPT (game can run faster/slower than 50ms with the new `/tick` command).

### Rendering

`ClickableWidget#render` made **final**. Override `renderWidget` instead. `PopupScreen` and builder-pattern `CheckboxWidget` added.

### Command system

`CommandManager#execute` no longer returns int. Use `ServerCommandSource#withReturnValueConsumer` instead. `/tick` command added.

---

## 1.20.3 → 1.20.4: No modding changes

**Severity: NONE.** 1.20.4 is a single-bug hotfix released two days after 1.20.3. It is the last version supporting 32-bit operating systems and Java 17-20. Mods compiled for 1.20.3 work on 1.20.4 without modification.

---

## 1.20.4 → 1.20.5: The component revolution and networking rewrite

**Severity: CRITICAL — the largest breaking change in modern Minecraft modding history.** Item stacks abandon NBT for a typed component system. Networking switches from manual PacketByteBuf to PacketCodec. Java jumps from 17 to 21.

### Item component system replaces NBT entirely

`ItemStack.getNbt()`, `getOrCreateNbt()`, `hasNbt()`, `setTag()`, and all NBT-based methods on stacks were **removed**. They are replaced by a typed key-value component system built on `DataComponentType<T>`.

```java
// OLD (1.20.4 — NBT)
NbtCompound nbt = stack.getOrCreateNbt();
nbt.putInt("mymod_counter", 5);
int counter = stack.getNbt() != null ? stack.getNbt().getInt("mymod_counter") : 0;

// NEW (1.20.5 — Components)
stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Cool Sword"));
@Nullable Text name = stack.get(DataComponentTypes.CUSTOM_NAME);
int damage = stack.getOrDefault(DataComponentTypes.DAMAGE, 0);
boolean hasName = stack.contains(DataComponentTypes.CUSTOM_NAME);
stack.remove(DataComponentTypes.CUSTOM_NAME);
```

**Registering custom components:**

```java
public static final DataComponentType<Integer> CLICK_COUNT =
    Registry.register(Registries.DATA_COMPONENT_TYPE,
        Identifier.of("mymod", "click_count"),
        DataComponentType.<Integer>builder()
            .codec(Codec.INT)
            .packetCodec(PacketCodecs.VAR_INT)
            .build());
```

**Setting defaults on items via `Item.Settings`:**

```java
new Item(new Item.Settings()
    .component(DataComponentTypes.CUSTOM_NAME, Text.literal("Hello"))
    .component(MY_COMPONENT, new MyComponent(0.0f, false)));
```

Component values must be treated as immutable—always copy, modify, then set. Over **60 vanilla component types** were introduced covering everything from enchantments to banner patterns to potion contents.

### Networking rewrite: FabricPacket → CustomPayload + PacketCodec

`FabricPacket` and `PacketType` were removed. Packets now implement vanilla `CustomPayload`, declare a `PacketCodec`, and **must be registered on both sender and receiver sides** via `PayloadTypeRegistry`:

```java
// Packet definition
public record SlapPacket(UUID slapped) implements CustomPayload {
    public static final Id<SlapPacket> PACKET_ID =
        new Id<>(Identifier.of("mymod", "slap"));
    public static final PacketCodec<RegistryByteBuf, SlapPacket> PACKET_CODEC =
        Uuids.PACKET_CODEC.xmap(SlapPacket::new, SlapPacket::slapped).cast();

    @Override public Id<? extends CustomPayload> getId() { return PACKET_ID; }
}

// Registration (BOTH sides, in ModInitializer)
PayloadTypeRegistry.playC2S().register(SlapPacket.PACKET_ID, SlapPacket.PACKET_CODEC);

// Receiver
ServerPlayNetworking.registerGlobalReceiver(SlapPacket.PACKET_ID, (payload, context) -> {
    ServerWorld world = context.player().getServerWorld();
    // ...
});
```

**Four registries exist**: `playC2S()`, `playS2C()`, `configurationC2S()`, `configurationS2C()`. Play-phase uses `RegistryByteBuf`; configuration uses `PacketByteBuf`.

### Codec system changes

DFU was updated: `MapCodec` now used in all dispatch codecs (affecting `ParticleType`, `RecipeSerializer`, `BiomeSource`, `Feature`, and dozens more). **`optionalFieldOf` is strict by default**—decoding errors in optional fields are now real errors; use `lenientOptionalFieldOf` for old behavior.

### Block method visibility

All ~50 `AbstractBlock` public overridable methods became **`protected`**: `onUse`, `neighborChanged`, `onPlace`, `onRemove`, `getShape`, `tick`, `randomTick`, `entityInside`, etc.

### Entity changes

`Entity` now implements `SyncedDataHolder`. `defineSynchedData` takes a `Builder`. `setSecondsOnFire` → `igniteForSeconds`. `getMyRidingOffset` → `getVehicleAttachmentPoint`. `MobType` enum removed entirely. `EquipmentSlot.BODY` added for wolf armor. Item damage simplified:

```java
// Old: stack.damage(1, entity, p -> p.sendToolBreakStatus(Hand.MAIN_HAND));
// New: stack.damage(1, entity, EquipmentSlot.MAINHAND);
```

### Fabric API removals

`FabricItemSettings` removed (use vanilla `Item.Settings`). `FabricPacket`/`PacketType` removed. `fabric-containers-v0`, `fabric-events-lifecycle-v0`, `fabric-mining-level-api-v1`, `ScreenRegistry`, `ScreenHandlerRegistry`, NBT ingredient support—all gone.

### Build configuration

Java 21, Loom 1.6, Gradle 8.6+ now required. `FabricBlockSettings` deprecated.

---

## 1.20.5 → 1.20.6: Trivial hotfix

**Severity: NONE.** Only `BlockEntity#parseCustomNameSafe` was added. Mods compiled for 1.20.5 work unchanged on 1.20.6.

---

## 1.20.6 → 1.21: Data-driven enchantments, Identifier API, vertex overhaul

**Severity: HIGH.** The Tricky Trials update brings data-driven enchantments, singularized data pack paths, a vertex rendering overhaul, and the `Identifier` constructor change.

### Identifier constructor made protected

```java
// Old: new Identifier("example", "foo_bar")
// New: Identifier.of("example", "foo_bar")
//      Identifier.ofVanilla("creeper")
//      Identifier.tryParse("namespace", unsanitizedInput)  // nullable
```

This breaks any mixin targeting the `Identifier` constructor.

### Enchantments became fully data-driven

Enchantments are no longer hardcoded. They are defined via JSON data packs using **effect components** (`EnchantmentEffectComponentTypes`). Three effect categories: `EnchantmentValueEffect`, `EnchantmentEntityEffect`, and `AttributeEnchantmentEffect`. Check enchantments via tags:

```java
EnchantmentHelper.hasAnyEnchantmentsIn(stack, EnchantmentTags.PREVENTS_BEE_SPAWNS_WHEN_MINING);
```

Custom enchantment effects require codec registration on both client and server.

### Data pack paths singularized (BREAKS ALL DATA PACKS)

`tags/blocks` → `tags/block`, `tags/entity_types` → `tags/entity_type`, `advancements` → `advancement`, `recipes` → `recipe`, `loot_tables` → `loot_table`, `structures` → `structure`, `functions` → `function`, etc.

### Vertex rendering overhauled

```java
// Old
BufferBuilder builder = tessellator.getBuffer();
builder.begin(...);
builder.vertex(...).texture(...).color(...).next();
tessellator.draw();

// New
BufferBuilder builder = tessellator.begin(...);
builder.vertex(...).texture(...).color(...); // no .next()
BufferRenderer.drawWithGlobalProgram(builder.end());
```

`HudRenderCallback` passes `RenderTickCounter` instead of `tickDelta`. Item color providers now take **ARGB** instead of RGB—wrap with `ColorHelper.Argb.fullAlpha()` or items render transparent.

### Dimension teleportation reworked

`Entity#changeDimension(ServerLevel)` → `changeDimension(DimensionTransition)`. `PortalInfo` class removed, replaced by `DimensionTransition`. `FabricDimensions` API removed—use `Entity#teleportTo`.

### Attribute modifiers use Identifier

```java
// Old: new AttributeModifier(UUID, "name", value, operation)
// New: new AttributeModifier(Identifier.of("mod", "my_modifier"), value, operation)
```

### Recipe input refactored

Recipes now use `RecipeInput` instead of `Container`: `CraftingInput` for crafting grids, `SingleRecipeInput` for single-item recipes.

---

## 1.21 → 1.21.1: Minimal hotfix

**Severity: MINIMAL.** Bug fixes and exploit patches. Servers running 1.21.1 are compatible with 1.21 clients. One notable change: block entities now require all supported blocks to be explicitly added via `BlockEntityType.SIGN.addSupportedBlock(ModBlocks.TEAL_SIGN)`.

---

## 1.21.1 → 1.21.2: The most breaking 1.21.x update

**Severity: VERY HIGH.** RegistryKeys required in all settings, `ServerWorld` required throughout, `ActionResult` unified, entity rendering completely reworked, and the recipe system fundamentally rewritten.

### Block and item settings require registry keys

```java
// Items
RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, Identifier.of("mymod", "test"));
Item.Settings settings = new Item.Settings().registryKey(key);
Registry.register(Registries.ITEM, key, new Item(settings));

// Blocks
RegistryKey<Block> key = RegistryKey.of(RegistryKeys.BLOCK, Identifier.of("mymod", "test"));
Block.Settings settings = Block.Settings.create().registryKey(key);
Registry.register(Registries.BLOCK, key, new Block(settings));
```

Failure to set the key causes `NullPointerException: Block id not set`.

### Registry method mass renames

| Old | New |
|---|---|
| `Registry#getEntry(key)` | `Registry#getOptional(key)` |
| `Registry#entryOf(key)` | `Registry#getOrThrow(key)` |
| `Registry#getOrThrow(key)` | `Registry#getValueOrThrow(key)` |
| `Registry#getOrEmpty(key)` | `Registry#getOptionalValue(key)` |
| `DynamicRegistryManager#get` | `DynamicRegistryManager#getOrThrow` |
| `WrapperLookup#getWrapperOrThrow` | `WrapperLookup#getOrThrow` |

### Entity rendering completely reworked with EntityRenderState

Renderers gain an additional type parameter `S extends EntityRenderState`. Entity data is copied to a state object, and rendering operates solely from the state—**no entity reference during render**:

```java
public class MyRenderer extends EntityRenderer<MyEntity, MyRenderState> {
    public MyRenderState createRenderState() { return new MyRenderState(); }

    public void updateRenderState(MyEntity entity, MyRenderState state, float tickDelta) {
        super.updateRenderState(entity, state, tickDelta);
        state.isSaddled = entity.isSaddled();
    }

    public void render(MyRenderState state, MatrixStack matrices,
                       VertexConsumerProvider consumers, int light) {
        // no entity reference available here
    }
}
```

### ActionResult unified

`TypedActionResult` and `ItemActionResult` merged into a single `ActionResult`:

```java
// Old: TypedActionResult.success(stack, world.isClient())
// New: ActionResult.SUCCESS
// For replacing hand stacks: ActionResult.SUCCESS.withNewHandStack(newStack)
```

### Recipe system fundamentally rewritten

**Recipes are now server-side only.** Clients receive `RecipeDisplayEntry` for display purposes. Recipes identified by `RegistryKey<? extends Recipe>`. One recipe can have multiple display entries. `Ingredient` is now a list of items (not item stacks). Data generation changed:

```java
// Old
public void generate(RecipeExporter exporter) { ... }

// New
protected RecipeGenerator getRecipeGenerator(WrapperLookup registries, RecipeExporter exporter) {
    return new RecipeGenerator(registries, exporter) {
        @Override public void generate() { ... }
    };
}
```

### ServerWorld required explicitly

`Entity#damage` now requires `ServerWorld` and is only called server-side. A separate `clientDamage` exists for the client:

```java
if (world instanceof ServerWorld serverWorld) {
    entity.damage(serverWorld, source, amount);
}
```

### Entity attributes renamed

The `GENERIC_` prefix was dropped: `EntityAttributes.GENERIC_ATTACK_KNOCKBACK` → `EntityAttributes.ATTACK_KNOCKBACK`. Entity creation requires a spawn reason: `EntityType.PIG.create(overworld)` → `EntityType.PIG.create(overworld, SpawnReason.SPAWN_ITEM_USE)`.

### Fuel registration changed

```java
// Old: FuelRegistry.INSTANCE.add(ModItems.TEST_ITEM, 50);
// New:
FuelRegistryEvents.BUILD.register((builder, context) -> {
    builder.add(ModItems.TEST_ITEM, context.baseSmeltTime() / 4);
});
```

### Other changes

`CoreShaderRegistrationCallback` removed—vanilla now loads modded core shaders from resource packs. `LootTables#EMPTY` removed in favor of `Optional`. `Profiler` access changed: `world.getProfiler()` → `Profilers.get()`. `FabricBlockSettings` class fully removed. MixinExtras 0.4.0 bundled with `@WrapMethod` and `@Cancellable` support.

---

## 1.21.2 → 1.21.3: Bug fix only

**Severity: NONE.** 1.21.3 was a bug-fix release covered in the same Fabric blog post as 1.21.2. No modding API changes.

---

## 1.21.3 → 1.21.4: Item model definitions, model loading rework

**Severity: MODERATE-HIGH.** The Garden Awakens update introduces a new JSON-based item model definition system and moves pick-block to the server.

### Item model definitions replace ItemColors

A new file at `assets/<namespace>/items/<item>.json` defines how items render:

```json
{
  "model": {
    "type": "minecraft:model",
    "model": "modid:item/my_item"
  }
}
```

Supports conditions, tints (`minecraft:constant`, potion-based, etc.). **`ItemColors` and all related tinting APIs were removed**—tints must be specified in item model definitions.

### Block entities always render block models

`BlockRenderType.ENTITYBLOCK_ANIMATED` removed. Remove any override returning it—block entities now always render the block model alongside the entity renderer.

### Pick block moved from client to server

`ClientPickBlockApplyCallback`, `ClientPickBlockCallback`, `ClientPickBlockGatherCallback` removed. Use new server-side events:

```java
PlayerPickItemEvents.BLOCK.register((player, pos, state, requestIncludeData) -> {
    if (state.isIn(MyTags.NOT_PICKABLE)) return ItemStack.EMPTY;
    return null; // default behavior
});
```

### Model loading API major rework

All `BeforeBake` and `AfterBake` events removed. `ModelResolver` removed. Use `ModelModifier.OnLoad` with wrapping via `WrapperUnbakedModel`/`WrapperGroupableModel`. `BuiltinItemRendererRegistry` removed; use `SpecialModelTypes.ID_MAPPER`.

### Data attachment syncing

New API for syncing attachment data to clients:

```java
public static final AttachmentType<Integer> THIRST = AttachmentRegistry.create(
    Identifier.of("modid", "thirst"),
    builder -> builder
        .initializer(() -> 20)
        .persistent(Codec.INT)
        .syncWith(PacketCodecs.VAR_INT, AttachmentSyncPredicate.targetOnly()));
```

### Client data generation

Mojang moved data generation classes to the client. Configure in Loom:

```groovy
fabricApi {
    configureDataGeneration {
        client = true
    }
}
```

### Yarn renames

`net/minecraft/data/client` → `net/minecraft/client/data`. All `xp` references → `experience`. `CherryLeavesBlock` → `ParticleLeavesBlock`. `EntityModelLoader` → `LoadedEntityModels`.

---

## 1.21.4 → 1.21.5: NBT system overhaul, Spring to Life

**Severity: HIGH.** The Spring to Life "game drop" overhauls the NBT API and deprecates HudRenderCallback.

### NBT getters return Optional

`NbtCompound` getter methods now return `Optional<T>` instead of raw values. Type-aware `contains()` and `NbtElement.NUMBER_TYPE` removed:

```java
// Old: int value = nbt.getInt("value");
// New: Optional<Integer> value = nbt.getInt("value");
//      int value = nbt.getInt("value", 1000); // with fallback
```

New codec-based put/get: `nbt.put("Id", Identifier.CODEC, id)` and `nbt.get("Id", Identifier.CODEC)`. `NbtList` can now contain mixed types. Typed arrays no longer implement `List`.

### HudRenderCallback deprecated

Replace with `HudLayerRegistrationCallback` for registering HUD layers with proper ordering relative to vanilla elements.

### Other changes

`DataPool` replaced with `Pool`. `TradeOfferHelper` takes `RegistryKey` of profession instead of `VillagerProfession`. `VillagerProfessionBuilder` and `VillagerTypeHelper` removed. New Fabric APIs added: Client Game Test API, tag aliases, registry aliasing, `RegistryAttribute#OPTIONAL`. Integer numbers in commands support hex (`0xCAFE`) and binary (`0b101`) prefixes.

---

## 1.21.5 → 1.21.6: Rendering pipeline overhaul, HUD rewrite

**Severity: VERY HIGH.** Chase the Skies overhauls the rendering pipeline, replaces BlockEntity NBT with ReadView/WriteView, and removes several Fabric API modules.

### Rendering pipeline overhaul

Chunk, GUI, and HUD rendering converted to a new separated rendering style. Many `RenderSystem` methods removed. New `RenderPipeline` + `RenderLayer` system. Core shader system changes with new `stars` and `sky` shaders. Fog system split into environmental (spherical) and render-distance (cylindrical). `FogShape` removed entirely.

### BlockEntity serialization: NBT → ReadView/WriteView

```java
// New pattern for block entity serialization
@Override
protected void writeData(WriteView view) {
    super.writeData(view);
    view.putInt("anInt", anInt);
    view.putNullable("extra", Extra.CODEC, this.extra);
}

@Override
protected void readData(ReadView view) {
    super.readData(view);
    view.getOptionalInt("anInt").ifPresent(i -> this.anInt = i);
    view.read("extra", Extra.CODEC).ifPresent(e -> this.extra = e);
}
```

### HUD API rewritten

```java
// New HUD registration pattern
HudElementRegistry.addLast(Identifier.of("example", "hud"), (context, tickCounter) -> {
    context.drawTextWithShadow(client.textRenderer, "Example", 10, 10, Colors.WHITE);
});
HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, id, callback);
```

### BlockRenderLayerMap API change

```java
// Old: BlockRenderLayerMap.INSTANCE.putBlock(block, RenderLayer.CUTOUT);
// New: BlockRenderLayerMap.putBlock(block, RenderLayer.CUTOUT);
```

### Fabric API module changes

Removed: `fabric-command-api-v1`, `fabric-commands-v0`, `fabric-keybindings-v0`, `fabric-rendering-data-attachment-v1`. Material API removed from Fabric Rendering API. `fabric-client-tags-api-v1` merged into `fabric-tag-api-v1`. New APIs: `ComponentTooltipAppenderRegistry`, `LootTableEvents.MODIFY_DROPS`, `ServerPlayerEvents.JOIN/LEAVE`, `FabricTrackedDataRegistry`. `getOrCreateTagBuilder` → `valueLookupBuilder` in data generation.

---

## 1.21.6 → 1.21.7 → 1.21.8: Hotfixes only

**Severity: NONE to MINIMAL.** 1.21.7 (Jun 30 2025) added a new painting and music disc and fixed GPU issues. 1.21.8 (Jul 17 2025) fixed Intel Gen11 GPU graphics corruption. No significant modding API changes. 1.21.8 is compatible with 1.21.7 servers. Fabric API for 1.21.8 added mob conversion support in AttachmentRegistry and extra ReadView/WriteView utility methods.

---

## 1.21.8 → 1.21.9: Queue-based rendering, keybinding overhaul

**Severity: VERY HIGH.** The Copper Age game drop completes the rendering overhaul and restructures the keybinding and resource loading systems.

### Rendering overhaul completed

Almost all world rendering reworked to use **`OrderedRenderCommandQueue`**. Block entities, particles, and entities all use the new queue-based rendering. `VertexConsumerProvider` no longer passed to block entity renderers:

```java
public void render(BlockEntityRenderState state, MatrixStack matrices,
                   OrderedRenderCommandQueue queue, CameraRenderState cameraRenderState) {
    queue.submitText(matrices, 0, 0, text, false,
                     TextRenderer.TextLayerType.NORMAL, state.lightmapCoordinates,
                     Colors.WHITE, 0, Colors.BLACK);
}
```

**World Render Events were removed** in 1.21.9 (reintroduced in 1.21.10).

### Keybinding system overhauled

```java
// Old: new KeyBinding("key.test.random", InputUtil.Type.KEYSYM, code, "key.category.test.main");
// New:
KeyBinding.Category TEST_CATEGORY = KeyBinding.Category.create(Identifier.of("test", "main"));
new KeyBinding("key.test.random", InputUtil.Type.KEYSYM, code, TEST_CATEGORY);
```

### Resource loader API reworked

```java
// Old: ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(reloader);
// New:
ResourceLoader.get(ResourceType.SERVER_DATA).registerReloader(
    Identifier.of("modid", "custom"), reloader);
```

Ordering uses `addReloaderOrdering()`. Registry access via shared `Store`: `store.getOrThrow(ResourceLoader.RELOADER_REGISTRY_LOOKUP_KEY)`.

### Entity#getWorld renamed

`Entity#getWorld` → `Entity#getEntityWorld`. MixinExtras 5.0.0 bundled with expressions support. Fabric Mixin 0.16.3. New debug text API: `DebugHudEntries.register()`.

---

## 1.21.9 → 1.21.10: Hotfix restoring World Render Events

**Severity: LOW.** Bug fixes (entity/piston clipping, wind charge collision, chunk loading during teleportation). **World Render Events reintroduced** after being removed in 1.21.9. Compatible with 1.21.9 servers.

---

## 1.21.10 → 1.21.11: The last obfuscated version

**Severity: MODERATE.** Mounts of Mayhem is the **final version using obfuscated code, the final version requiring Java 21, and the final version using the 1.x.y format**. After this, version 26.1 ships fully unobfuscated.

### Game rules registry overhaul

```java
public static final GameRule<Boolean> EXAMPLE = GameRuleBuilder.forBoolean(true)
    .buildAndRegister(Identifier.fromNamespaceAndPath("fabric", "example"));

GameRuleEvents.changeCallback(GameRules.FIRE_DAMAGE).register((value, server) -> {
    // React to change
});
```

### Networking: large packet support

```java
PayloadTypeRegistry.playS2C().registerLarge(YourPayload.ID, YourPayload.CODEC, DATA_SIZE);
```

### Recipe synchronization API

```java
RecipeSynchronization.synchronizeRecipeSerializer(YourRecipeSerializers.RECIPE_TYPE);
```

### Rendering additions

RGSS (Rotated Grid Super Sampling) shader-based filtering. Anisotropic texture filtering option. Chunk fade time option.

### Deobfuscation milestone

1.21.11 ships with both obfuscated and experimental unobfuscated variants. **Yarn mappings will NOT be updated after this version.** Modders must migrate to Mojang Mappings. Intermediary will no longer be needed. Loom gained a new plugin ID: `net.fabricmc.fabric-loom-remap` for 1.21.11, and post-1.21.11 versions use a plugin that skips remapping entirely. Gradle **9.2+** now required.

---

## Build configuration reference

### build.gradle for 1.20.1 (Java 17, Yarn)

```groovy
plugins {
    id 'fabric-loom' version '1.5-SNAPSHOT'
}
dependencies {
    minecraft "com.mojang:minecraft:1.20.1"
    mappings "net.fabricmc:yarn:1.20.1+build.10:v2"
    modImplementation "net.fabricmc:fabric-loader:0.15.11"
    modImplementation "net.fabricmc.fabric-api:fabric-api:0.92.2+1.20.1"
}
tasks.withType(JavaCompile).configureEach { it.options.release = 17 }
```

### build.gradle for 1.20.5/1.20.6 (Java 21, component era)

```groovy
plugins {
    id 'fabric-loom' version '1.6-SNAPSHOT'
}
dependencies {
    minecraft "com.mojang:minecraft:1.20.6"
    mappings "net.fabricmc:yarn:1.20.6+build.3:v2"
    modImplementation "net.fabricmc:fabric-loader:0.15.10"
    modImplementation "net.fabricmc.fabric-api:fabric-api:0.100.8+1.20.6"
}
tasks.withType(JavaCompile).configureEach { it.options.release = 21 }
```

### build.gradle for 1.21.4 (split source sets, Mojang mappings)

```groovy
plugins {
    id 'fabric-loom' version '1.9-SNAPSHOT'
}
loom {
    splitEnvironmentSourceSets()
    mods {
        "modid" {
            sourceSet sourceSets.main
            sourceSet sourceSets.client
        }
    }
}
dependencies {
    minecraft "com.mojang:minecraft:1.21.4"
    mappings loom.officialMojangMappings()  // or Yarn
    modImplementation "net.fabricmc:fabric-loader:0.16.9"
    modImplementation "net.fabricmc.fabric-api:fabric-api:0.114.0+1.21.4"
}
tasks.withType(JavaCompile).configureEach { it.options.release = 21 }
```

### build.gradle for 1.21.11 (final obfuscated, new plugin ID)

```groovy
plugins {
    id 'net.fabricmc.fabric-loom-remap' version "${loom_version}"  // 1.14
}
loom {
    splitEnvironmentSourceSets()
    mods {
        "modid" {
            sourceSet sourceSets.main
            sourceSet sourceSets.client
        }
    }
}
dependencies {
    minecraft "com.mojang:minecraft:1.21.11"
    mappings loom.officialMojangMappings()
    modImplementation "net.fabricmc:fabric-loader:0.18.1"
    modImplementation "net.fabricmc.fabric-api:fabric-api:0.141.3+1.21.11"
}
tasks.withType(JavaCompile).configureEach { it.options.release = 21 }
```

### fabric.mod.json (unchanged schema, version 1)

The schema has not changed across any version in this range. Always use `"schemaVersion": 1`. Key fields:

```json
{
  "schemaVersion": 1,
  "id": "my-mod",
  "version": "${version}",
  "entrypoints": {
    "main": ["com.example.MyMod"],
    "client": ["com.example.MyModClient"]
  },
  "mixins": ["my-mod.mixins.json"],
  "accessWidener": "my-mod.accesswidener",
  "depends": {
    "fabricloader": ">=0.18.0",
    "minecraft": "~1.21.11",
    "java": ">=21",
    "fabric-api": "*"
  }
}
```

Version range syntax: `>=1.0.0` (at least), `~1.21` (>=1.21.0 <1.22.0), `^1.21` (>=1.21.0 <2.0.0), `*` (any). Arrays express OR: `[">=1.0", "2.5.0"]`.

Entry points (`ModInitializer`, `ClientModInitializer`, `DedicatedServerModInitializer`) and access widener syntax (`accessWidener v2 named`) have **not changed** across any version. Mixin config `compatibilityLevel` should be `JAVA_17` for ≤1.20.4 and `JAVA_21` for ≥1.20.5.

---

## Cumulative mixin target changes

Mixins are the most fragile part of any Fabric mod during version transitions. Below are the most impactful class and method changes that break mixin targets:

- **1.20.2**: `ServerCommonPacketListener` introduced as parent type. Session/reporting/telemetry classes moved to `client.session`.
- **1.20.3**: `ClickableWidget#render` made final (target `renderWidget` instead). `GravelBlock`, `SandBlock`, `AbstractGlassBlock`, `GlassBlock`, `FernBlock`, `PowderSnowCauldronBlock` removed. `Text.Serializer` split.
- **1.20.5**: All `AbstractBlock` overridable methods → `protected`. `ItemStack` NBT methods removed. `FabricPacket` removed. `PortalInfo` removed. Dozens of entity methods renamed/removed.
- **1.21**: `Identifier` constructor → `protected` (breaks constructor mixins). Vertex consumer methods renamed (`vertex`→`addVertex`, `color`→`setColor`). `BufferVertexConsumer`, `DefaultedVertexConsumer` removed. `RecordItem` class removed.
- **1.21.2**: `Registry` methods mass-renamed (7+ methods). `Entity#damage` split into server/client variants. Entity attribute names lost `GENERIC_` prefix. `ActionResult` types merged. Renderer classes gain `EntityRenderState` parameter.
- **1.21.4**: `ItemColors` removed. `BlockRenderType.ENTITYBLOCK_ANIMATED` removed. Pick block callbacks removed. Model loading events restructured.
- **1.21.5**: `NbtCompound` methods return `Optional` instead of raw values. `DataPool` → `Pool`.
- **1.21.6**: BlockEntity `readNbt`/`writeNbt` → `readData(ReadView)`/`writeData(WriteView)`. `RenderSystem` methods removed. `FogShape` removed. `fabric-command-api-v1` removed.
- **1.21.9**: `Entity#getWorld` → `Entity#getEntityWorld`. `ResourceManagerHelper` → `ResourceLoader`. KeyBinding constructor changed. Block entity renderers take `OrderedRenderCommandQueue` instead of `VertexConsumerProvider`.

---

## Breaking change severity index

| Transition | Severity | Key Issue |
|---|---|---|
| 1.20.1 → 1.20.2 | Moderate | Configuration networking, recipe codecs |
| 1.20.2 → 1.20.3 | Moderate-High | Text NBT wire format, block codecs, widget changes |
| 1.20.3 → 1.20.4 | None | Single bug fix |
| **1.20.4 → 1.20.5** | **Critical** | **Components replace NBT, networking rewrite, Java 21** |
| 1.20.5 → 1.20.6 | None | Single bug fix |
| 1.20.6 → 1.21 | High | Identifier API, data-driven enchantments, vertex overhaul |
| 1.21 → 1.21.1 | Minimal | Bug fixes |
| **1.21.1 → 1.21.2** | **Very High** | **RegistryKeys required, EntityRenderState, recipe rewrite, ActionResult merge** |
| 1.21.2 → 1.21.3 | None | Bug fix |
| 1.21.3 → 1.21.4 | Moderate-High | Item model definitions, model loading rework, pick block moved |
| 1.21.4 → 1.21.5 | High | NBT Optional returns, HUD deprecation |
| **1.21.5 → 1.21.6** | **Very High** | **Rendering pipeline overhaul, ReadView/WriteView, module removals** |
| 1.21.6 → 1.21.7 | None | GPU bug fixes |
| 1.21.7 → 1.21.8 | None | GPU bug fixes |
| **1.21.8 → 1.21.9** | **Very High** | **Queue-based rendering, keybinding overhaul, resource loader rework** |
| 1.21.9 → 1.21.10 | Low | World Render Events restored |
| 1.21.10 → 1.21.11 | Moderate | Game rules overhaul, last obfuscated version |

The four most painful transitions are **1.20.4→1.20.5** (components), **1.21.1→1.21.2** (registry keys + render states), **1.21.5→1.21.6** (rendering + serialization), and **1.21.8→1.21.9** (rendering completion + keybindings). Plan significant development time for each.

## Conclusion

The 1.20.1–1.21.11 era represents Minecraft's most aggressive modernization period. Three architectural shifts define it: **the component system** (1.20.5) replacing NBT with typed, immutable key-value pairs; **the rendering pipeline rewrite** (1.21.2 through 1.21.9) separating state extraction from rendering; and **the data-driven push** moving enchantments (1.21), recipes (1.21.2), and item models (1.21.4) into JSON. The choice of 1.21.11 as the final obfuscated version makes it a natural checkpoint—after this, Yarn mappings are no longer maintained, Intermediary is unnecessary, and the entire Fabric toolchain simplifies dramatically. Developers targeting this range should treat 1.20.5, 1.21.2, 1.21.6, and 1.21.9 as the major migration barriers and budget accordingly.