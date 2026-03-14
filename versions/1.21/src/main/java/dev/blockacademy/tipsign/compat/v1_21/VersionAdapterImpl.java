package dev.blockacademy.tipsign.compat.v1_21;

import dev.blockacademy.tipsign.block.TipSignBlockEntity;
import dev.blockacademy.tipsign.common.TipSignData;
import dev.blockacademy.tipsign.compat.VersionAdapter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Band C (MC 1.21–1.21.1) version adapter implementation.
 * Uses CompoundTag + HolderLookup.Provider serialization, ResourceLocation.fromNamespaceAndPath().
 */
public class VersionAdapterImpl implements VersionAdapter {

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
        if (!tag.contains("Id")) return; // No data stored yet

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

    @Override
    public void writeToItemStack(ItemStack stack, TipSignData data) {
        // TODO: Phase 6 — DataComponentType for items
    }

    @Override
    public TipSignData readFromItemStack(ItemStack stack) {
        // TODO: Phase 6 — DataComponentType for items
        return null;
    }

    @Override
    public void setItemTooltipTitle(ItemStack stack, String title) {
        // TODO: Phase 6
    }

    @Override
    public void registerServerPlayReceivers() {
        // TODO: Phase 4 — register UpdateSignC2S receiver
    }

    @Override
    public void sendOpenSignToClient(ServerPlayer player, BlockPos pos, TipSignData data, boolean authorMode) {
        // TODO: Phase 4 — send OpenSignS2C packet
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
