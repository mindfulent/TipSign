package dev.blockacademy.tipsign.compat;

import dev.blockacademy.tipsign.common.TipSignData;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ServiceLoader;

/**
 * Central interface isolating all version-specific Minecraft API calls.
 * Each version band provides an implementation loaded via ServiceLoader.
 */
public interface VersionAdapter {

    // --- Registration ---
    void registerComponents();

    // --- Block Entity Serialization ---
    void saveBlockEntityData(Object blockEntity, Object tagOrOutput);
    void loadBlockEntityData(Object blockEntity, Object tagOrInput);

    // --- Item Stack Data ---
    void writeToItemStack(ItemStack stack, TipSignData data);
    TipSignData readFromItemStack(ItemStack stack);
    void setItemTooltipTitle(ItemStack stack, String title);

    // --- Networking ---
    void registerServerPlayReceivers();
    void sendOpenSignToClient(ServerPlayer player, BlockPos pos, TipSignData data, boolean authorMode);

    // --- Client-side networking ---
    void registerClientPlayReceivers();
    void sendUpdateToServer(BlockPos pos, TipSignData data);

    // --- Identifier Construction ---
    ResourceLocation createId(String namespace, String path);

    // --- Singleton ---
    VersionAdapter INSTANCE = ServiceLoader.load(VersionAdapter.class)
        .findFirst()
        .orElseThrow(() -> new RuntimeException("No VersionAdapter found! Is a version band JAR loaded?"));
}
