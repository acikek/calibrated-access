package com.acikek.calibrated.item;

import com.acikek.calibrated.CalibratedAccess;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;

public class ModItems {

    public static final Item CALIBRATOR = new Item(settings());
    public static final RemoteItem NOVICE_ACCESSOR = new RemoteItem(settings(), 3, false, false);
    public static final RemoteItem SKILLED_ACCESSOR = new RemoteItem(settings(), 7, true, false);
    public static final RemoteItem EXPERT_ACCESSOR = new RemoteItem(settings(), 15, true, false);
    public static final RemoteItem UNLIMITED_ACCESSOR = new RemoteItem(settings(), 0, true, true);

    public static Item.Settings settings() {
        return new FabricItemSettings().group(CalibratedAccess.ITEM_GROUP);
    }

    public static void register(String name, Item item) {
        Registry.register(Registry.ITEM, CalibratedAccess.id(name), item);
    }

    public static void register() {
        register("calibrator", CALIBRATOR);
        register("novice_accessor", NOVICE_ACCESSOR);
        register("skilled_accessor", SKILLED_ACCESSOR);
        register("expert_accessor", EXPERT_ACCESSOR);
        register("unlimited_accessor", UNLIMITED_ACCESSOR);
    }
}
