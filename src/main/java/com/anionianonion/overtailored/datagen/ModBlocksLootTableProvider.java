package com.anionianonion.overtailored.datagen;
import com.anionianonion.overtailored.block.ModBlocks;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.Set;

//based on https://www.youtube.com/watch?v=T-9h-FbAQH0&t=710s
public class ModBlocksLootTableProvider extends BlockLootSubProvider {

    protected ModBlocksLootTableProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        dropSelf(ModBlocks.PATTERN_TABLE.get());
        dropSelf(ModBlocks.SIMPLE_SEWING_STATION_BLOCK.get());
        dropSelf(ModBlocks.SEWING_MACHINE_BLOCK.get());
        dropOther(ModBlocks.SEWING_MACHINE_EXTENSION_BLOCK.get(), ModBlocks.SIMPLE_SEWING_STATION_BLOCK.asItem());
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS_REGISTRY.getEntries().stream().map(Holder::value)::iterator;
    }
}