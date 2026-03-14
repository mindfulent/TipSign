package dev.blockacademy.tipsign.block;

import dev.blockacademy.tipsign.TipSignMod;
import dev.blockacademy.tipsign.common.TipSignData;
import dev.blockacademy.tipsign.compat.VersionAdapter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

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
    }

    // --- Serialization (delegates to VersionAdapter) ---

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        VersionAdapter.INSTANCE.saveBlockEntityData(this, tag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        VersionAdapter.INSTANCE.loadBlockEntityData(this, tag);
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
        if (data.ownerUuid() != null) {
            tag.putUUID("OwnerUuid", data.ownerUuid());
        }
        return tag;
    }
}
