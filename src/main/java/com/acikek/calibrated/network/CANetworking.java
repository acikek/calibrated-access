package com.acikek.calibrated.network;

import com.acikek.calibrated.CalibratedAccess;
import com.acikek.calibrated.client.CalibratedAccessClient;
import com.acikek.calibrated.util.RemoteUser;
import com.acikek.calibrated.util.SessionData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.UUID;

public class CANetworking {

    public static final Identifier MODIFY_SESSION = CalibratedAccess.id("modify_session");
    public static final Identifier SET_MAX_SESSIONS = CalibratedAccess.id("set_max_sessions");

    public static void s2cModifySession(ServerPlayerEntity to, UUID session, SessionData data, boolean remove) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeUuid(session);
        buf.writeBoolean(remove);
        if (!remove) {
            data.write(buf);
        }
        ServerPlayNetworking.send(to, MODIFY_SESSION, buf);
    }

    public static void s2cSetMaxSessions(List<ServerPlayerEntity> to, int maxSessions) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(maxSessions);
        for (ServerPlayerEntity player : to) {
            ServerPlayNetworking.send(player, SET_MAX_SESSIONS, buf);
        }
    }

    @Environment(EnvType.CLIENT)
    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(MODIFY_SESSION, (client, handler, buf, responseSender) -> {
            RemoteUser remoteUser = ((RemoteUser) client.player);
            UUID session = buf.readUuid();
            if (buf.readBoolean()) {
                remoteUser.removeSession(session);
            }
            else {
                remoteUser.setSessionData(session, SessionData.read(buf));
            }
        });
        ClientPlayNetworking.registerGlobalReceiver(SET_MAX_SESSIONS, (client, handler, buf, responseSender) ->
                CalibratedAccessClient.maxSessions = buf.readInt()
        );
    }
}
