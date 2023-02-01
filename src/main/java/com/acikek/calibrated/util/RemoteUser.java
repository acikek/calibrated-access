package com.acikek.calibrated.util;

import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public interface RemoteUser {

    void setUsingRemote(BlockPos syncedPos, UUID session);

    boolean isUsingRemote();

    void setSession(UUID uuid);

    UUID getSession();

    default boolean hasSession() {
        return getSession() != null;
    }
}
