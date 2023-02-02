package com.acikek.calibrated.item.remote;

import net.minecraft.item.Item;
import net.minecraft.item.Items;

public record RemoteType(String name, int accesses, boolean interdimensional, boolean unlimited, Item casingMaterial,
                         Item antennaMaterial, RemoteType upgradeFrom, Item upgradeMaterial) {

    public static final RemoteType NOVICE = normal("novice", 3, false, Items.IRON_INGOT, Items.COPPER_INGOT);
    public static final RemoteType SKILLED = normal("skilled", 7, true, Items.GOLD_INGOT, Items.QUARTZ);
    public static final RemoteType EXPERT = normal("expert", 15, true, Items.DIAMOND, Items.OBSIDIAN);
    public static final RemoteType UNLIMITED = unlimited("unlimited", EXPERT, Items.NETHERITE_INGOT);

    public static RemoteType normal(String name, int accesses, boolean interdimensional, Item casingMaterial, Item antennaMaterial) {
        return new RemoteType(name, accesses, interdimensional, false, casingMaterial, antennaMaterial, null, null);
    }

    public static RemoteType unlimited(String name, RemoteType upgradeFrom, Item upgradeMaterial) {
        return new RemoteType(name, 0, true, true, null, null, upgradeFrom, upgradeMaterial);
    }
}
