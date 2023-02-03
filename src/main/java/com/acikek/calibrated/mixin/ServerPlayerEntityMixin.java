package com.acikek.calibrated.mixin;

import com.acikek.calibrated.util.AccessTicker;
import com.acikek.calibrated.util.RemoteUser;
import com.acikek.calibrated.util.SessionMapHelper;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin implements AccessTicker {

    private final Map<UUID, Integer> calibrated$accessTicks = new HashMap<>();

    @Override
    public void setAccessTicks(UUID session, int accessTicks) {
        calibrated$accessTicks.put(session, accessTicks);
    }

    @Override
    public int getAccessTicks(UUID session) {
        return calibrated$accessTicks.get(session);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void calibrated$tickDownAccess(CallbackInfo ci) {
        // Small optimization to not instantiate a new removal list every tick
        List<UUID> toRemove = null;
        for (Map.Entry<UUID, Integer> ticker : calibrated$accessTicks.entrySet()) {
            if (ticker.getValue() <= 0) {
                return;
            }
            ticker.setValue(ticker.getValue() - 1);
            if (ticker.getValue() == 0) {
                ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
                RemoteUser.removeUsingSession(player, ticker.getKey());
                if (toRemove == null) {
                    toRemove = new ArrayList<>();
                    toRemove.add(ticker.getKey());
                }
            }
        }
        if (toRemove != null) {
            for (UUID session : toRemove) {
                calibrated$accessTicks.remove(session);
            }
        }
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void calibrated$writeTicks(NbtCompound nbt, CallbackInfo ci) {
        if (!calibrated$accessTicks.isEmpty()) {
            nbt.put("calibrated$AccessTicks", SessionMapHelper.toNbt(calibrated$accessTicks, (cpd, i) -> cpd.putInt("Ticks", i)));
        }
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void calibrated$readTicks(NbtCompound nbt, CallbackInfo ci) {
        calibrated$accessTicks.putAll(SessionMapHelper.readNbt(nbt, "calibrated$AccessTicks", cpd -> cpd.getInt("Ticks")));
    }
}
