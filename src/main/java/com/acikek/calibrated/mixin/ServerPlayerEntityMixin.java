package com.acikek.calibrated.mixin;

import com.acikek.calibrated.network.CalibratedAccessNetworking;
import com.acikek.calibrated.util.AccessTicker;
import com.acikek.calibrated.util.RemoteUser;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin implements AccessTicker {

    private int calibrated$accessTicks;

    @Override
    public void setAccessTicks(int accessTicks) {
        calibrated$accessTicks = accessTicks;
    }

    @Override
    public int getAccessTicks() {
        return calibrated$accessTicks;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void calibrated$tickDownAccess(CallbackInfo ci) {
        if (calibrated$accessTicks <= 0) {
            return;
        }
        calibrated$accessTicks--;
        if (calibrated$accessTicks == 0) {
            ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
            ((RemoteUser) player).setUsingRemote(null, null);
            CalibratedAccessNetworking.s2cSetUsingRemote(player, null, null);
        }
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void calibrated$writeTicks(NbtCompound nbt, CallbackInfo ci) {
        if (isAccessing()) {
            nbt.putInt("calibrated$AccessTicks", calibrated$accessTicks);
        }
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void calibrated$readTicks(NbtCompound nbt, CallbackInfo ci) {
        calibrated$accessTicks = nbt.getInt("calibrated$accessTicks");
    }
}
