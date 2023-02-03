package com.acikek.calibrated.network;

import com.acikek.calibrated.CalibratedAccess;
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

import java.util.UUID;

public class CANetworking {

    public static final Identifier MODIFY_SESSION = CalibratedAccess.id("modify_session");

    public enum SessionModifier {
        ADD,
        SET,
        REMOVE
    }

    public static void s2cModifySession(ServerPlayerEntity to, UUID session, SessionData data, SessionModifier modifier) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeUuid(session);
        buf.writeEnumConstant(modifier);
        if (data != null) {
            data.write(buf);
        }
        ServerPlayNetworking.send(to, MODIFY_SESSION, buf);
    }

    @Environment(EnvType.CLIENT)
    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(MODIFY_SESSION, (client, handler, buf, responseSender) -> {
            RemoteUser remoteUser = ((RemoteUser) client.player);
            UUID session = buf.readUuid();
            SessionModifier modifier = buf.readEnumConstant(SessionModifier.class);
            switch (modifier) {
                case ADD -> remoteUser.addSession(session, SessionData.read(buf));
                case SET -> remoteUser.setSessionData(session, SessionData.read(buf));
                case REMOVE -> remoteUser.removeSession(session);
            }
        });
    }
}
