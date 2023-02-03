package com.acikek.calibrated.network;

import com.acikek.calibrated.CalibratedAccess;
import com.acikek.calibrated.util.RemoteUser;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public class CANetworking {

    public static final Identifier MODIFY_USING_SESSION = CalibratedAccess.id("modify_using_session");

    public static void s2cModifyUsingSession(ServerPlayerEntity to, UUID session, BlockPos syncedPos) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeUuid(session);
        buf.writeBoolean(syncedPos != null);
        if (syncedPos != null) {
            buf.writeBlockPos(syncedPos);
        }
        ServerPlayNetworking.send(to, MODIFY_USING_SESSION, buf);
    }

    @Environment(EnvType.CLIENT)
    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(MODIFY_USING_SESSION, (client, handler, buf, responseSender) -> {
            RemoteUser remoteUser = ((RemoteUser) client.player);
            UUID session = buf.readUuid();
            if (buf.readBoolean()) {
                remoteUser.addUsingSession(session, buf.readBlockPos());
            }
            else {
                remoteUser.removeUsingSession(session);
            }
        });
    }
}
