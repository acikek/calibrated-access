package com.acikek.calibrated.util;

import com.acikek.calibrated.api.session.SessionView;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;

public class SessionData implements SessionView {

    public BlockPos syncedPos;
    public boolean active;
    public int ticks;

    public SessionData(BlockPos syncedPos, boolean active, int ticks) {
        this.syncedPos = syncedPos;
        this.active = active;
        this.ticks = ticks;
    }

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putLong("SyncedPos", syncedPos.asLong());
        nbt.putBoolean("Active", active);
        nbt.putInt("Ticks", ticks);
        return nbt;
    }

    public static SessionData fromNbt(NbtCompound nbt) {
        return new SessionData(
                BlockPos.fromLong(nbt.getLong("SyncedPos")),
                nbt.getBoolean("Active"),
                nbt.getInt("Ticks")
        );
    }

    public void write(PacketByteBuf buf) {
        buf.writeBlockPos(syncedPos);
        buf.writeBoolean(active);
        buf.writeInt(ticks);
    }

    public static SessionData read(PacketByteBuf buf) {
        return new SessionData(
                buf.readBlockPos(),
                buf.readBoolean(),
                buf.readInt()
        );
    }

    @Override
    public BlockPos syncedPos() {
        return syncedPos;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public int remainingTicks() {
        return ticks;
    }

    @Override
    public String toString() {
        return "SessionData{" +
                "syncedPos=" + syncedPos +
                ", active=" + active +
                ", ticks=" + ticks +
                '}';
    }
}
