package dev.blockacademy.tipsign.compat.v1_21_6;

import com.mojang.serialization.Codec;
import dev.blockacademy.tipsign.TipSignMod;
import dev.blockacademy.tipsign.common.TipSignDataCodec;
import dev.blockacademy.tipsign.common.TipSignData;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;

/**
 * Custom DataComponentType that stores TipSignData as a JSON string on item stacks.
 */
public class TipSignDataComponent {

    public static final DataComponentType<String> TIPSIGN_DATA = DataComponentType.<String>builder()
        .persistent(Codec.STRING)
        .networkSynchronized(ByteBufCodecs.STRING_UTF8)
        .build();

    public static void register() {
        Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            ResourceLocation.fromNamespaceAndPath(TipSignMod.MOD_ID, "sign_data"),
            TIPSIGN_DATA
        );
    }

    public static void writeToStack(net.minecraft.world.item.ItemStack stack, TipSignData data) {
        stack.set(TIPSIGN_DATA, TipSignDataCodec.toJson(data));
    }

    public static TipSignData readFromStack(net.minecraft.world.item.ItemStack stack) {
        String json = stack.get(TIPSIGN_DATA);
        if (json == null || json.isEmpty()) return null;
        return TipSignDataCodec.fromJson(json);
    }
}
