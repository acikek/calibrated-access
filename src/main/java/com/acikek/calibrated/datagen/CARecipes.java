package com.acikek.calibrated.datagen;

import com.acikek.calibrated.CalibratedAccess;
import com.acikek.calibrated.item.CAItems;
import com.acikek.calibrated.item.remote.RemoteItem;
import com.acikek.calibrated.item.remote.RemoteType;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.data.server.recipe.RecipeProvider;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.data.server.recipe.SmithingRecipeJsonBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.book.RecipeCategory;

import java.util.function.Consumer;

public class CARecipes extends FabricRecipeProvider {

    public CARecipes(FabricDataOutput output) {
        super(output);
    }

    public static void generateSmithing(String name, Item upgradeFrom, Item upgradeMaterial, Item result, RecipeCategory category, Consumer<RecipeJsonProvider> exporter) {
        SmithingRecipeJsonBuilder.create(Ingredient.ofItems(upgradeFrom), Ingredient.ofItems(upgradeMaterial), category, result)
                .criterion("has_upgrade", RecipeProvider.conditionsFromItem(upgradeMaterial))
                .offerTo(exporter, CalibratedAccess.id(name));
    }

    public static void generateSmithing(Item item, RemoteType type, Consumer<RecipeJsonProvider> exporter) {
        generateSmithing(type.name() + "_accessor", type.upgradeFrom().get(), type.upgradeMaterial(), item, RecipeCategory.TOOLS, exporter);
    }

    public static void generateShaped(Item item, RemoteType type, Consumer<RecipeJsonProvider> exporter) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.TOOLS, item)
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
    public void generate(Consumer<RecipeJsonProvider> exporter) {
        generateSmithing("calibrator", Items.ENDER_PEARL, Items.REDSTONE, CAItems.CALIBRATOR, RecipeCategory.MISC, exporter);
        for (RemoteItem item : CAItems.remotes) {
            generate(item, exporter);
        }
    }
}
