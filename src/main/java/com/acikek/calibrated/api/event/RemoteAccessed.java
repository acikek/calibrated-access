package com.acikek.calibrated.api.event;

import com.acikek.calibrated.item.remote.RemoteItem;
import com.acikek.calibrated.item.remote.RemoteUseResult;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@FunctionalInterface
public interface RemoteAccessed {

    RemoteUseResult onRemoteAccessed(World world, PlayerEntity player, BlockPos pos, BlockState state, RemoteItem remote, ItemStack remoteStack);
}
