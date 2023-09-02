package com.acikek.calibrated.mixin;

import com.acikek.calibrated.CalibratedAccess;
import com.acikek.calibrated.util.RemoteUser;
import com.acikek.calibrated.util.SessionData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@Mixin(Entity.class)
public abstract class EntityMixin implements RemoteUser {

    @Shadow public abstract float getEyeHeight(EntityPose pose);

    @Shadow public abstract EntityPose getPose();

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
    public void calibrated$addSession(UUID session, SessionData data, int maxSessions) {
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

    /**
     * Compares a calibrated block position value with a coordinate value passed in by {@link Entity#squaredDistanceTo(double, double, double)}.
     * <p>
     * When validating screen interactions, Vanilla increments each block coordinate by {@code 0.5}, resulting in the block's center.
     * An earlier version of this check was to remove the {@code 0.5} offsets and compare them against the ints of the block position.
     * <p>
     * With <a href="https://modrinth.com/mod/pehkui">Pehkui</a>, however, that method fails; Pehkui in particular fine-tunes the {@code squaredDistanceTo} values
     * so that reach calculations are more precise at smaller scales. Flooring the passed in values and checking against those
     * also fails as the values can sometimes 'bleed into' the next block coordinate, such as {@code 5} becoming {@code 6.0},
     * of course with a floored value of {@code 6.0} instead of {@code 5.0}. It also takes eye level into account for the Y value.
     * <p>
     * As these are two very different sets of values, a broad comparison covering both cases is not sufficient. This method
     * uses a manual compatibility implementation.
     */
    @Unique
    private boolean calibrated$compare(int blockPosValue, double providedValue, double eyeOffset) {
        return CalibratedAccess.isPehkuiEnabled
                ? Math.abs((blockPosValue + 0.5 - eyeOffset) - providedValue) <= 0.5
                : blockPosValue == (int) (Math.floor(providedValue));
    }

    // Screen handlers call this method in some way, just not consistently.
    // Automatic validation - if this call doesn't go through on the server, no slot/GUI actions will be submitted
    @Inject(method = "squaredDistanceTo(DDD)D", cancellable = true, at = @At("HEAD"))
    private void calibrated$fakeDistance(double x, double y, double z, CallbackInfoReturnable<Double> cir) {
        if (!calibrated$hasSessions()) {
            return;
        }
        for (SessionData data : calibrated$sessions.values()) {
            // Inactive sessions do not need to be counted in this search
            if (!data.active) {
                continue;
            }
            // This is an important math method that can be used elsewhere, so make sure we're targeting the synced position
            BlockPos pos = data.syncedPos;
            // Only calculate eye offset if Pehkui is enabled for the relevant check with it
            double eyeOffset = CalibratedAccess.isPehkuiEnabled ? getEyeHeight(getPose()) : 0.0;
            if (calibrated$compare(pos.getX(), x, 0.0)
                    && calibrated$compare(pos.getY(), y, eyeOffset)
                    && calibrated$compare(pos.getZ(), z, 0.0)) {
                cir.setReturnValue(0.0);
            }
        }
    }

    @Inject(method = "writeNbt", at = @At("TAIL"))
    private void calibrated$writeNbt(NbtCompound nbt, CallbackInfoReturnable<NbtCompound> cir) {
        if (!calibrated$hasSessions()) {
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
