package dev.blockacademy.tipsign.compat.v1_21_9;

import dev.blockacademy.tipsign.TipSignMod;
import dev.blockacademy.tipsign.block.TipSignBlockEntity;
import dev.blockacademy.tipsign.common.TipSignData;
import dev.blockacademy.tipsign.common.TipSignDataCodec;
import dev.blockacademy.tipsign.compat.VersionAdapter;
import dev.blockacademy.tipsign.compat.v1_21_9.network.OpenSignPayload;
import dev.blockacademy.tipsign.compat.v1_21_9.network.UpdateSignPayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.storage.ValueOutput;
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
 * Band G (MC 1.21.9–1.21.11) version adapter implementation.
 * Uses ValueOutput/ValueInput for block entity serialization (replaces CompoundTag).
 */
public class VersionAdapterImpl implements VersionAdapter {

    @Override
    public void registerComponents() {
        TipSignDataComponent.register();
    }

    @Override
    public void saveBlockEntityData(Object blockEntity, Object tagOrOutput) {
        if (!(blockEntity instanceof TipSignBlockEntity be)) return;
        TipSignData data = be.getData();
        if (data == null) return;

        if (tagOrOutput instanceof ValueOutput view) {
            view.putString("Id", data.id());
            view.putString("Title", data.title() != null ? data.title() : TipSignData.DEFAULT_TITLE);

            view.putInt("PageCount", data.pages().size());
            for (int i = 0; i < data.pages().size(); i++) {
                view.putString("Page" + i, data.pages().get(i));
            }

            if (data.kofiUrl() != null) view.putString("KofiUrl", data.kofiUrl());
            if (data.patreonUrl() != null) view.putString("PatreonUrl", data.patreonUrl());

            if (data.ownerUuid() != null) view.store("OwnerUuid", net.minecraft.core.UUIDUtil.CODEC, data.ownerUuid());
            if (data.ownerUsername() != null) view.putString("OwnerUsername", data.ownerUsername());

            if (data.placedAt() != null) view.putString("PlacedAt", data.placedAt().toString());
            if (data.lastEditedAt() != null) view.putString("LastEditedAt", data.lastEditedAt().toString());
            view.putInt("BgColorIndex", data.bgColorIndex());
        } else if (tagOrOutput instanceof CompoundTag tag) {
            tag.putString("Id", data.id());
            tag.putString("Title", data.title() != null ? data.title() : TipSignData.DEFAULT_TITLE);

            ListTag pages = new ListTag();
            for (String page : data.pages()) {
                pages.add(StringTag.valueOf(page));
            }
            tag.put("Pages", pages);

            if (data.kofiUrl() != null) tag.putString("KofiUrl", data.kofiUrl());
            if (data.patreonUrl() != null) tag.putString("PatreonUrl", data.patreonUrl());

            if (data.ownerUuid() != null) tag.putIntArray("OwnerUuid", net.minecraft.core.UUIDUtil.uuidToIntArray(data.ownerUuid()));
            if (data.ownerUsername() != null) tag.putString("OwnerUsername", data.ownerUsername());

            if (data.placedAt() != null) tag.putString("PlacedAt", data.placedAt().toString());
            if (data.lastEditedAt() != null) tag.putString("LastEditedAt", data.lastEditedAt().toString());
            tag.putInt("BgColorIndex", data.bgColorIndex());
        }
    }

    @Override
    public void loadBlockEntityData(Object blockEntity, Object tagOrInput) {
        if (!(blockEntity instanceof TipSignBlockEntity be)) return;

        if (tagOrInput instanceof ValueInput view) {
            if (view.getString("Id").isEmpty()) return;

            String id = view.getString("Id").orElse("");
            String title = view.getString("Title").orElse(TipSignData.DEFAULT_TITLE);

            List<String> pages = new ArrayList<>();
            int pageCount = view.getInt("PageCount").orElse(0);
            for (int i = 0; i < pageCount; i++) {
                view.getString("Page" + i).ifPresent(pages::add);
            }
            if (pages.isEmpty()) pages.add("");

            String kofiUrl = view.getString("KofiUrl").orElse(null);
            String patreonUrl = view.getString("PatreonUrl").orElse(null);

            UUID ownerUuid = view.read("OwnerUuid", net.minecraft.core.UUIDUtil.CODEC).orElse(new UUID(0, 0));
            String ownerUsername = view.getString("OwnerUsername").orElse("");

            Instant placedAt = view.getString("PlacedAt").map(VersionAdapterImpl::parseInstantStr).orElse(null);
            Instant lastEditedAt = view.getString("LastEditedAt").map(VersionAdapterImpl::parseInstantStr).orElse(null);
            int bgColorIndex = view.getInt("BgColorIndex").orElse(0);

            be.setData(new TipSignData(id, title, pages, kofiUrl, patreonUrl, ownerUuid, ownerUsername, placedAt, lastEditedAt, bgColorIndex));
        } else if (tagOrInput instanceof CompoundTag tag) {
            if (tag.getString("Id").isEmpty()) return;

            String id = tag.getStringOr("Id", "");
            String title = tag.getStringOr("Title", TipSignData.DEFAULT_TITLE);

            List<String> pages = new ArrayList<>();
            tag.getList("Pages").ifPresent(pageList -> {
                for (int i = 0; i < pageList.size(); i++) {
                    if (pageList.get(i) instanceof StringTag st) {
                        pages.add(st.value());
                    }
                }
            });
            if (pages.isEmpty()) pages.add("");

            String kofiUrl = tag.getStringOr("KofiUrl", null);
            String patreonUrl = tag.getStringOr("PatreonUrl", null);

            UUID ownerUuid = tag.getIntArray("OwnerUuid")
                .filter(arr -> arr.length == 4)
                .map(net.minecraft.core.UUIDUtil::uuidFromIntArray)
                .orElse(new UUID(0, 0));
            String ownerUsername = tag.getStringOr("OwnerUsername", "");

            Instant placedAt = tag.getString("PlacedAt").map(VersionAdapterImpl::parseInstantStr).orElse(null);
            Instant lastEditedAt = tag.getString("LastEditedAt").map(VersionAdapterImpl::parseInstantStr).orElse(null);
            int bgColorIndex = tag.getIntOr("BgColorIndex", 0);

            be.setData(new TipSignData(id, title, pages, kofiUrl, patreonUrl, ownerUuid, ownerUsername, placedAt, lastEditedAt, bgColorIndex));
        }
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

            player.level().getServer().execute(() -> {
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

    private static Instant parseInstantStr(String s) {
        try {
            return Instant.parse(s);
        } catch (Exception e) {
            return null;
        }
    }

    private static Instant parseInstant(CompoundTag tag, String key) {
        return tag.getString(key).map(VersionAdapterImpl::parseInstantStr).orElse(null);
    }
}
