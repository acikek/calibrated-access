package com.acikek.calibrated;

import com.acikek.calibrated.item.CAItems;
import com.acikek.calibrated.item.remote.RemoteItem;
import com.acikek.calibrated.sound.CASoundEvents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules;

public class CalibratedAccess implements ModInitializer {

    public static final String ID = "calibrated";
    public static GameRules.Key<GameRules.IntRule> MAX_REMOTE_SESSIONS;

    public static Identifier id(String path) {
        return new Identifier(ID, path);
    }

    public static final ItemGroup ITEM_GROUP = FabricItemGroupBuilder.create(id("main"))
            .icon(() -> new ItemStack(CAItems.NOVICE_ACCESSOR))
            .build();

    @Override
    public void onInitialize() {
        CAItems.register();
        CASoundEvents.register();
        RemoteItem.registerStats();
        registerGamerule();
    }

    public static void registerGamerule() {
        CalibratedAccess.MAX_REMOTE_SESSIONS = GameRuleRegistry.register(
                "maxRemoteSessions",
                GameRules.Category.MISC,
                GameRuleFactory.createIntRule(1)
        );
    }
}
