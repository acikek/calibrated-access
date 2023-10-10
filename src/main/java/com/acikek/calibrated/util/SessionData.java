package com.acikek.calibrated.util;

import com.acikek.calibrated.api.session.SessionView;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SessionData implements SessionView {

    public BlockPos syncedPos;
    public RegistryKey<World> worldKey;
    public boolean active;
    public int ticks;

    public static final Codec<SessionData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BlockPos.CODEC.fieldOf("synced_pos").forGetter(SessionView::syncedPos),
                    RegistryKey.createCodec(RegistryKeys.WORLD).fieldOf("world_key").forGetter(SessionView::worldKey),
                    Codec.BOOL.fieldOf("active").forGetter(SessionView::isActive),
                    Codec.INT.fieldOf("ticks").forGetter(SessionView::remainingTicks)
            ).apply(instance, SessionData::new)
    );

    // Preserve order
    public static final Codec<List<Pair<UUID, SessionData>>> LIST_CODEC = Codec.list(Codec.pair(
            Uuids.STRING_CODEC.fieldOf("uuid").codec(),
            CODEC.fieldOf("session_data").codec()
    ));

    public SessionData(BlockPos syncedPos, RegistryKey<World> worldKey, boolean active, int ticks) {
        this.syncedPos = syncedPos;
        this.worldKey = worldKey;
        this.active = active;
        this.ticks = ticks;
    }

    @Override
    public BlockPos syncedPos() {
        return syncedPos;
    }

    @Override
    public RegistryKey<World> worldKey() {
        return worldKey;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public int remainingTicks() {
        return ticks;
    }
}
