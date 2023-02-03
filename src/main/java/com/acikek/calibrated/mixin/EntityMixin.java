package com.acikek.calibrated.mixin;

import com.acikek.calibrated.util.RemoteUser;
import com.acikek.calibrated.util.SessionData;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Mixin(Entity.class)
public class EntityMixin implements RemoteUser {

    // TODO maybe possibly needs synchronization (the java feature)
    private final Map<UUID, SessionData> calibrated$sessions = new LinkedHashMap<>();

    @Override
    public SessionData addSession(UUID session, SessionData data) {
        calibrated$sessions.put(session, data);
        if (calibrated$sessions.size() > 3) {
            UUID removingSession = calibrated$sessions.entrySet().iterator().next().getKey();
            calibrated$sessions.remove(removingSession);
        }
        return data;
    }

    @Override
    public void setSessionData(UUID session, SessionData data) {
        calibrated$sessions.put(session, data);
    }

    @Override
    public SessionData activateSession(UUID session, int ticks) {
        SessionData data = calibrated$sessions.get(session);
        data.active = true;
        data.ticks = ticks;
        return data;
    }

    @Override
    public void removeSession(UUID session) {
        calibrated$sessions.remove(session);
    }

    @Override
    public SessionData getSession(UUID session) {
        return calibrated$sessions.get(session);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void calibrated$serverTickDownAccess(CallbackInfo ci) {
        for (SessionData ticker : calibrated$sessions.values()) {
            if (!ticker.active) {
                continue;
            }
            ticker.ticks--;
            if (ticker.ticks == 0) {
                ticker.active = false;
            }
        }
    }

    // Screen handlers call this method in some way, just not consistently.
    // Automatic validation - if this call doesn't go through on the server, no slot/GUI actions will be submitted
    @Inject(method = "squaredDistanceTo(DDD)D", cancellable = true, at = @At("HEAD"))
    private void calibrated$fakeDistance(double x, double y, double z, CallbackInfoReturnable<Double> cir) {
        for (SessionData data : calibrated$sessions.values()) {
            // Inactive sessions do not need to be counted in this search
            if (!data.active) {
                continue;
            }
            // This is an important math method that can be used elsewhere, so make sure we're targeting the synced position
            BlockPos pos = data.syncedPos;
            if (pos.getX() == (int) (x - 0.5)
                    && pos.getY() == (int) (y - 0.5)
                    && pos.getZ() == (int) (z - 0.5)) {
                cir.setReturnValue(0.0);
            }
        }
    }

    @Inject(method = "writeNbt", at = @At("TAIL"))
    private void calibrated$writeNbt(NbtCompound nbt, CallbackInfoReturnable<NbtCompound> cir) {
        if (!calibrated$sessions.isEmpty()) {
            NbtList sessionEntries = new NbtList();
            for (Map.Entry<UUID, SessionData> entry : calibrated$sessions.entrySet()) {
                NbtCompound cpd = new NbtCompound();
                cpd.putUuid("Session", entry.getKey());
                cpd.put("Data", entry.getValue().toNbt());
                sessionEntries.add(cpd);
            }
            nbt.put("calibrated$Sessions", sessionEntries);
        }
    }

    @Inject(method = "readNbt", at = @At("TAIL"))
    private void calibrated$readNbt(NbtCompound nbt, CallbackInfo ci) {
        NbtList sessions = nbt.getList("calibrated$Sessions", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < sessions.size(); i++) {
            NbtCompound entry = sessions.getCompound(i);
            calibrated$sessions.put(entry.getUuid("Session"), SessionData.fromNbt(nbt.getCompound("Data")));
        }
    }
}
