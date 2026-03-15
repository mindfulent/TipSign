# MOD-REFERENCE.md — Fabric Modding API Changes, 1.20 Through 1.21.11

**This reference tracks every breaking Minecraft modding API change from 1.20 through 1.21.11, using exclusively Mojang (Mojmap) class names, method signatures, and package paths.** It is designed for Fabric mod developers who need to maintain or port mods across version bands. Hotfix versions (1.20.1, 1.20.4, 1.20.6, 1.21.1, 1.21.3, 1.21.7, 1.21.8, 1.21.10) are noted but share the API surface of their parent release unless otherwise stated.

> **Mojmap naming convention used throughout.** The Yarn equivalent is noted in parentheses only on first use. For a full mapping table, see the appendix at the end.

---

## 1.20 / 1.20.1 — Trails & Tales (June 2023)

**Java 17. Fabric Loader 0.14.19+. Loom 1.2.**

### Material class removed, GuiGraphics introduced

The `Material` class was deleted entirely. Block properties like map color and piston behavior moved to `BlockBehaviour.Properties` (Yarn: `AbstractBlock.Settings`):

```java
// Old
new Block(BlockBehaviour.Properties.of(Material.PLANT, MapColor.GREEN))
// New
new Block(BlockBehaviour.Properties.of().mapColor(MapColor.GREEN).pushReaction(PushReaction.DESTROY))
```

**`Entity.level` field became private** — all access now through the `Entity.level()` getter method.

The new **`GuiGraphics`** (Yarn: `DrawContext`) object replaced scattered render utilities and `PoseStack` (Yarn: `MatrixStack`) in all GUI render methods. HUD and Screen callbacks now receive `GuiGraphics` instead of `PoseStack`. Key methods: `guiGraphics.blit()`, `drawString()`, `renderItem()`, `renderTooltip()`.

**`CreativeModeTab`** (Yarn: `ItemGroup`) moved to a registry: `BuiltInRegistries.CREATIVE_MODE_TAB`. `CommandSourceStack.sendSuccess()` changed to accept `Supplier<Component>` instead of `Component` (Yarn: `Text`).

### ItemStack and loot system changes

`ItemStack.copyWithCount(int)` replaced the `copy()` + `setCount()` pattern. `LootContext.Builder` became `LootParams.Builder`. Loot predicates moved to `LootDataManager` identified via `LootDataId`.

### Fabric API specifics

New packet-based networking API added alongside legacy API. `FabricMaterialBuilder` removed (matching vanilla `Material` removal). Renderer API dropped `spriteIndex` parameter; `disableAo` became `ambientOcclusion(TriState)`.

---

## 1.20.2 — Configuration networking stage (September 2023)

**Fabric Loader 0.14.22+. Loom 1.3.**

### Network configuration phase

A new **configuration stage** between login and play was added. `ServerConfigurationPacketListenerImpl` handles config tasks before the player enters the world. Mods using `ServerLoginPacketListener` should migrate to configuration-stage networking.

### Recipes and advancements became records

`RecipeHolder` (Yarn: `RecipeEntry`) and `AdvancementHolder` (Yarn: `AdvancementEntry`) now wrap ID + value. Recipes serialize via `Codec`s. In data generation, `RecipeOutput` (Yarn: `RecipeExporter`) replaced `Consumer<FinishedRecipe>`.

**GUI changes**: `AbstractWidget.render()` became `final` — override `renderWidget()` instead. `Screen.renderBackground()` now takes mouse position and `partialTick`.

---

## 1.20.3 / 1.20.4 — Component serialization overhaul (December 2023)

**Fabric Loader 0.14.25+. Loom 1.4.**

### Component (Text) goes Codec-based

`Component` (Yarn: `Text`) now serializes via `Codec` and transmits over the network as NBT rather than JSON. **`Component.translatable()` now requires arguments to be numbers, booleans, strings, or other `Component` instances** — passing `BlockPos`, `ResourceLocation` (Yarn: `Identifier`), etc. directly will fail silently.

`TickRateManager` (Yarn: `TickManager`) introduced for the `/tick` command. `BlockBehaviour.randomTick()` no longer calls `tick()`. `SaplingBlock` generators consolidated into `TreeGrower`.

---

## 1.20.5 / 1.20.6 — Data Components revolution (April 2024)

**Java 21 required. Fabric Loader 0.15.10+. Loom 1.6. This is the most breaking dot-release in Minecraft's history.**

### Data Components replace NBT on ItemStack

`ItemStack` no longer stores a `CompoundTag` (Yarn: `NbtCompound`). Instead, all item data lives in **`DataComponentType<T>`** keys within a `DataComponentMap`:

```java
@Nullable Component name = stack.get(DataComponents.CUSTOM_NAME);
stack.set(DataComponents.CUSTOM_NAME, Component.literal("Hello"));
stack.remove(DataComponents.CUSTOM_NAME);

// Custom fallback: DataComponents.CUSTOM_DATA wraps a CompoundTag
CustomData data = stack.get(DataComponents.CUSTOM_DATA);
```

Custom components register via `DataComponentType.<T>builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT).build()` into `BuiltInRegistries.DATA_COMPONENT_TYPE`. Component values must be treated as **immutable**.

### StreamCodec replaces manual buffer read/write

`StreamCodec<B, T>` (Yarn: `PacketCodec`) replaces manual `FriendlyByteBuf` (Yarn: `PacketByteBuf`) serialization for networking. `RegistryFriendlyByteBuf` (Yarn: `RegistryByteBuf`) extends `FriendlyByteBuf` for PLAY-phase packets.

### Networking API overhaul

`FabricPacket` removed. Packets now implement vanilla's `CustomPacketPayload` (Yarn: `CustomPayload`):

```java
public record SlapPacket(UUID slapped) implements CustomPacketPayload {
    public static final Type<SlapPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("mymod", "slap"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SlapPacket> STREAM_CODEC = ...;
}
// Register: PayloadTypeRegistry.playC2S().register(SlapPacket.TYPE, SlapPacket.STREAM_CODEC);
// Receive: ServerPlayNetworking.registerGlobalReceiver(SlapPacket.TYPE, (payload, context) -> { ... });
```

### BlockEntity gains HolderLookup.Provider

```java
// Before (1.20.4):
protected void saveAdditional(CompoundTag tag) { ... }
protected void load(CompoundTag tag) { ... }

// After (1.20.5+):
protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) { ... }
protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) { ... }
```

The `load()` method was renamed to **`loadAdditional()`** and both methods now take `HolderLookup.Provider` (Yarn: `RegistryWrapper.WrapperLookup`).

### Block methods made protected

All `BlockBehaviour` public overridable methods became `protected`. New split: `useWithoutItem()` for non-item interaction, `useItemOn()` returning `ItemInteractionResult` for item-specific interaction.

### Holder<T> wrapping everywhere

`MobEffect`, `Attribute`, `ArmorMaterial`, `GameEvent`, `ChatType` — all wrapped in `Holder<T>` (Yarn: `RegistryEntry<T>`). Loot tables became "reloadable registries" identified by `ResourceKey<LootTable>`. `BootstapContext` renamed to `BootstrapContext`.

### Rendering: ARGB replaces RGB

`DyedItemColor` and item/block color providers now use ARGB format. Wrap colors with `ARGB.opaque()` to avoid transparent items on the translucent render layer.

---

## 1.21 / 1.21.1 — Tricky Trials (June 2024)

**Fabric Loader 0.15.11+. Loom 1.6.**

### ResourceLocation constructor privatized

```java
// Old
new ResourceLocation("mymod", "my_item")
// New
ResourceLocation.fromNamespaceAndPath("mymod", "my_item")
ResourceLocation.parse("mymod:my_item")
ResourceLocation.withDefaultNamespace("creeper")
```

### Data pack paths depluralized

All folder names became singular: `tags/blocks` → `tags/block`, `recipes` → `recipe`, `loot_tables` → `loot_table`, `structures` → `structure`, etc.

### Enchantments became data-driven

`Enchantment` moved to a datapack registry. References use `Holder<Enchantment>` queried via `HolderLookup.Provider`. Custom enchantment effects register via `EnchantmentEffectComponentTypes`. Direct enchantment checks replaced by `EnchantmentHelper` methods like `getDamage()`, `getProtectionAmount()`, `hasAnyEnchantmentsIn()`.

### Vertex system overhaul

```java
// Old
builder.vertex(...).texture(...).color(...).next();
// New (Mojmap)
builder.addVertex(x, y, z).setUv(u, v).setColor(color);
// endVertex() removed — automatic on next addVertex or upload
```

`BufferBuilder.RenderedBuffer` → `MeshData`. `RenderType.end()` → `RenderType.draw()`. `Model.renderToBuffer()` now takes `int` ARGB tint instead of four floats.

### Fabric API specifics

`FabricDimensions.teleport()` removed — use vanilla's `Entity.teleportTo()` with `TeleportTarget`. `HudRenderCallback` passes `RenderTickCounter` instead of `tickDelta`.

---

## 1.21.2 / 1.21.3 — Bundles of Bravery (October 2024)

**Fabric Loader 0.16.7+. Loom 1.8. (1.21.3 is a hotfix with zero API changes.)**

### EntityRenderer gains two type parameters

Entity renderers adopted the **render state pattern** — data is extracted into a state object, then rendered from that state. This applies to **entity renderers only** (not block entity renderers yet):

```java
public class MyEntityRenderer extends EntityRenderer<MyEntity, MyEntityRenderState> {
    public MyEntityRenderState createRenderState() { return new MyEntityRenderState(); }
    public void updateRenderState(MyEntity entity, MyEntityRenderState state, float partialTick) {
        super.updateRenderState(entity, state, partialTick);
        state.isSaddled = entity.isSaddled();
    }
    public void render(MyEntityRenderState state, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) { ... }
}
```

### Item and Block properties require registry keys

```java
ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("mymod", "test_item"));
Item.Properties props = new Item.Properties().setId(key); // REQUIRED — NPE without this
Registry.register(BuiltInRegistries.ITEM, key, new Item(props));
```

Same for blocks via `BlockBehaviour.Properties.of().setId(blockKey)`.

### InteractionResult types merged

`InteractionResult`, `InteractionResultHolder`, and `ItemInteractionResult` collapsed into a single **`InteractionResult`**. `sidedSuccess(isClient)` → `SUCCESS`. Use `InteractionResult.withNewHandStack(newStack)` when replacing the held item.

### Registry method renames

`Registry.getHolder()` → `get()`, `getHolderOrThrow()` → `getOrThrow()`, `getOrThrow()` (value) → `getValueOrThrow()`. `RegistryAccess.registryOrThrow()` → `getOrThrow()`. Attribute names dropped their prefix: `Attributes.GENERIC_ATTACK_KNOCKBACK` → `Attributes.ATTACK_KNOCKBACK`.

### GUI rendering changes

`GuiGraphics.blit()` methods now take `Function<ResourceLocation, RenderType>` and require PNG size parameters:

```java
graphics.blit(RenderType::guiTextured, texture, x, y, u, v, w, h, texW, texH);
```

`GuiGraphics.setColor()` and `drawManaged()` removed. `CoreShaderRegistrationCallback` removed.

### Recipes became server-only

Clients receive `RecipeDisplayEntry` instead of full recipe data. `Ingredient` is now internally a list of items. Data gen: `RecipeProvider` overrides `getRecipeGenerator()`.

---

## 1.21.4 — The Garden Awakens (December 2024)

**Fabric Loader 0.16.9+. Loom 1.9.**

### New item model definition system

Items now use `assets/<namespace>/items/<path>.json` instead of `models/item/<path>.json`:

```json
{
    "model": {
        "type": "minecraft:model",
        "model": "mymod:item/my_item",
        "tints": [{ "type": "minecraft:constant", "value": 65280 }]
    }
}
```

`ItemColors` and related tinting APIs **removed** — tints now live in the `tints` array of item model definitions. Types include `minecraft:model`, `minecraft:range_dispatch`, `minecraft:select`, `minecraft:condition`, `minecraft:composite`.

### Client data generation split

Data generation classes in `net.minecraft.data.client` moved to `net.minecraft.client.data` (client-only). Loom 1.9 added `fabricApi { configureDataGeneration { client = true } }`.

### Model loading API restructured

`UnbakedModel` and `GroupableModel` split. All `BeforeBake`/`AfterBake` model events removed in favor of `OnLoad` events with wrapper pattern. FRAPI's `WrapperBakedModel` → `UnwrappableBakedModel`.

---

## 1.21.5 — Spring to Life (March 2025)

**Fabric Loader 0.16.10+. Loom 1.10. This version brings the most sweeping NBT changes since Data Components.**

### CompoundTag API overhaul

The `Tag` hierarchy was sealed and finalized. `CompoundTag` became `final`. `ByteTag`, `IntTag`, `StringTag`, etc. became **records**. `CollectionTag` became a sealed interface.

**All getter methods now return `Optional<T>`:**

```java
// Before (1.21.4):
int val = tag.getInt("key");       // raw int, 0 if missing
String s = tag.getString("key");   // raw String, "" if missing

// After (1.21.5):
Optional<Integer> val = tag.getInt("key");
int valRaw = tag.getIntOr("key", 0);             // explicit default
Optional<String> s = tag.getString("key");
String sRaw = tag.getStringOr("key", "default");  // explicit default
```

The pattern is consistent: **`get*(key)` returns `Optional<T>`**, while **`get*Or(key, default)` returns the raw type with a fallback**.

### Removed CompoundTag methods

| Removed method | Replacement |
|---|---|
| `putUUID(String, UUID)` | `tag.store("key", UUIDUtil.CODEC, uuid)` |
| `getUUID(String)` | `tag.read("key", UUIDUtil.CODEC)` |
| `hasUUID(String)` | No direct replacement |
| `contains(String)` | Removed entirely |
| `contains(String, int)` | Removed entirely (no type-checked variant) |
| `getTagType(String)` | Removed |
| `getList(String, int)` | `tag.getListOrEmpty("key")` — no type parameter |
| `getAllKeys()` | `tag.keySet()` |

### New Codec integration on CompoundTag

```java
tag.store("key", MY_CODEC, value);           // write via Codec
tag.storeNullable("key", MY_CODEC, nullableValue); // writes nothing if null
Optional<T> val = tag.read("key", MY_CODEC); // read via Codec
tag.store(MY_MAP_CODEC, value);              // MapCodec — merges onto root
```

**`storeNullable()` was new in 1.21.5** — there was no prior `putNullable()` method it replaced.

### Weapons, tools, and armor became data components

`SwordItem`, `DiggerItem`, `PickaxeItem`, `ArmorItem`, `AnimalArmorItem`, `SaddleItem` — all **removed**. Replaced by data components: `DataComponents.WEAPON`, `DataComponents.TOOL`, `DataComponents.ARMOR`, `DataComponents.BLOCKS_ATTACKS`. New `Item.Properties` methods: `.sword()`, `.pickaxe()`, `.axe()`, `.hoe()`, `.shovel()`.

### BlockEntityRenderer gains 7th parameter

```java
// Before (≤1.21.4): 6 parameters
void render(T blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource bufferSource, int packedLight, int packedOverlay);

// After (1.21.5): 7 parameters — Vec3 cameraPos added
void render(T blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource bufferSource, int packedLight, int packedOverlay,
            Vec3 cameraPos);
```

**`BlockEntityRenderer` still has only one type parameter** (`<T extends BlockEntity>`) in 1.21.5. The render state system for block entities arrives later in 1.21.9.

### Other changes

`BlockBehaviour.onRemove()` split into `BlockEntity.preRemoveSideEffects()` + `BlockBehaviour.affectNeighborsAfterRemoval()`. `DataComponents.TOOLTIP_DISPLAY` replaced `HIDE_ADDITIONAL_TOOLTIP` and `HIDE_TOOLTIP`. `Item.inventoryTick()` signature changed to `(ItemStack, ServerLevel, Entity, EquipmentSlot)`.

---

## 1.21.6 / 1.21.7 / 1.21.8 — Chase the Skies (June–July 2025)

**Fabric Loader 0.16.14+. (1.21.7 and 1.21.8 are hotfixes with no significant API changes.)**

### ValueInput / ValueOutput replace CompoundTag in serialization

The most significant architectural change in 1.21.6: a new serialization abstraction layer decouples entity and block entity persistence from the concrete `CompoundTag` type.

**Package: `net.minecraft.world.level.storage`**

```java
// Before (1.21.5):
protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) { ... }
protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) { ... }

// After (1.21.6):
protected void saveAdditional(ValueOutput output) { ... }
protected void loadAdditional(ValueInput input) { ... }
```

The `HolderLookup.Provider` is now embedded within the `ValueInput`/`ValueOutput` instance. Entity serialization changed identically: `addAdditionalSaveData(ValueOutput)` and `readAdditionalSaveData(ValueInput)`.

### ValueInput key methods

```java
Optional<Integer> val = input.getInt("key");
int valRaw = input.getIntOr("key", 0);
Optional<String> s = input.getString("key");
String sRaw = input.getStringOr("key", "default");
boolean b = input.getBooleanOr("key", false);
Optional<T> obj = input.read("key", MY_CODEC);
ValueInput nested = input.childOrEmpty("subobject");
ValueInput.ValueInputList list = input.childrenListOrEmpty("items");
```

### ValueOutput key methods

```java
output.putInt("key", 42);
output.putString("key", "hello");
output.putBoolean("key", true);
output.store("key", MY_CODEC, value);
output.storeNullable("key", MY_CODEC, nullableValue);
ValueOutput nested = output.child("subobject");
output.discard("key"); // remove a key
```

### Bridging to CompoundTag when needed

```java
// Writing to CompoundTag via ValueOutput
TagValueOutput out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registries);
// ... write data ...
CompoundTag tag = out.buildResult();

// Reading from CompoundTag via ValueInput
TagValueInput in = TagValueInput.create(reporter, registries, tag);
// ... read data ...
```

`TagValueOutput` and `TagValueInput` are the NBT-specific implementations, located in `net.minecraft.nbt`.

### Entity rendering moves to SubmitNodeCollector

In 1.21.6, `EntityRenderer`s (which already used the render state pattern from 1.21.2) now submit to **`SubmitNodeCollector`** instead of `MultiBufferSource` (Yarn: `VertexConsumerProvider`). Many `RenderSystem` methods removed; capabilities exist via `RenderPipeline`s.

### GUI rendering overhaul

A two-phase rendering system replaced immediate drawing: "prepare" (submitting to `GuiRenderState`) and "render" (executed by `GuiRenderer`). **`GuiGraphics.drawString()` now requires alpha in color** — `0xFFFFFF` renders invisible; use `0xFFFFFFFF`. Sprites now use `RenderPipelines.GUI_TEXTURE` instead of `RenderType::guiTextured`.

### Fabric API specifics

Material API removed from Fabric Rendering API. `BlockRenderLayerMap` relocated to `net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap` with static methods (no more `INSTANCE`). HUD API rewritten around `HudElementRegistry`. Several deprecated modules removed including `fabric-command-api-v1`.

---

## 1.21.9 / 1.21.10 — The Copper Age (September–October 2025)

**Fabric Loader 0.17.2+. Loom 1.11. (1.21.10 is a hotfix with minimal API changes.)**

### BlockEntityRenderer adopts two-type-parameter render state system

This is the **most significant rendering change for block entity modders**. The pattern that entity renderers adopted in 1.21.2 now extends to block entity renderers:

```java
// Before (1.21.5–1.21.8):
public class MyBER implements BlockEntityRenderer<MyBlockEntity> {
    public void render(MyBlockEntity be, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight,
                       int packedOverlay, Vec3 cameraPos) { ... }
}

// After (1.21.9+):
public class MyBER implements BlockEntityRenderer<MyBlockEntity, MyBlockEntityRenderState> {

    @Override
    public MyBlockEntityRenderState createRenderState() {
        return new MyBlockEntityRenderState();
    }

    @Override
    public void extractRenderState(MyBlockEntity blockEntity, MyBlockEntityRenderState state,
                                    float partialTick, Vec3 cameraPos,
                                    @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        super.extractRenderState(blockEntity, state, partialTick, cameraPos, crumblingOverlay);
        state.facing = blockEntity.getFacing();
    }

    @Override
    public void submit(MyBlockEntityRenderState state, PoseStack poseStack,
                       SubmitNodeCollector collector, CameraRenderState cameraState) {
        // Render from state — NEVER access the block entity directly here
    }
}
```

### Key BER changes summarized

- **Two generic type parameters**: `BlockEntityRenderer<T extends BlockEntity, S extends BlockEntityRenderState>`
- **`render()` renamed to `submit()`**
- **`MultiBufferSource` replaced by `SubmitNodeCollector`** (Yarn: `OrderedRenderCommandQueue`)
- **`packedLight` and `packedOverlay` removed from submit()** — available via `BlockEntityRenderState.lightCoords`
- **`createRenderState()` + `extractRenderState()`** — the extraction phase copies data from the live block entity into a render state object
- **`BlockEntityRenderState`** base class contains: position, block state, block entity type, light coordinates, crumbling overlay

### BlockEntityRendererProvider.Context became a record

Accessor methods lost their `get` prefix:

```java
// Before (1.21.8):
Font font = context.getFont();
// After (1.21.9):
Font font = context.font();  // record-style accessor
context.itemModelResolver();  // also record-style
```

**`Font`** is the Mojmap name (Yarn: `TextRenderer`).

### SubmitNodeCollector API

The collector provides submit methods for various render primitives:

```java
collector.submitText(poseStack, x, y, text, dropShadow, Font.DisplayMode.NORMAL,
                     packedLight, color, backgroundColor, outlineColor);
collector.submitModel(poseStack, model, ...);
collector.submitModelPart(poseStack, modelPart, ...);
collector.submitItem(poseStack, itemRenderState, ...);
```

### Particle rendering rework

`ParticleGroup` system introduced. `ParticleEngine` calls `ParticleGroup.extractRenderState()`. Particles now use `ParticleGroupRenderState` with `submit()` and `clear()`.

### Fabric API specifics

Resource Loader API v1 major rework: `ResourceLoader.get()` replaces `ResourceManagerHelper.get()`. World render events removed (replacement pending). `DebugHudEntries` for F3 screen. MixinExtras 5.0.0 bundled. Screen key events consolidated into `KeyInput` context object.

---

## 1.21.11 — Mounts of Mayhem (December 2025)

**The last obfuscated Minecraft version. Starting with 26.1, Minecraft ships unobfuscated.**

### Mojang renames ResourceLocation to Identifier

In a move preparing for deobfuscation, Mojang renamed **`ResourceLocation` to `Identifier`** in their own mappings — matching what Yarn had used for years. Many utility classes moved to `net.minecraft.util`. `net.minecraft.advancements.critereon` → `net.minecraft.advancements.criterion`.

### JSpecify nullability annotations

`javax.annotation.Nullable` → `org.jspecify.annotations.Nullable`. All packages marked `@NullMarked`. JSpecify annotations are type-use only: `Map.@Nullable Entry` (not `@Nullable Map.Entry`).

### Game rules overhauled

`GameRule` class with registry-based system (`BuiltInRegistries.GAME_RULE`) replaced the old `GameRules.Type` / `GameRules.Key` system.

### Fabric API

API names updated to align with Mojang's official names for 26.1 compatibility. Dimension modification events added. New block/item interaction events: `BlockEvents.USE_ITEM_ON`, `BlockEvents.USE_WITHOUT_ITEM`, `ItemEvents.USE_ON`, `ItemEvents.USE`.

---

## Cross-version API evolution tables

### CompoundTag API timeline

| API surface | 1.20–1.20.4 | 1.20.5–1.21.4 | 1.21.5+ |
|---|---|---|---|
| `getInt("key")` | Returns `int` (0 if missing) | Returns `int` (0 if missing) | Returns `Optional<Integer>` |
| `getString("key")` | Returns `String` ("" if missing) | Returns `String` ("" if missing) | Returns `Optional<String>` |
| Explicit default | N/A | N/A | `getIntOr("key", 0)`, `getStringOr("key", "")` |
| `putUUID()` / `getUUID()` | Present | Present | **Removed** — use `tag.store("key", UUIDUtil.CODEC, uuid)` |
| `contains(String, int)` | Present | Present | **Removed** |
| `getList(String, int)` | Present (type param required) | Present (type param required) | `getListOrEmpty("key")` — **no type param** |
| `getAllKeys()` | Present | Present | Renamed to **`keySet()`** |
| Codec integration | None | None | `store()`, `storeNullable()`, `read()` |

### BlockEntity serialization timeline

| Version band | Save method | Load method |
|---|---|---|
| 1.20–1.20.4 | `saveAdditional(CompoundTag)` | `load(CompoundTag)` |
| 1.20.5–1.21.5 | `saveAdditional(CompoundTag, HolderLookup.Provider)` | `loadAdditional(CompoundTag, HolderLookup.Provider)` |
| 1.21.6+ | `saveAdditional(ValueOutput)` | `loadAdditional(ValueInput)` |

### BlockEntityRenderer signature timeline

| Version band | Interface | Main method | Parameters |
|---|---|---|---|
| 1.20–1.21.4 | `BlockEntityRenderer<T>` | `render()` | `T, float, PoseStack, MultiBufferSource, int, int` |
| 1.21.5–1.21.8 | `BlockEntityRenderer<T>` | `render()` | `T, float, PoseStack, MultiBufferSource, int, int, Vec3` |
| 1.21.9+ | `BlockEntityRenderer<T, S>` | **`submit()`** | `S, PoseStack, SubmitNodeCollector, CameraRenderState` |

### EntityRenderer timeline

| Version band | Interface | Render state? |
|---|---|---|
| 1.20–1.21.1 | `EntityRenderer<T>` | No — render directly from entity |
| 1.21.2+ | `EntityRenderer<T, S extends EntityRenderState>` | Yes — `createRenderState()`, `updateRenderState()`, `render()` |
| 1.21.6+ | Same two-param | Yes, but now submits to `SubmitNodeCollector` |

---

## Mojang ↔ Yarn mapping reference

| Mojang (Mojmap) | Yarn | First relevant version |
|---|---|---|
| `PoseStack` | `MatrixStack` | All |
| `Component` | `Text` | All |
| `ResourceLocation` | `Identifier` | All (Mojang renamed to `Identifier` in 1.21.11) |
| `Level` / `ServerLevel` / `ClientLevel` | `World` / `ServerWorld` / `ClientWorld` | All |
| `CompoundTag` | `NbtCompound` | All |
| `MultiBufferSource` | `VertexConsumerProvider` | All |
| `RenderType` | `RenderLayer` | All |
| `GuiGraphics` | `DrawContext` | 1.20+ |
| `BlockBehaviour.Properties` | `AbstractBlock.Settings` | All |
| `Item.Properties` | `Item.Settings` | All |
| `FriendlyByteBuf` | `PacketByteBuf` | All |
| `RegistryFriendlyByteBuf` | `RegistryByteBuf` | 1.20.5+ |
| `StreamCodec` | `PacketCodec` | 1.20.5+ |
| `ByteBufCodecs` | `PacketCodecs` | 1.20.5+ |
| `CustomPacketPayload` | `CustomPayload` | 1.20.5+ |
| `DataComponents` | `DataComponentTypes` | 1.20.5+ |
| `HolderLookup.Provider` | `RegistryWrapper.WrapperLookup` | 1.20.5+ |
| `Holder<T>` | `RegistryEntry<T>` | All |
| `ResourceKey<T>` | `RegistryKey<T>` | All |
| `BuiltInRegistries` | `Registries` | All |
| `InteractionResult` | `ActionResult` | All |
| `CommandSourceStack` | `ServerCommandSource` | All |
| `MobCategory` | `SpawnGroup` | All |
| `ChatFormatting` | `Formatting` | All |
| `Containers` | `ItemScatterer` | All |
| `TickRateManager` | `TickManager` | 1.20.3+ |
| `Font` | `TextRenderer` | All |
| `Vec3` | `Vec3d` | All |
| `SubmitNodeCollector` | `OrderedRenderCommandQueue` | 1.21.6+ |
| `ValueInput` | `ReadView` | 1.21.6+ |
| `ValueOutput` | `WriteView` | 1.21.6+ |
| `BlockEntityRenderState` | `BlockEntityRenderState` | 1.21.9+ |

---

## Corrections to common misconceptions

Several claims circulating in community documentation have been verified against decompiled source and migration primers. The following corrections apply:

**ValueInput/ValueOutput were introduced in 1.21.6, not earlier.** The package is confirmed as **`net.minecraft.world.level.storage`** via reflection mapping analysis of the Item-NBT-API project. The NBT-specific implementations `TagValueInput` and `TagValueOutput` reside in `net.minecraft.nbt`.

**CompoundTag's Optional-returning getters arrived in 1.21.5, not 1.21.6.** The entire CompoundTag API overhaul (Optional returns, removal of `putUUID`/`getUUID`/`hasUUID`, removal of `contains()`, removal of the type parameter from `getList()`, `getAllKeys()` → `keySet()`, Codec integration via `store()`/`read()`) all landed in **1.21.5**.

**The BER's 7th `Vec3 cameraPos` parameter was added in 1.21.5, not 1.21.6.** Yarn API docs for 1.21.4 confirm 6 parameters; 1.21.5 confirms 7. The BER remained single-type-parameter through 1.21.8.

**`getOptionalInt()` / `getOptionalString()` never existed as method names in Mojmap.** The existing `getInt()` and `getString()` methods changed their return type from primitive to `Optional<T>` in 1.21.5. The `getIntOr()`/`getStringOr()` convenience methods were added simultaneously.

**`putNullable()` never existed.** `storeNullable()` was a new method introduced in 1.21.5 as part of the Codec integration on CompoundTag.

**authlib 7.x / GameProfile becoming a record: Unverified.** No NeoForge migration primer, Fabric blog post, or documentation source through 1.21.11 documents `GameProfile` becoming a Java record or authlib upgrading to version 7.x. `GameProfile` appears to remain a standard class with `getName()` and `getId()` through 1.21.11. This change, if real, may apply to version 26.1 or later.

**ServerPlayer.server becoming private / Entity.getServer() removal: Unverified through 1.21.11.** No migration primer documents `ServerPlayer.server` changing access level or `Entity.getServer()` being removed. The field appears to remain `public final MinecraftServer server` through available documentation. The recommended access pattern `player.level().getServer()` works in all versions but may not yet be strictly required.

---

## Conclusion

The Minecraft modding API underwent three seismic shifts in this version range. **1.20.5** replaced NBT-based item data with typed Data Components and introduced StreamCodec networking — the largest single breaking change in Minecraft's history. **1.21.5** sealed the NBT type hierarchy, made all CompoundTag getters return `Optional`, and added first-class Codec integration to tags. **1.21.6** then abstracted serialization entirely behind `ValueInput`/`ValueOutput`, completing the decoupling of persistence logic from concrete NBT types.

On the rendering side, the transition from immediate-mode to state-based rendering happened in two waves: entity renderers adopted `EntityRenderState` in **1.21.2**, while block entity renderers followed in **1.21.9** with the `BlockEntityRenderState` + `submit()` + `SubmitNodeCollector` pattern. The elimination of `MultiBufferSource` from BER code and the separation of extraction from rendering enables Mojang's long-term goal of parallel frame extraction and rendering.

With **1.21.11** being the last obfuscated version and Mojang renaming `ResourceLocation` to `Identifier` in their own mappings, the Fabric ecosystem faces a fundamental transition: starting with 26.1, Mojang mappings become the de facto standard, and Yarn's role will necessarily evolve. Mod developers maintaining cross-version compatibility should pin their version bands carefully around the breakpoints identified in this reference: **1.20.5**, **1.21.2**, **1.21.5**, **1.21.6**, and **1.21.9**.