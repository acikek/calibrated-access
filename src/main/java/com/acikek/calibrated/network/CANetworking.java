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
    public static final Identifier SET_GAMERULES = CalibratedAccess.id("set_gamerules");

    public static void s2cModifySession(ServerPlayerEntity to, UUID session, SessionData data, boolean remove) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeUuid(session);
        buf.writeBoolean(remove);
        if (!remove) {
            buf.encodeAsJson(SessionData.CODEC, data);
        }
        ServerPlayNetworking.send(to, MODIFY_SESSION, buf);
    }

    public static void s2cSetGameRules(List<ServerPlayerEntity> to, boolean allow, int maxSessions) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(allow);
        buf.writeInt(maxSessions);
        for (ServerPlayerEntity player : to) {
            ServerPlayNetworking.send(player, SET_GAMERULES, buf);
        }
    }

    @Environment(EnvType.CLIENT)
    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(MODIFY_SESSION, (client, handler, buf, responseSender) -> {
            RemoteUser remoteUser = ((RemoteUser) client.player);
            final UUID session = buf.readUuid();
            final boolean remove = buf.readBoolean();
            final SessionData data = !remove ? buf.decodeAsJson(SessionData.CODEC) : null;
            client.execute(() -> {
                if (remove) {
                    remoteUser.calibrated$getSessions().remove(session);
                }
                else {
                    remoteUser.calibrated$getSessions().put(session, data);
                }
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(SET_GAMERULES, (client, handler, buf, responseSender) -> {
            final boolean allowAccess = buf.readBoolean();
            final int maxSessions = buf.readInt();
            client.execute(() -> {
                CalibratedAccessClient.allowAccess = allowAccess;
                CalibratedAccessClient.maxSessions = maxSessions;
            });
        });
    }
}
