package com.acikek.calibrated;

import com.acikek.calibrated.api.CalibratedAccessAPI;
import com.acikek.calibrated.api.event.RemoteUseResults;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CAVanillaIntegration {

    public static final Text ENDER_CHEST_CONTAINER_NAME = Text.translatable("container.enderchest");

    public static boolean openEnderChest(World world, PlayerEntity player, BlockPos pos) {
        EnderChestInventory inventory = player.getEnderChestInventory();
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (inventory == null || !(blockEntity instanceof EnderChestBlockEntity enderChest)) {
            return false;
        }
        if (world.isClient()) {
            return true;
        }
        inventory.setActiveBlockEntity(enderChest);
        player.openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, inv, playerx) ->
                GenericContainerScreenHandler.createGeneric9x3(syncId, inv, inventory), ENDER_CHEST_CONTAINER_NAME));
        player.incrementStat(Stats.OPEN_ENDERCHEST);
        PiglinBrain.onGuardedBlockInteracted(player, true);
        return true;
    }

    public static void register() {
        CalibratedAccessAPI.registerListener(Blocks.ENDER_CHEST, (world, player, pos, state, remote, remoteStack) -> {
            var result = openEnderChest(world, player, pos);
            return result ? RemoteUseResults.SUCCESS : RemoteUseResults.DESYNC;
        });
    }
}
