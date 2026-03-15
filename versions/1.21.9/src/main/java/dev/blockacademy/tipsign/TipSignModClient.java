package dev.blockacademy.tipsign;

import com.mojang.blaze3d.platform.InputConstants;
import dev.blockacademy.tipsign.block.TipSignBlockEntity;
import dev.blockacademy.tipsign.common.TipSignData;
import dev.blockacademy.tipsign.compat.VersionAdapter;
import dev.blockacademy.tipsign.render.TipSignBlockEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.lwjgl.glfw.GLFW;

/**
 * Band G (MC 1.21.9–1.21.11) client initializer.
 * KeyMapping constructor takes KeyMapping.Category instead of String in 1.21.9+.
 */
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

        // === DEBUG keybinds (remove after BER calibration) ===
        KeyMapping.Category debugCategory = KeyMapping.Category.register(
            VersionAdapter.INSTANCE.createId("tipsign", "debug"));
        KeyMapping debugUp = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "TipSign +", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_KP_ADD, debugCategory));
        KeyMapping debugDown = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "TipSign -", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_KP_SUBTRACT, debugCategory));
        KeyMapping debugSwitch = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "TipSign Mode", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_KP_MULTIPLY, debugCategory));
        KeyMapping debugFlip = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "TipSign Rot+90", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_KP_DIVIDE, debugCategory));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            if (debugSwitch.consumeClick()) {
                TipSignBlockEntityRenderer.debugMode = (TipSignBlockEntityRenderer.debugMode + 1) % 3;
                String[] names = {"RotOffset", "Z-offset", "Y-center"};
                client.player.displayClientMessage(
                        Component.literal("[TipSign] Mode: " + names[TipSignBlockEntityRenderer.debugMode]), true);
            }

            if (debugFlip.consumeClick()) {
                TipSignBlockEntityRenderer.debugRotOffset += 90f;
                if (TipSignBlockEntityRenderer.debugRotOffset >= 360f)
                    TipSignBlockEntityRenderer.debugRotOffset -= 360f;
                printDebug(client.player);
            }

            float step = 0.01f;
            if (debugUp.consumeClick()) {
                adjustDebug(step);
                printDebug(client.player);
            }
            if (debugDown.consumeClick()) {
                adjustDebug(-step);
                printDebug(client.player);
            }
        });
    }

    private static void adjustDebug(float delta) {
        switch (TipSignBlockEntityRenderer.debugMode) {
            case 0 -> TipSignBlockEntityRenderer.debugRotOffset += delta * 1000;
            case 1 -> {
                TipSignBlockEntityRenderer.debugZStanding += delta;
                TipSignBlockEntityRenderer.debugZWall += delta;
            }
            case 2 -> {
                TipSignBlockEntityRenderer.debugYStanding += delta;
                TipSignBlockEntityRenderer.debugYWall += delta;
            }
        }
    }

    private static void printDebug(net.minecraft.world.entity.player.Player player) {
        String msg = String.format("[TipSign] rot=%.0f  zStand=%.3f  zWall=%.3f  yStand=%.3f  yWall=%.3f",
                TipSignBlockEntityRenderer.debugRotOffset,
                TipSignBlockEntityRenderer.debugZStanding,
                TipSignBlockEntityRenderer.debugZWall,
                TipSignBlockEntityRenderer.debugYStanding,
                TipSignBlockEntityRenderer.debugYWall);
        player.displayClientMessage(Component.literal(msg), true);
    }
}
