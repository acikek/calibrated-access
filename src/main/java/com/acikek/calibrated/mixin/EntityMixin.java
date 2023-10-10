package com.acikek.calibrated.mixin;

import com.acikek.calibrated.CalibratedAccess;
import com.acikek.calibrated.util.RemoteUser;
import com.acikek.calibrated.util.SessionData;
import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mixin(Entity.class)
public abstract class EntityMixin implements RemoteUser {

    @Unique
    private static final String NBT_KEY = "calibrated$sessions";

    @Unique
    private Map<UUID, SessionData> calibrated$sessions = null;

    @Override
    public boolean calibrated$hasSessions() {
        return calibrated$sessions != null && !calibrated$sessions.isEmpty();
    }

    @Override
    public Map<UUID, SessionData> calibrated$getSessions() {
        if (calibrated$sessions == null) {
            calibrated$sessions = new LinkedHashMap<>();
        }
        return calibrated$sessions;
    }

    @Override
    public List<SessionData> calibrated$addSession(UUID session, SessionData data, int maxSessions) {
        calibrated$getSessions().put(session, data);
        List<SessionData> removed = new ArrayList<>();
        if (calibrated$sessions.size() > maxSessions) {
            var iter = calibrated$sessions.entrySet().iterator();
            List<UUID> toRemove = new ArrayList<>();
            for (int i = 0; i < calibrated$sessions.size() - maxSessions; i++) {
                toRemove.add(iter.next().getKey());
            }
            for (UUID uuid : toRemove) {
                var removedData = calibrated$sessions.remove(uuid);
                removed.add(removedData);
            }
        }
        return removed;
    }

    @Override
    public SessionData calibrated$activateSession(UUID session, int ticks) {
        SessionData data = calibrated$getSessions().get(session);
        data.active = true;
        data.ticks = ticks;
        return data;
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

    @Inject(method = "writeNbt", at = @At("TAIL"))
    private void calibrated$writeNbt(NbtCompound nbt, CallbackInfoReturnable<NbtCompound> cir) {
        if (!calibrated$hasSessions()) {
            return;
        }
        var sessions = calibrated$sessions.entrySet().stream()
                .map(entry -> Pair.of(entry.getKey(), entry.getValue()))
                .toList();
        var encoded = SessionData.LIST_CODEC.encodeStart(NbtOps.INSTANCE, sessions)
                .getOrThrow(true, Util.addPrefix("Failed to serialize sessions: ", CalibratedAccess.LOGGER::error));
        nbt.put(NBT_KEY, encoded);
    }

    @Inject(method = "readNbt", at = @At("TAIL"))
    private void calibrated$readNbt(NbtCompound nbt, CallbackInfo ci) {
        if (!nbt.contains(NBT_KEY)) {
            return;
        }
        var sessions = SessionData.LIST_CODEC.decode(NbtOps.INSTANCE, nbt.getCompound(NBT_KEY))
                .getOrThrow(true, Util.addPrefix("Failed to deserialize sessions: ", CalibratedAccess.LOGGER::error))
                .getFirst();
        for (var session : sessions) {
            calibrated$getSessions().put(session.getFirst(), session.getSecond());
        }
    }
}
