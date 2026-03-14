package dev.blockacademy.tipsign;

import dev.blockacademy.tipsign.block.TipSignBlock;
import dev.blockacademy.tipsign.block.TipSignBlockEntity;
import dev.blockacademy.tipsign.common.TipSignConfig;
import dev.blockacademy.tipsign.compat.VersionAdapter;
import dev.blockacademy.tipsign.discovery.DiscoveryManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TipSignMod implements ModInitializer {
    public static final String MOD_ID = "tipsign";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // Block
    public static final Block SIGN_POST_BLOCK = new TipSignBlock(
        BlockBehaviour.Properties.of()
            .mapColor(MapColor.WOOD)
            .strength(2.0f, 3.0f)
            .sound(SoundType.WOOD)
            .noOcclusion()
            .pushReaction(PushReaction.BLOCK)
    );

    // Block Item
    public static final Item SIGN_POST_ITEM = new BlockItem(SIGN_POST_BLOCK, new Item.Properties());

    // Block Entity Type
    public static BlockEntityType<TipSignBlockEntity> SIGN_POST_BLOCK_ENTITY;

    @Override
    public void onInitialize() {
        // Register data components (must be before block/item registration)
        VersionAdapter.INSTANCE.registerComponents();

        ResourceLocation blockId = ResourceLocation.fromNamespaceAndPath(MOD_ID, "sign_post");

        // Register block
        Registry.register(BuiltInRegistries.BLOCK, blockId, SIGN_POST_BLOCK);

        // Register block item
        Registry.register(BuiltInRegistries.ITEM, blockId, SIGN_POST_ITEM);

        // Register block entity type
        SIGN_POST_BLOCK_ENTITY = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            blockId,
            BlockEntityType.Builder.of(TipSignBlockEntity::new, SIGN_POST_BLOCK).build(null)
        );

        // Add to creative tab
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(entries -> {
            entries.accept(SIGN_POST_ITEM);
        });

        // Register server-side networking
        VersionAdapter.INSTANCE.registerServerPlayReceivers();

        // Load config
        TipSignConfig.load(net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir());

        // Server lifecycle events for discovery API
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            DiscoveryManager.get().init(server, server.getServerDirectory().resolve("config"));
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            DiscoveryManager.get().shutdown();
        });

        LOGGER.info("TipSign loaded");
    }
}
