package com.acikek.calibrated.mixin;

import com.acikek.calibrated.util.RemoteScreenPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin implements RemoteScreenPlayer {

    private BlockPos calibrated$syncedPos;

    @Override
    public void setUsingRemote(BlockPos syncedPos) {
        calibrated$syncedPos = syncedPos;
    }

    // Screen handlers call this method in some way, just not consistently.
    // Automatic validation - if this call doesn't go through on the server, no slot/GUI actions will be submitted
    @Inject(method = "squaredDistanceTo(DDD)D", cancellable = true, at = @At("HEAD"))
    private void calibrated$fakeDistance(double x, double y, double z, CallbackInfoReturnable<Double> cir) {
        if (calibrated$syncedPos == null) {
            return;
        }
        // This is an important math method that can be used elsewhere, so make sure we're targeting the synced position
        if (calibrated$syncedPos.getX() == (int) (x - 0.5)
                && calibrated$syncedPos.getY() == (int) (y - 0.5)
                && calibrated$syncedPos.getZ() == (int) (z - 0.5)) {
            cir.setReturnValue(0.0);
        }
    }
}
