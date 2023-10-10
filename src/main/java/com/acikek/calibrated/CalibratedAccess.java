package com.acikek.calibrated;

import com.acikek.calibrated.gamerule.CAGameRules;
import com.acikek.calibrated.item.CAItems;
import com.acikek.calibrated.item.remote.RemoteItem;
import com.acikek.calibrated.sound.CASoundEvents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CalibratedAccess implements ModInitializer {

    public static final String ID = "calibrated";

    public static final Logger LOGGER = LogManager.getLogger(ID);

    public static Identifier id(String path) {
        return new Identifier(ID, path);
    }

    public static final RegistryKey<ItemGroup> ITEM_GROUP_KEY = RegistryKey.of(RegistryKeys.ITEM_GROUP, id("main"));
    public static final ItemGroup ITEM_GROUP = FabricItemGroup.builder()
            .icon(() -> new ItemStack(CAItems.NOVICE_ACCESSOR))
            .displayName(Text.translatable("itemGroup.calibrated.main"))
            .build();

    public static boolean isPehkuiEnabled;

    @Override
    public void onInitialize() {
        isPehkuiEnabled = FabricLoader.getInstance().isModLoaded("pehkui");
        LOGGER.info("Calibrating Access...");
        Registry.register(Registries.ITEM_GROUP, id("main"), ITEM_GROUP);
        CAItems.register();
        CASoundEvents.register();
        CAGameRules.register();
        RemoteItem.registerStats();
        CAVanillaIntegration.register();
    }
}
