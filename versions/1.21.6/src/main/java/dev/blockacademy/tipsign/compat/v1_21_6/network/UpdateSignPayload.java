package dev.blockacademy.tipsign.compat.v1_21_6.network;

import dev.blockacademy.tipsign.TipSignMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * C2S packet: client sends updated sign data to server from Author UI.
 */
public record UpdateSignPayload(
    BlockPos pos,
    String jsonData  // Updated TipSignData serialized as JSON
) implements CustomPacketPayload {

    public static final Type<UpdateSignPayload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(TipSignMod.MOD_ID, "update_sign"));

    public static final StreamCodec<FriendlyByteBuf, UpdateSignPayload> STREAM_CODEC =
        StreamCodec.composite(
            BlockPos.STREAM_CODEC, UpdateSignPayload::pos,
            ByteBufCodecs.STRING_UTF8, UpdateSignPayload::jsonData,
            UpdateSignPayload::new
        );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
