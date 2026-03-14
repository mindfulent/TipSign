package dev.blockacademy.tipsign.compat.v1_21_4;

import dev.blockacademy.tipsign.TipSignMod;
import dev.blockacademy.tipsign.block.TipSignBlockEntity;
import dev.blockacademy.tipsign.common.TipSignData;
import dev.blockacademy.tipsign.common.TipSignDataCodec;
import dev.blockacademy.tipsign.compat.VersionAdapter;
import dev.blockacademy.tipsign.compat.v1_21_4.network.OpenSignPayload;
import dev.blockacademy.tipsign.compat.v1_21_4.network.UpdateSignPayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Band E (MC 1.21.4–1.21.5) version adapter implementation.
 * Note: 1.21.4 introduces item model definitions and Optional NBT getters,
 * but the core serialization/networking API matches Band C.
 */
public class VersionAdapterImpl implements VersionAdapter {

    @Override
    public void registerComponents() {
        TipSignDataComponent.register();
    }

    @Override
    public void saveBlockEntityData(Object blockEntity, Object tagOrOutput) {
        if (!(blockEntity instanceof TipSignBlockEntity be) || !(tagOrOutput instanceof CompoundTag tag)) return;
        TipSignData data = be.getData();
        if (data == null) return;

        tag.putString("Id", data.id());
        tag.putString("Title", data.title() != null ? data.title() : TipSignData.DEFAULT_TITLE);

        ListTag pages = new ListTag();
        for (String page : data.pages()) {
            pages.add(StringTag.valueOf(page));
        }
        tag.put("Pages", pages);

        if (data.kofiUrl() != null) tag.putString("KofiUrl", data.kofiUrl());
        if (data.patreonUrl() != null) tag.putString("PatreonUrl", data.patreonUrl());

        if (data.ownerUuid() != null) tag.putUUID("OwnerUuid", data.ownerUuid());
        if (data.ownerUsername() != null) tag.putString("OwnerUsername", data.ownerUsername());

        if (data.placedAt() != null) tag.putString("PlacedAt", data.placedAt().toString());
        if (data.lastEditedAt() != null) tag.putString("LastEditedAt", data.lastEditedAt().toString());
        tag.putInt("BgColorIndex", data.bgColorIndex());
    }

    @Override
    public void loadBlockEntityData(Object blockEntity, Object tagOrInput) {
        if (!(blockEntity instanceof TipSignBlockEntity be) || !(tagOrInput instanceof CompoundTag tag)) return;
        if (!tag.contains("Id")) return;

        String id = tag.getString("Id");
        String title = tag.getString("Title");

        List<String> pages = new ArrayList<>();
        if (tag.contains("Pages", Tag.TAG_LIST)) {
            ListTag pageList = tag.getList("Pages", Tag.TAG_STRING);
            for (int i = 0; i < pageList.size(); i++) {
                pages.add(pageList.getString(i));
            }
        }
        if (pages.isEmpty()) pages.add("");

        String kofiUrl = tag.contains("KofiUrl") ? tag.getString("KofiUrl") : null;
        String patreonUrl = tag.contains("PatreonUrl") ? tag.getString("PatreonUrl") : null;

        UUID ownerUuid = tag.hasUUID("OwnerUuid") ? tag.getUUID("OwnerUuid") : new UUID(0, 0);
        String ownerUsername = tag.contains("OwnerUsername") ? tag.getString("OwnerUsername") : "";

        Instant placedAt = parseInstant(tag, "PlacedAt");
        Instant lastEditedAt = parseInstant(tag, "LastEditedAt");
        int bgColorIndex = tag.contains("BgColorIndex") ? tag.getInt("BgColorIndex") : 0;

        be.setData(new TipSignData(id, title, pages, kofiUrl, patreonUrl, ownerUuid, ownerUsername, placedAt, lastEditedAt, bgColorIndex));
    }

    @Override
    public void writeToItemStack(ItemStack stack, TipSignData data) {
        TipSignDataComponent.writeToStack(stack, data);
    }

    @Override
    public TipSignData readFromItemStack(ItemStack stack) {
        return TipSignDataComponent.readFromStack(stack);
    }

    @Override
    public void setItemTooltipTitle(ItemStack stack, String title) {
        if (title != null && !title.isEmpty()) {
            stack.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,
                net.minecraft.network.chat.Component.literal(title));
        }
    }

    @Override
    public void registerServerPlayReceivers() {
        PayloadTypeRegistry.playS2C().register(OpenSignPayload.TYPE, OpenSignPayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(UpdateSignPayload.TYPE, UpdateSignPayload.STREAM_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(UpdateSignPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            BlockPos pos = payload.pos();

            context.player().server.execute(() -> {
                BlockEntity be = player.level().getBlockEntity(pos);
                if (!(be instanceof TipSignBlockEntity tipSign)) return;

                boolean isOwner = player.getUUID().equals(tipSign.getData().ownerUuid());
                boolean isAdmin = player.hasPermissions(2);
                if (!isOwner && !isAdmin) {
                    player.sendSystemMessage(Component.literal("You are not the owner of this Tip Sign."));
                    return;
                }

                TipSignData updated = TipSignDataCodec.fromJson(payload.jsonData());
                TipSignData existing = tipSign.getData();
                TipSignData merged = new TipSignData(
                    existing.id(),
                    updated.title(),
                    updated.pages(),
                    updated.kofiUrl(),
                    updated.patreonUrl(),
                    existing.ownerUuid(),
                    existing.ownerUsername(),
                    existing.placedAt(),
                    Instant.now(),
                    updated.bgColorIndex()
                );
                tipSign.setData(merged);

                player.level().sendBlockUpdated(pos, tipSign.getBlockState(), tipSign.getBlockState(), 3);
            });
        });
    }

    @Override
    public void sendOpenSignToClient(ServerPlayer player, BlockPos pos, TipSignData data, boolean authorMode) {
        String json = TipSignDataCodec.toJson(data);
        ServerPlayNetworking.send(player, new OpenSignPayload(pos, authorMode, json));
    }

    @Override
    public void registerClientPlayReceivers() {
        ClientPlayNetworking.registerGlobalReceiver(OpenSignPayload.TYPE, (payload, context) -> {
            TipSignData data = TipSignDataCodec.fromJson(payload.jsonData());
            BlockPos pos = payload.pos();
            boolean authorMode = payload.authorMode();

            context.client().execute(() -> {
                if (authorMode) {
                    context.client().setScreen(
                        new dev.blockacademy.tipsign.screen.TipSignAuthorScreen(data, pos));
                } else {
                    context.client().setScreen(
                        new dev.blockacademy.tipsign.screen.TipSignReaderScreen(data, pos));
                }
            });
        });
    }

    @Override
    public void sendUpdateToServer(BlockPos pos, TipSignData data) {
        String json = TipSignDataCodec.toJson(data);
        ClientPlayNetworking.send(new UpdateSignPayload(pos, json));
    }

    @Override
    public ResourceLocation createId(String namespace, String path) {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }

    private static Instant parseInstant(CompoundTag tag, String key) {
        if (!tag.contains(key)) return null;
        try {
            return Instant.parse(tag.getString(key));
        } catch (Exception e) {
            return null;
        }
    }
}
