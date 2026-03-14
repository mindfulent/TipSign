package dev.blockacademy.tipsign.compat.v1_21_6.network;

import dev.blockacademy.tipsign.TipSignMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * S2C packet: server sends sign data + position to client to open Reader or Author screen.
 */
public record OpenSignPayload(
    BlockPos pos,
    boolean authorMode,
    String jsonData  // TipSignData serialized as JSON
) implements CustomPacketPayload {

    public static final Type<OpenSignPayload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(TipSignMod.MOD_ID, "open_sign"));

    public static final StreamCodec<FriendlyByteBuf, OpenSignPayload> STREAM_CODEC =
        StreamCodec.composite(
            BlockPos.STREAM_CODEC, OpenSignPayload::pos,
            ByteBufCodecs.BOOL, OpenSignPayload::authorMode,
            ByteBufCodecs.STRING_UTF8, OpenSignPayload::jsonData,
            OpenSignPayload::new
        );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
