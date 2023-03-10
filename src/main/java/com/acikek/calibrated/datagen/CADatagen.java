package com.acikek.calibrated.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class CADatagen implements DataGeneratorEntrypoint {

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        fabricDataGenerator.addProvider(new CAModels(fabricDataGenerator));
        fabricDataGenerator.addProvider(new CARecipes(fabricDataGenerator));
    }
}
