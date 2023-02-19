package com.acikek.calibrated.gamerule;

import com.acikek.calibrated.client.CalibratedAccessClient;
import com.acikek.calibrated.network.CANetworking;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

import java.util.List;

public class CAGameRules implements ServerPlayConnectionEvents.Join {

    // Synced
    public static GameRules.Key<GameRules.BooleanRule> ALLOW_ACCESS;
    public static GameRules.Key<GameRules.IntRule> MAX_SESSIONS;

    // Server-side
    public static GameRules.Key<GameRules.BooleanRule> ALLOW_ID_MISMATCH;

    public static boolean isAccessAllowed(World world) {
        return world.isClient()
                ? CalibratedAccessClient.allowAccess
                : world.getGameRules().getBoolean(ALLOW_ACCESS);
    }

    public static int getMaxSessions(World world) {
        return world.isClient()
                ? CalibratedAccessClient.maxSessions
                : world.getGameRules().getInt(MAX_SESSIONS);
    }

    public static void send(MinecraftServer server, List<ServerPlayerEntity> players, Boolean allow, Integer maxSessions) {
        GameRules rules = server.getGameRules();
        CANetworking.s2cSetGameRules(
                players != null ? players : server.getPlayerManager().getPlayerList(),
                allow != null ? allow : rules.getBoolean(ALLOW_ACCESS),
                maxSessions != null ? maxSessions : rules.getInt(MAX_SESSIONS)
        );
    }

    @Override
    public void onPlayReady(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) {
        send(server, List.of(handler.player), null, null);
    }

    public static void register() {
        ALLOW_ACCESS = GameRuleRegistry.register(
                "allowCalibratedAccess",
                GameRules.Category.MISC,
                GameRuleFactory.createBooleanRule(true, (server, rule) -> send(server, null, rule.get(), null))
        );
        MAX_SESSIONS = GameRuleRegistry.register(
                "maxRemoteSessions",
                GameRules.Category.MISC,
                GameRuleFactory.createIntRule(1, 1, (server, rule) -> send(server, null, null, rule.get()))
        );
        ALLOW_ID_MISMATCH = GameRuleRegistry.register(
                "allowSyncedIdMismatch",
                GameRules.Category.MISC,
                GameRuleFactory.createBooleanRule(true)
        );
        ServerPlayConnectionEvents.JOIN.register(new CAGameRules());
    }
}
