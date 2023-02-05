package com.acikek.calibrated.gamerule;

import com.acikek.calibrated.client.CalibratedAccessClient;
import com.acikek.calibrated.network.CANetworking;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

import java.util.List;

public class CAGameRules implements ServerPlayConnectionEvents.Join {

    public static GameRules.Key<GameRules.IntRule> MAX_SESSIONS;
    public static GameRules.Key<GameRules.BooleanRule> ALLOW_ID_MISMATCH;

    public static int getMaxSessions(World world) {
        return world.isClient()
                ? CalibratedAccessClient.maxSessions
                : world.getGameRules().getInt(MAX_SESSIONS);
    }

    @Override
    public void onPlayReady(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) {
        CANetworking.s2cSetMaxSessions(List.of(handler.player), server.getGameRules().getInt(MAX_SESSIONS));
    }

    public static void register() {
        MAX_SESSIONS = GameRuleRegistry.register(
                "maxRemoteSessions",
                GameRules.Category.MISC,
                GameRuleFactory.createIntRule(1, 1, (server, rule) ->
                    CANetworking.s2cSetMaxSessions(server.getPlayerManager().getPlayerList(), rule.get())
                )
        );
        ALLOW_ID_MISMATCH = GameRuleRegistry.register(
                "allowSyncedIdMismatch",
                GameRules.Category.MISC,
                GameRuleFactory.createBooleanRule(true)
        );
        ServerPlayConnectionEvents.JOIN.register(new CAGameRules());
    }
}
