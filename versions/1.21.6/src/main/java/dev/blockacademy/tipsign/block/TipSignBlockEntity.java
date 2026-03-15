package dev.blockacademy.tipsign.block;

import dev.blockacademy.tipsign.TipSignMod;
import dev.blockacademy.tipsign.common.TipSignData;
import dev.blockacademy.tipsign.compat.VersionAdapter;
import dev.blockacademy.tipsign.discovery.DiscoveryManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Band F (MC 1.21.6–1.21.8) TipSignBlockEntity.
 * Uses ValueOutput/ValueInput serialization instead of CompoundTag.
 */
public class TipSignBlockEntity extends BlockEntity {

    private TipSignData data;

    public TipSignBlockEntity(BlockPos pos, BlockState state) {
        super(TipSignMod.SIGN_POST_BLOCK_ENTITY, pos, state);
        this.data = TipSignData.createNew(new UUID(0, 0), "");
    }

    public TipSignData getData() {
        return data;
    }

    public void setData(TipSignData data) {
        this.data = data;
        setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            DiscoveryManager.get().trackSign(this.getBlockPos(), data);
            DiscoveryManager.get().onSignChanged();
        }
    }

    // --- Serialization (1.21.6+: ValueOutput/ValueInput) ---

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        VersionAdapter.INSTANCE.saveBlockEntityData(this, output);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        // Sync tags from getUpdateTag() don't contain "Id" — handle them directly
        if (input.getString("Id").isEmpty()) {
            input.getString("Title").ifPresent(title ->
                this.data = this.data.withTitle(title));
            input.getInt("BgColorIndex").ifPresent(idx ->
                this.data = this.data.withBgColorIndex(idx));
            return;
        }
        VersionAdapter.INSTANCE.loadBlockEntityData(this, input);
    }

    // --- Client sync (title for BER rendering) ---

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        // Only sync title and owner for client-side BER rendering
        tag.putString("Title", data.title() != null ? data.title() : TipSignData.DEFAULT_TITLE);
        tag.putInt("BgColorIndex", data.bgColorIndex());
        if (data.ownerUuid() != null) {
            tag.putIntArray("OwnerUuid", net.minecraft.core.UUIDUtil.uuidToIntArray(data.ownerUuid()));
        }
        return tag;
    }
}
