package com.acikek.calibrated.mixin;

import com.acikek.calibrated.util.RemoteUser;
import com.acikek.calibrated.util.SessionMapHelper;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
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

    // The current sessions are set whenever a player calibrates with a remote. A "using" session is set whenever
    // a player *uses* a remote; this session is ticked down in the server player mixin and removed when the grace
    // period is over. IF the current sessions are full, a player can use the remote whose session is about to be dequeued,
    // calibrate with a new remote, and then try to use the new one, in which case these session types would differ.

    private final Map<UUID, BlockPos> calibrated$usingSessions = new HashMap<>();

    private final Queue<UUID> calibrated$currentSessions = new ArrayDeque<>();

    @Override
    public void addUsingSession(UUID session, BlockPos syncedPos) {
        calibrated$usingSessions.put(session, syncedPos);
    }

    @Override
    public void removeUsingSession(UUID session) {
        calibrated$usingSessions.remove(session);
    }

    @Override
    public void addSession(UUID uuid) {
        calibrated$currentSessions.add(uuid);
        if (calibrated$currentSessions.size() > 3) {
            calibrated$currentSessions.remove();
        }
    }

    @Override
    public boolean hasUsingSession(UUID session) {
        return calibrated$usingSessions.containsKey(session);
    }

    @Override
    public boolean hasCurrentSession(UUID session) {
        return calibrated$currentSessions.contains(session);
    }

    // Screen handlers call this method in some way, just not consistently.
    // Automatic validation - if this call doesn't go through on the server, no slot/GUI actions will be submitted
    @Inject(method = "squaredDistanceTo(DDD)D", cancellable = true, at = @At("HEAD"))
    private void calibrated$fakeDistance(double x, double y, double z, CallbackInfoReturnable<Double> cir) {
        for (BlockPos pos : calibrated$usingSessions.values()) {
            // This is an important math method that can be used elsewhere, so make sure we're targeting the synced position
            if (pos.getX() == (int) (x - 0.5)
                    && pos.getY() == (int) (y - 0.5)
                    && pos.getZ() == (int) (z - 0.5)) {
                cir.setReturnValue(0.0);
            }
        }
    }

    @Inject(method = "writeNbt", at = @At("TAIL"))
    private void calibrated$writeNbt(NbtCompound nbt, CallbackInfoReturnable<NbtCompound> cir) {
        if (!calibrated$usingSessions.isEmpty()) {
            nbt.put("calibrated$UsingSessions", SessionMapHelper.toNbt(
                    calibrated$usingSessions,
                    (cpd, pos) -> cpd.putLong("SyncedPos", pos.asLong()))
            );
        }
        if (!calibrated$currentSessions.isEmpty()) {
            NbtList currentSessions = new NbtList();
            for (UUID session : calibrated$currentSessions) {
                currentSessions.add(NbtHelper.fromUuid(session));
            }
            nbt.put("calibrated$CurrentSessions", currentSessions);
        }
    }

    @Inject(method = "readNbt", at = @At("TAIL"))
    private void calibrated$readNbt(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("calibrated$UsingSessions")) {
            calibrated$usingSessions.putAll(SessionMapHelper.readNbt(
                    nbt, "calibrated$UsingSessions",
                    cpd -> BlockPos.fromLong(cpd.getLong("SyncedPos")))
            );
        }
        if (nbt.contains("calibrated$CurrentSessions")) {
            NbtList currentSessions = nbt.getList("calibrated$CurrentSessions", NbtElement.INT_ARRAY_TYPE);
            for (NbtElement element : currentSessions) {
                calibrated$currentSessions.add(NbtHelper.toUuid(element));
            }
        }
    }
}
