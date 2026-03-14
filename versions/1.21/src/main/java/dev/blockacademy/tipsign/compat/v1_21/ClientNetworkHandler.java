package dev.blockacademy.tipsign.compat.v1_21;

import dev.blockacademy.tipsign.common.TipSignData;
import dev.blockacademy.tipsign.common.TipSignDataCodec;
import dev.blockacademy.tipsign.compat.v1_21.network.OpenSignPayload;
import dev.blockacademy.tipsign.compat.v1_21.network.UpdateSignPayload;
import dev.blockacademy.tipsign.screen.TipSignAuthorScreen;
import dev.blockacademy.tipsign.screen.TipSignReaderScreen;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.BlockPos;

/**
 * Client-only network handlers. This class is never loaded on a dedicated server,
 * isolating client imports (Screen, ClientPlayNetworking) from ServiceLoader scanning.
 */
public final class ClientNetworkHandler {

    public static void registerReceivers() {
        ClientPlayNetworking.registerGlobalReceiver(OpenSignPayload.TYPE, (payload, context) -> {
            TipSignData data = TipSignDataCodec.fromJson(payload.jsonData());
            BlockPos pos = payload.pos();
            boolean authorMode = payload.authorMode();

            context.client().execute(() -> {
                if (authorMode) {
                    context.client().setScreen(new TipSignAuthorScreen(data, pos));
                } else {
                    context.client().setScreen(new TipSignReaderScreen(data, pos));
                }
            });
        });
    }

    public static void sendUpdate(BlockPos pos, TipSignData data) {
        String json = TipSignDataCodec.toJson(data);
        ClientPlayNetworking.send(new UpdateSignPayload(pos, json));
    }
}
