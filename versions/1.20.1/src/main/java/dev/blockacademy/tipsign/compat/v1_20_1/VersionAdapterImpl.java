package dev.blockacademy.tipsign.compat.v1_20_1;

import dev.blockacademy.tipsign.TipSignMod;
import dev.blockacademy.tipsign.block.TipSignBlockEntity;
import dev.blockacademy.tipsign.common.TipSignData;
import dev.blockacademy.tipsign.common.TipSignDataCodec;
import dev.blockacademy.tipsign.compat.VersionAdapter;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
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
 * Band A (MC 1.20.1–1.20.4) version adapter implementation.
 *
 * Key differences from Band C:
 * - No DataComponentType (uses BlockEntityTag NBT on items)
 * - FriendlyByteBuf networking (not CustomPayload)
 * - ResourceLocation constructor (not fromNamespaceAndPath)
 * - saveAdditional/loadAdditional without HolderLookup.Provider
 */
public class VersionAdapterImpl implements VersionAdapter {

    private static final ResourceLocation OPEN_SIGN_ID = new ResourceLocation(TipSignMod.MOD_ID, "open_sign");
    private static final ResourceLocation UPDATE_SIGN_ID = new ResourceLocation(TipSignMod.MOD_ID, "update_sign");

    @Override
    public void registerComponents() {
        // No-op: DataComponentType does not exist in 1.20.1
    }

    // --- Block Entity Serialization ---

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

        be.setData(new TipSignData(id, title, pages, kofiUrl, patreonUrl, ownerUuid, ownerUsername, placedAt, lastEditedAt));
    }

    // --- Item Stack Data (BlockEntityTag NBT) ---

    @Override
    public void writeToItemStack(ItemStack stack, TipSignData data) {
        CompoundTag beTag = stack.getOrCreateTagElement("BlockEntityTag");

        beTag.putString("Id", data.id());
        beTag.putString("Title", data.title() != null ? data.title() : TipSignData.DEFAULT_TITLE);

        ListTag pages = new ListTag();
        for (String page : data.pages()) {
            pages.add(StringTag.valueOf(page));
        }
        beTag.put("Pages", pages);

        if (data.kofiUrl() != null) beTag.putString("KofiUrl", data.kofiUrl());
        if (data.patreonUrl() != null) beTag.putString("PatreonUrl", data.patreonUrl());

        if (data.ownerUuid() != null) beTag.putUUID("OwnerUuid", data.ownerUuid());
        if (data.ownerUsername() != null) beTag.putString("OwnerUsername", data.ownerUsername());

        if (data.placedAt() != null) beTag.putString("PlacedAt", data.placedAt().toString());
        if (data.lastEditedAt() != null) beTag.putString("LastEditedAt", data.lastEditedAt().toString());
    }

    @Override
    public TipSignData readFromItemStack(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("BlockEntityTag", Tag.TAG_COMPOUND)) return null;

        CompoundTag beTag = tag.getCompound("BlockEntityTag");
        if (!beTag.contains("Id")) return null;

        String id = beTag.getString("Id");
        String title = beTag.getString("Title");

        List<String> pages = new ArrayList<>();
        if (beTag.contains("Pages", Tag.TAG_LIST)) {
            ListTag pageList = beTag.getList("Pages", Tag.TAG_STRING);
            for (int i = 0; i < pageList.size(); i++) {
                pages.add(pageList.getString(i));
            }
        }
        if (pages.isEmpty()) pages.add("");

        String kofiUrl = beTag.contains("KofiUrl") ? beTag.getString("KofiUrl") : null;
        String patreonUrl = beTag.contains("PatreonUrl") ? beTag.getString("PatreonUrl") : null;

        UUID ownerUuid = beTag.hasUUID("OwnerUuid") ? beTag.getUUID("OwnerUuid") : new UUID(0, 0);
        String ownerUsername = beTag.contains("OwnerUsername") ? beTag.getString("OwnerUsername") : "";

        Instant placedAt = parseInstant(beTag, "PlacedAt");
        Instant lastEditedAt = parseInstant(beTag, "LastEditedAt");

        return new TipSignData(id, title, pages, kofiUrl, patreonUrl, ownerUuid, ownerUsername, placedAt, lastEditedAt);
    }

    @Override
    public void setItemTooltipTitle(ItemStack stack, String title) {
        if (title != null && !title.isEmpty()) {
            stack.setHoverName(Component.literal(title));
        }
    }

    // --- Networking (FriendlyByteBuf, legacy API) ---

    @Override
    public void registerServerPlayReceivers() {
        // Handle C2S: Author UI save
        ServerPlayNetworking.registerGlobalReceiver(UPDATE_SIGN_ID, (server, player, handler, buf, sender) -> {
            BlockPos pos = buf.readBlockPos();
            String jsonData = buf.readUtf(32767);

            server.execute(() -> {
                BlockEntity be = player.level().getBlockEntity(pos);
                if (!(be instanceof TipSignBlockEntity tipSign)) return;

                // Permission check
                boolean isOwner = player.getUUID().equals(tipSign.getData().ownerUuid());
                boolean isAdmin = player.hasPermissions(2);
                if (!isOwner && !isAdmin) {
                    player.sendSystemMessage(Component.literal("You are not the owner of this Tip Sign."));
                    return;
                }

                // Apply update
                TipSignData updated = TipSignDataCodec.fromJson(jsonData);
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
                    Instant.now()
                );
                tipSign.setData(merged);

                // Sync to nearby clients
                player.level().sendBlockUpdated(pos, tipSign.getBlockState(), tipSign.getBlockState(), 3);
            });
        });
    }

    @Override
    public void sendOpenSignToClient(ServerPlayer player, BlockPos pos, TipSignData data, boolean authorMode) {
        String json = TipSignDataCodec.toJson(data);
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(pos);
        buf.writeBoolean(authorMode);
        buf.writeUtf(json, 32767);
        ServerPlayNetworking.send(player, OPEN_SIGN_ID, buf);
    }

    @Override
    public void registerClientPlayReceivers() {
        ClientPlayNetworking.registerGlobalReceiver(OPEN_SIGN_ID, (client, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            boolean authorMode = buf.readBoolean();
            String json = buf.readUtf(32767);

            client.execute(() -> {
                TipSignData data = TipSignDataCodec.fromJson(json);
                if (authorMode) {
                    client.setScreen(
                        new dev.blockacademy.tipsign.screen.TipSignAuthorScreen(data, pos));
                } else {
                    client.setScreen(
                        new dev.blockacademy.tipsign.screen.TipSignReaderScreen(data, pos));
                }
            });
        });
    }

    @Override
    public void sendUpdateToServer(BlockPos pos, TipSignData data) {
        String json = TipSignDataCodec.toJson(data);
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(pos);
        buf.writeUtf(json, 32767);
        ClientPlayNetworking.send(UPDATE_SIGN_ID, buf);
    }

    // --- Identifier Construction ---

    @Override
    public ResourceLocation createId(String namespace, String path) {
        return new ResourceLocation(namespace, path);
    }

    // --- Helpers ---

    private static Instant parseInstant(CompoundTag tag, String key) {
        if (!tag.contains(key)) return null;
        try {
            return Instant.parse(tag.getString(key));
        } catch (Exception e) {
            return null;
        }
    }
}
