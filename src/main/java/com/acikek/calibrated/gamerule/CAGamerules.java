package com.acikek.calibrated.gamerule;

import com.acikek.calibrated.client.CalibratedAccessClient;
import com.acikek.calibrated.network.CANetworking;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

public class CAGamerules {

    public static GameRules.Key<GameRules.IntRule> MAX_SESSIONS;

    public static int getMaxSessions(World world) {
        return world.isClient()
                ? CalibratedAccessClient.maxSessions
                : world.getGameRules().getInt(MAX_SESSIONS);
    }

    public static void register() {
        MAX_SESSIONS = GameRuleRegistry.register(
                "maxRemoteSessions",
                GameRules.Category.MISC,
                GameRuleFactory.createIntRule(1, (server, rule) ->
                    CANetworking.s2cSetMaxSessions(server.getPlayerManager().getPlayerList(), rule.get())
                )
        );
    }
}
