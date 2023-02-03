package com.acikek.calibrated.util;

import com.acikek.calibrated.network.CANetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public interface RemoteUser {

    SessionData addSession(UUID session, SessionData data);

    default SessionData addSession(UUID session, BlockPos syncedPos) {
        return addSession(session, new SessionData(syncedPos, false, 0));
    }

    void setSessionData(UUID session, SessionData data);

    SessionData activateSession(UUID session, int ticks);

    void removeSession(UUID session);

    SessionData getSession(UUID session);

    default boolean hasSession(UUID session) {
        return getSession(session) != null;
    }

    default boolean isSessionActive(UUID session) {
        return getSession(session).active;
    }

    static void addSession(ServerPlayerEntity player, UUID session, BlockPos syncedPos) {
        SessionData data = ((RemoteUser) player).addSession(session, syncedPos);
        CANetworking.s2cModifySession(player, session, data, CANetworking.SessionModifier.ADD);
    }

    static void activateSession(ServerPlayerEntity player, UUID session, int ticks) {
        SessionData data = ((RemoteUser) player).activateSession(session, ticks);
        CANetworking.s2cModifySession(player, session, data, CANetworking.SessionModifier.SET);
    }

    static void removeSession(ServerPlayerEntity player, UUID session) {
        ((RemoteUser) player).removeSession(session);
        CANetworking.s2cModifySession(player, session, null, CANetworking.SessionModifier.REMOVE);
    }
}
