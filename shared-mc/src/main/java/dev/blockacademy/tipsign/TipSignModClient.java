package dev.blockacademy.tipsign;

import dev.blockacademy.tipsign.compat.VersionAdapter;
import dev.blockacademy.tipsign.render.TipSignBlockEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

public class TipSignModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        VersionAdapter.INSTANCE.registerClientPlayReceivers();
        BlockEntityRenderers.register(TipSignMod.SIGN_POST_BLOCK_ENTITY, TipSignBlockEntityRenderer::new);
    }
}
