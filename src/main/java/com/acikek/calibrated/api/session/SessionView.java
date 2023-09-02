package com.acikek.calibrated.api.session;

import net.minecraft.util.math.BlockPos;

/**
 * An immutable view of a player's remote session data.
 */
public interface SessionView {

    /**
     * @return the synced block's position
     */
    BlockPos syncedPos();

    /**
     * @return whether the player is currently accessing a block through this session
     */
    boolean isActive();

    /**
     * @return the amount of ticks left in the access period, or {@code 0} if this session is inactive
     * @see SessionView#isActive()
     */
    int remainingTicks();
}
