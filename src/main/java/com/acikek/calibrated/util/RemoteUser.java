package com.acikek.calibrated.util;

import com.acikek.calibrated.network.CANetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public interface RemoteUser {

    void calibrated$addSession(UUID session, SessionData data, int maxSessions);

    default void calibrated$addSession(UUID session, BlockPos syncedPos, int maxSessions) {
        calibrated$addSession(session, new SessionData(syncedPos, false, 0), maxSessions);
    }

    void calibrated$setSessionData(UUID session, SessionData data);

    SessionData calibrated$activateSession(UUID session, int ticks);

    void calibrated$removeSession(UUID session);

    SessionData calibrated$getSession(UUID session);

    default boolean calibrated$hasSession(UUID session) {
        return calibrated$getSession(session) != null;
    }

    default boolean calibrated$isSessionActive(UUID session) {
        return calibrated$getSession(session).active;
    }

    static void activateSession(ServerPlayerEntity player, UUID session, int ticks) {
        SessionData data = ((RemoteUser) player).calibrated$activateSession(session, ticks);
        CANetworking.s2cModifySession(player, session, data, false);
    }

    static void removeSession(ServerPlayerEntity player, UUID session) {
        ((RemoteUser) player).calibrated$removeSession(session);
        CANetworking.s2cModifySession(player, session, null, true);
    }
}
