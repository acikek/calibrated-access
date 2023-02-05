package com.acikek.calibrated;

import com.acikek.calibrated.api.CalibratedAccessAPI;
import com.acikek.calibrated.api.event.RemoteUseResults;
import com.acikek.calibrated.gamerule.CAGameRules;
import com.acikek.calibrated.item.CAItems;
import com.acikek.calibrated.item.remote.RemoteItem;
import com.acikek.calibrated.sound.CASoundEvents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;

public class CalibratedAccess implements ModInitializer {

    public static final String ID = "calibrated";

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
        CAGameRules.register();
        RemoteItem.registerStats();
    }
}
