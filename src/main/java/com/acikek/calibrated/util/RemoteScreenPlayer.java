package com.acikek.calibrated.util;

import net.minecraft.util.math.BlockPos;

public interface RemoteScreenPlayer {

    void setUsingRemote(BlockPos syncedPos);

    default void setIdling() {
        setUsingRemote(null);
    }
}
