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

import java.util.*;

@Mixin(Entity.class)
public class EntityMixin implements RemoteUser {

    private Map<UUID, SessionData> calibrated$sessions = null;

    private Map<UUID, SessionData> calibrated$getSessions() {
        if (calibrated$sessions == null) {
            calibrated$sessions = new LinkedHashMap<>();
        }
        return calibrated$sessions;
    }

    @Override
    public void addSession(UUID session, SessionData data, int maxSessions) {
        calibrated$getSessions().put(session, data);
        if (calibrated$sessions.size() > maxSessions) {
            var iter = calibrated$sessions.entrySet().iterator();
            List<UUID> toRemove = new ArrayList<>();
            for (int i = 0; i < calibrated$sessions.size() - maxSessions; i++) {
                toRemove.add(iter.next().getKey());
            }
            for (UUID uuid : toRemove) {
                calibrated$sessions.remove(uuid);
            }
        }
    }

    @Override
    public void setSessionData(UUID session, SessionData data) {
        calibrated$getSessions().put(session, data);
    }

    @Override
    public SessionData activateSession(UUID session, int ticks) {
        SessionData data = calibrated$getSessions().get(session);
        data.active = true;
        data.ticks = ticks;
        return data;
    }

    @Override
    public void removeSession(UUID session) {
        calibrated$getSessions().remove(session);
    }

    @Override
    public SessionData getSession(UUID session) {
        return calibrated$getSessions().get(session);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void calibrated$serverTickDownAccess(CallbackInfo ci) {
        if (calibrated$sessions == null) {
            return;
        }
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
        if (calibrated$sessions == null) {
            return;
        }
        for (SessionData data : calibrated$sessions.values()) {
            // Inactive sessions do not need to be counted in this search
            if (!data.active) {
                continue;
            }
            // This is an important math method that can be used elsewhere, so make sure we're targeting the synced position
            BlockPos pos = data.syncedPos;
            if (pos.getX() == (int) (Math.floor(x))
                    && pos.getY() == (int) (Math.floor(y))
                    && pos.getZ() == (int) (Math.floor(z))) {
                cir.setReturnValue(0.0);
            }
        }
    }

    @Inject(method = "writeNbt", at = @At("TAIL"))
    private void calibrated$writeNbt(NbtCompound nbt, CallbackInfoReturnable<NbtCompound> cir) {
        if (calibrated$sessions == null || calibrated$sessions.isEmpty()) {
            return;
        }
        NbtList sessionEntries = new NbtList();
        for (Map.Entry<UUID, SessionData> entry : calibrated$sessions.entrySet()) {
            NbtCompound cpd = new NbtCompound();
            cpd.putUuid("Session", entry.getKey());
            cpd.put("Data", entry.getValue().toNbt());
            sessionEntries.add(cpd);
        }
        nbt.put("calibrated$Sessions", sessionEntries);
    }

    @Inject(method = "readNbt", at = @At("TAIL"))
    private void calibrated$readNbt(NbtCompound nbt, CallbackInfo ci) {
        if (!nbt.contains("calibrated$Sessions")) {
            return;
        }
        NbtList sessions = nbt.getList("calibrated$Sessions", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < sessions.size(); i++) {
            NbtCompound entry = sessions.getCompound(i);
            calibrated$getSessions().put(entry.getUuid("Session"), SessionData.fromNbt(entry.getCompound("Data")));
        }
    }
}
