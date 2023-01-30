package com.acikek.calibrated.mixin;

import com.acikek.calibrated.util.RemoteUser;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(Entity.class)
public class EntityMixin implements RemoteUser {

    private BlockPos calibrated$syncedPos;
    private UUID calibrated$session;

    @Override
    public void setUsingRemote(BlockPos syncedPos) {
        calibrated$syncedPos = syncedPos;
    }

    @Override
    public boolean isUsingRemote() {
        return calibrated$syncedPos != null;
    }

    @Override
    public void setSession(UUID uuid) {
        calibrated$session = uuid;
    }

    @Override
    public UUID getSession() {
        return calibrated$session;
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

    @Inject(method = "writeNbt", at = @At("TAIL"))
    private void calibrated$writeNbt(NbtCompound nbt, CallbackInfoReturnable<NbtCompound> cir) {
        if (calibrated$syncedPos != null) {
            nbt.putLong("calibrated$SyncedPos", calibrated$syncedPos.asLong());
        }
        if (calibrated$session != null) {
            nbt.putUuid("calibrated$Session", calibrated$session);
        }
    }

    @Inject(method = "readNbt", at = @At("TAIL"))
    private void calibrated$readNbt(NbtCompound nbt, CallbackInfo ci) {
        long syncedPosLong = nbt.getLong("calibrated$SyncedPos");
        if (syncedPosLong != 0L) {
            calibrated$syncedPos = BlockPos.fromLong(syncedPosLong);
        }
        if (nbt.containsUuid("calibrated$Session")) {
            calibrated$session = nbt.getUuid("calibrated$Session");
        }
    }
}
