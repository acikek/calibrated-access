package com.acikek.calibrated.datagen;

import com.acikek.calibrated.CalibratedAccess;
import com.acikek.calibrated.item.CAItems;
import com.acikek.calibrated.item.remote.RemoteItem;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.client.*;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

import java.util.Optional;

public class CAModels extends FabricModelProvider {

    public static final TextureKey LAYER1 = TextureKey.of("layer1");

    public record EffectModel(String effect, Model model) {
        public void upload(Item item, ItemModelGenerator generator) {
            model.upload(ModelIds.getItemSubModelId(item, "_" + effect), getEffectMap(item, effect), generator.writer);
        }
    }

    public static final EffectModel ACTIVATED = getAccessorEffectModel("zap");
    public static final EffectModel FAIL = getAccessorEffectModel("fail");

    public CAModels(FabricDataGenerator dataGenerator) {
        super(dataGenerator);
    }

    public static TextureMap getEffectMap(Item item, String effect) {
        return TextureMap.layer0(item).put(LAYER1, CalibratedAccess.id("item/effects/" + effect));
    }

    public static EffectModel getAccessorEffectModel(String effect) {
        return new EffectModel(effect, new Model(
                Optional.of(new Identifier("item/generated")),
                Optional.of("_" + effect),
                TextureKey.LAYER0, LAYER1
        ));
    }

    public static void generateRemoteModels(Item item, ItemModelGenerator generator) {
        ACTIVATED.upload(item, generator);
        FAIL.upload(item, generator);
    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {
        itemModelGenerator.register(CAItems.CALIBRATOR, Models.GENERATED);
        for (RemoteItem item : CAItems.remotes) {
            generateRemoteModels(item, itemModelGenerator);
        }
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
        // Empty
    }
}
