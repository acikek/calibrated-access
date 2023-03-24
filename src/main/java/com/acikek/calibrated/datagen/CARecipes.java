package com.acikek.calibrated.datagen;

import com.acikek.calibrated.CalibratedAccess;
import com.acikek.calibrated.item.CAItems;
import com.acikek.calibrated.item.remote.RemoteItem;
import com.acikek.calibrated.item.remote.RemoteType;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.server.RecipeProvider;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.data.server.recipe.SmithingRecipeJsonBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;

import java.util.function.Consumer;

public class CARecipes extends FabricRecipeProvider {

    public CARecipes(FabricDataGenerator generator) {
        super(generator);
    }

    public static void generateSmithing(String name, Item upgradeFrom, Item upgradeMaterial, Item result, Consumer<RecipeJsonProvider> exporter) {
        SmithingRecipeJsonBuilder.create(Ingredient.ofItems(upgradeFrom), Ingredient.ofItems(upgradeMaterial), result)
                .criterion("has_upgrade", RecipeProvider.conditionsFromItem(upgradeMaterial))
                .offerTo(exporter, CalibratedAccess.id(name));
    }

    public static void generateSmithing(Item item, RemoteType type, Consumer<RecipeJsonProvider> exporter) {
        generateSmithing(type.name() + "_accessor", type.upgradeFrom().get(), type.upgradeMaterial(), item, exporter);
    }

    public static void generateShaped(Item item, RemoteType type, Consumer<RecipeJsonProvider> exporter) {
        ShapedRecipeJsonBuilder.create(item)
                .pattern(" A ")
                .pattern("CXC")
                .pattern("CCC")
                .input('A', type.antennaMaterial())
                .input('C', type.casingMaterial())
                .input('X', CAItems.CALIBRATOR)
                .criterion("has_calibrator", RecipeProvider.conditionsFromItem(CAItems.CALIBRATOR))
                .offerTo(exporter);
    }

    public static void generate(RemoteItem item, Consumer<RecipeJsonProvider> exporter) {
        if (item.remoteType.isUpgrade()) {
            generateSmithing(item, item.remoteType, exporter);
            return;
        }
        generateShaped(item, item.remoteType, exporter);
    }

    @Override
    public void generateRecipes(Consumer<RecipeJsonProvider> exporter) {
        generateSmithing("calibrator", Items.ENDER_PEARL, Items.REDSTONE, CAItems.CALIBRATOR, exporter);
        for (RemoteItem item : CAItems.remotes) {
            generate(item, exporter);
        }
    }
}
