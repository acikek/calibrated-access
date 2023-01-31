package com.acikek.calibrated.mixin;

import com.acikek.calibrated.item.RemoteItem;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public class SlotMixin {

    @Shadow @Final public Inventory inventory;

    @Inject(method = "canInsert", cancellable = true, at = @At("HEAD"))
    private void calibrated$disableInsertIfAccessing(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        // VisualTicks can outlast actual accessing ticks, but it's not very important
        if (stack.getItem() instanceof RemoteItem && stack.hasNbt() && stack.getOrCreateNbt().contains("VisualTicks")) {
            cir.setReturnValue(inventory instanceof PlayerInventory);
        }
    }
}
