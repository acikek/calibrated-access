package com.acikek.calibrated.util;

import com.acikek.calibrated.network.CANetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public interface RemoteUser {

    // player calibrates remote:
        // adds UUID session to queue
    // remote is a "different remote" if the session is in the usingRemotes map but not in the sessions queue

    void addUsingSession(UUID session, BlockPos syncedPos);

    void removeUsingSession(UUID session);

    void addSession(UUID uuid);

    boolean hasUsingSession(UUID session);

    boolean hasCurrentSession(UUID session);

    static void addUsingSession(ServerPlayerEntity player, UUID session, BlockPos syncedPos) {
        ((RemoteUser) player).addUsingSession(session, syncedPos);
        CANetworking.s2cModifyUsingSession(player, session, syncedPos);
    }

    static void removeUsingSession(ServerPlayerEntity player, UUID session) {
        ((RemoteUser) player).removeUsingSession(session);
        CANetworking.s2cModifyUsingSession(player, session, null);
    }
}
