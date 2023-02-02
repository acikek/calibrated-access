package com.acikek.calibrated.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class CalibratedAccessDatagen implements DataGeneratorEntrypoint {

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        fabricDataGenerator.addProvider(new CalibratedAccessModels(fabricDataGenerator));
        fabricDataGenerator.addProvider(new CalibratedAccessRecipes(fabricDataGenerator));
    }
}
