package com.acikek.calibrated.mixin;

import com.acikek.calibrated.item.RemoteItem;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public class SlotMixin {

    // TODO put a slot key on this instead so it can only insert at that key
    @Inject(method = "canInsert", cancellable = true, at = @At("HEAD"))
    private void calibrated$disableInsertIfAccessing(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.getItem() instanceof RemoteItem && stack.hasNbt() && stack.getOrCreateNbt().contains("AccessingTicks")) {
            cir.setReturnValue(false);
        }
    }
}
