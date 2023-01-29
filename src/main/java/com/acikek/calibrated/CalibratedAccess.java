package com.acikek.calibrated;

import com.acikek.calibrated.item.ModItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;

public class CalibratedAccess implements ModInitializer {

    public static final String ID = "calibrated";

    public static Identifier id(String path) {
        return new Identifier(ID, path);
    }

    public static final ItemGroup ITEM_GROUP = FabricItemGroupBuilder.create(id("main"))
            .icon(ModItems.NOVICE_ACCESSOR::getDefaultStack)
            .build();

    @Override
    public void onInitialize() {
        ModItems.register();
    }
}
