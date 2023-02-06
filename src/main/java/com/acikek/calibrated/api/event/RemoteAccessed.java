package com.acikek.calibrated.api.event;

import com.acikek.calibrated.item.remote.RemoteItem;
import com.acikek.calibrated.item.remote.RemoteUseResult;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

/**
 * Callback for using a remote accessor after all the base checks have passed.<br>
 * Return a result with {@link RemoteUseResults} to determine how to proceed with the activation.
 */
@FunctionalInterface
public interface RemoteAccessed {

    RemoteUseResult onRemoteAccessed(ServerWorld world, ServerPlayerEntity player, BlockPos pos, BlockState state, RemoteItem remote, ItemStack remoteStack);
}
