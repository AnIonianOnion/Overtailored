package com.anionianonion.overtailored.datagen;

import com.anionianonion.overtailored.OvertailoredMod;
import com.anionianonion.overtailored.block.ModBlocks;
import com.anionianonion.overtailored.utils.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

//based on https://github.com/phuccom000/Overgeared/blob/1.21.1/src/main/java/net/stirdrem/overgeared/datagen/ModBlockTagGenerator.java
public class ModBlockTagGenerator extends BlockTagsProvider {
    public ModBlockTagGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, OvertailoredMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        this.tag(BlockTags.MINEABLE_WITH_AXE)
                .add(
                        ModBlocks.SIMPLE_SEWING_STATION_BLOCK.get(),
                        ModBlocks.SEWING_MACHINE_BLOCK.get()
                );

        this.tag(ModTags.Blocks.SEWING_MACHINE)
                .add(
                        ModBlocks.SIMPLE_SEWING_STATION_BLOCK.get(),
                        ModBlocks.SEWING_MACHINE_BLOCK.get()
                );
        this.tag(ModTags.Blocks.SEWING_STATION_BASES)
                .add(
                        Blocks.LOOM
                );
    }
}