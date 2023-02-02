package com.acikek.calibrated.item;

import com.acikek.calibrated.CalibratedAccess;
import com.acikek.calibrated.item.remote.RemoteItem;
import com.acikek.calibrated.item.remote.RemoteType;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.List;

public class CAItems {

    public static List<RemoteItem> remotes = new ArrayList<>();

    public static final Item CALIBRATOR = new Item(settings());
    public static final RemoteItem NOVICE_ACCESSOR = new RemoteItem(settings(), RemoteType.NOVICE);
    public static final RemoteItem SKILLED_ACCESSOR = new RemoteItem(settings().rarity(Rarity.UNCOMMON), RemoteType.SKILLED);
    public static final RemoteItem EXPERT_ACCESSOR = new RemoteItem(settings().rarity(Rarity.UNCOMMON), RemoteType.EXPERT);
    public static final RemoteItem UNLIMITED_ACCESSOR = new RemoteItem(settings().rarity(Rarity.RARE).fireproof(), RemoteType.UNLIMITED);

    public static Item.Settings settings() {
        return new FabricItemSettings()
                .group(CalibratedAccess.ITEM_GROUP)
                .maxCount(1);
    }

    public static void register(String name, Item item) {
        Registry.register(Registry.ITEM, CalibratedAccess.id(name), item);
        if (item instanceof RemoteItem remoteItem) {
            remotes.add(remoteItem);
        }
    }

    public static void register() {
        register("calibrator", CALIBRATOR);
        register("novice_accessor", NOVICE_ACCESSOR);
        register("skilled_accessor", SKILLED_ACCESSOR);
        register("expert_accessor", EXPERT_ACCESSOR);
        register("unlimited_accessor", UNLIMITED_ACCESSOR);
    }
}
