package com.acikek.calibrated.util;

import com.acikek.calibrated.network.CANetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public interface RemoteUser {

    void setUsingRemote(BlockPos syncedPos, UUID session);

    boolean isUsingRemote();

    void setSession(UUID uuid);

    UUID getUsingSession();

    UUID getSession();

    default boolean hasSession() {
        return getSession() != null;
    }

    static void setUsingRemote(ServerPlayerEntity player, BlockPos syncedPos, UUID session) {
        ((RemoteUser) player).setUsingRemote(syncedPos, session);
        CANetworking.s2cSetUsingRemote(player, syncedPos, session);
    }
}
