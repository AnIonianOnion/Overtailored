package com.anionianonion.overtailored.datagen;

import com.anionianonion.overtailored.OvertailoredMod;
import com.anionianonion.overtailored.item.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredHolder;

//based on https://github.com/phuccom000/Overgeared/blob/1.21.1/src/main/java/net/stirdrem/overgeared/datagen/ModItemModelProvider.java
public class ModItemModelProvider extends ItemModelProvider {

    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, OvertailoredMod.MOD_ID, existingFileHelper);
    }

    //for items only, not block items
    @Override
    protected void registerModels() {
        simpleItem(ModItems.SEWING_NEEDLE);
        simpleItem(ModItems.CLOTHES_PATTERN);
        simpleItem(ModItems.EMPTY_PATTERN);
    }

    private void simpleItem(DeferredHolder<Item, Item> item) {
        withExistingParent(item.getId().getPath(),
                mcLoc("item/generated")).texture("layer0",
                OvertailoredMod.loc("item/" + item.getId().getPath()));
    }
}
