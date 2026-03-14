package dev.blockacademy.tipsign;

import dev.blockacademy.tipsign.block.TipSignBlockEntity;
import dev.blockacademy.tipsign.common.TipSignData;
import dev.blockacademy.tipsign.compat.VersionAdapter;
import dev.blockacademy.tipsign.render.TipSignBlockEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.level.block.entity.BlockEntity;

public class TipSignModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        VersionAdapter.INSTANCE.registerClientPlayReceivers();
        BlockEntityRenderers.register(TipSignMod.SIGN_POST_BLOCK_ENTITY, TipSignBlockEntityRenderer::new);

        // Tint the board faces based on bgColorIndex
        ColorProviderRegistry.BLOCK.register((state, level, pos, tintIndex) -> {
            if (tintIndex != 0 || level == null || pos == null) return 0xFFFFFFFF;
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof TipSignBlockEntity tipSign && tipSign.getData() != null) {
                return TipSignData.blockTintColor(tipSign.getData().bgColorIndex());
            }
            return 0xFFFFFFFF;
        }, TipSignMod.SIGN_POST_BLOCK);
    }
}
