package dev.blockacademy.tipsign;

import dev.blockacademy.tipsign.block.TipSignBlockEntity;
import dev.blockacademy.tipsign.common.TipSignConfig;
import net.minecraft.server.level.ServerPlayer;

public final class TipSignPermissions {

    private TipSignPermissions() {}

    public static boolean canEdit(ServerPlayer player, TipSignBlockEntity be) {
        return isOwner(player, be) || isAdmin(player);
    }

    public static boolean canBreak(ServerPlayer player, TipSignBlockEntity be) {
        if (!TipSignConfig.get().ownerOnlyBreak()) return true;
        return isOwner(player, be) || isAdmin(player);
    }

    public static boolean isOwner(ServerPlayer player, TipSignBlockEntity be) {
        return player.getUUID().equals(be.getData().ownerUuid());
    }

    public static boolean isAdmin(ServerPlayer player) {
        // Op level 2+ is admin
        // Fabric Permissions API (tipsign.admin) checked via reflection if available
        return player.hasPermissions(2);
    }
}
