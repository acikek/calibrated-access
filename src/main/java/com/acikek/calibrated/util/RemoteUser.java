package com.acikek.calibrated.util;

import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public interface RemoteUser {

    void setUsingRemote(BlockPos syncedPos);

    boolean isUsingRemote();

    void setSession(UUID uuid);

    UUID getSession();

    default boolean hasSession() {
        return getSession() != null;
    }
}
