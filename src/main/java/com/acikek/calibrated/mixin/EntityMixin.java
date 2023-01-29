package com.acikek.calibrated.mixin;

import com.acikek.calibrated.util.RemoteScreenPlayer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin implements RemoteScreenPlayer {

    private boolean calibrated$usingRemote;

    @Override
    public void setUsingRemote(boolean usingRemote) {
        System.out.println("setUsingRemote " + usingRemote);
        calibrated$usingRemote = usingRemote;
    }

    // Screen handlers call this method in some way, just not consistently.
    @Inject(method = "squaredDistanceTo(DDD)D", cancellable = true, at = @At("HEAD"))
    private void calibrated$fakeDistance(double x, double y, double z, CallbackInfoReturnable<Double> cir) {
        // Also validated on server
        if (calibrated$usingRemote) {
            cir.setReturnValue(0.0);
        }
    }
}
