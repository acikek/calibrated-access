package com.acikek.calibrated.api.session;

import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * An immutable view of a player's remote session data.
 */
public interface SessionView {

    /**
     * @return the synced block's position
     */
    BlockPos syncedPos();

    /**
     * @return the synced block's world key
     */
    RegistryKey<World> worldKey();

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
