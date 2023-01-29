package com.acikek.calibrated.network;

import com.acikek.calibrated.CalibratedAccess;
import com.acikek.calibrated.util.RemoteScreenPlayer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class CalibratedAccessNetworking {

    public static final Identifier SET_USING_REMOTE = CalibratedAccess.id("set_using_remote");

    public static void s2cSetUsingRemote(ServerPlayerEntity to, boolean usingRemote) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(usingRemote);
        ServerPlayNetworking.send(to, SET_USING_REMOTE, buf);
    }

    @Environment(EnvType.CLIENT)
    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(SET_USING_REMOTE, (client, handler, buf, responseSender) ->
            ((RemoteScreenPlayer) client.player).setUsingRemote(buf.readBoolean())
        );
    }
}
