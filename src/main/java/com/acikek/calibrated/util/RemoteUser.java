package com.acikek.calibrated.util;

import com.acikek.calibrated.network.CANetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface RemoteUser {

    boolean calibrated$hasSessions();

    Map<UUID, SessionData> calibrated$getSessions();

    List<SessionData> calibrated$addSession(UUID session, SessionData data, int maxSessions);

    default List<SessionData> calibrated$addSession(UUID session, BlockPos syncedPos, World world, int maxSessions) {
        return calibrated$addSession(session, new SessionData(syncedPos, world.getRegistryKey(), false, 0), maxSessions);
    }

    SessionData calibrated$activateSession(UUID session, int ticks);

    static void activateSession(ServerPlayerEntity player, UUID session, int ticks) {
        SessionData data = ((RemoteUser) player).calibrated$activateSession(session, ticks);
        CANetworking.s2cModifySession(player, session, data, false);
    }

    static void removeSession(ServerPlayerEntity player, UUID session) {
        ((RemoteUser) player).calibrated$getSessions().remove(session);
        CANetworking.s2cModifySession(player, session, null, true);
    }
}
