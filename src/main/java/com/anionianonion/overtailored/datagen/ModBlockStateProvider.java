package com.anionianonion.overtailored.datagen;

import com.anionianonion.overtailored.OvertailoredMod;
import com.anionianonion.overtailored.block.ModBlocks;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

//based on https://github.com/phuccom000/Overgeared/blob/1.21.1/src/main/java/net/stirdrem/overgeared/datagen/ModBlockStateProvider.java
public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, OvertailoredMod.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {

        // Pattern table has a manually-created blockstates/block model, just need the item model
        simpleBlockItem(ModBlocks.PATTERN_TABLE.get(),
                new ModelFile.UncheckedModelFile(modLoc("block/pattern_table")));
    }
}