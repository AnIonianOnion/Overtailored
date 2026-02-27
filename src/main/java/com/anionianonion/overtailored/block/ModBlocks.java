package com.anionianonion.overtailored.block;

import com.anionianonion.overtailored.OvertailoredMod;
import com.anionianonion.overtailored.SewingMachineTier;
import com.anionianonion.overtailored.block.custom.BlueprintWorkbenchBlock;
import com.anionianonion.overtailored.block.custom.SewingMachineBlock;
import com.anionianonion.overtailored.block.custom.SewingMachinePartBlock;
import com.anionianonion.overtailored.block.custom.SimpleSewingStationBlock;
import com.anionianonion.overtailored.item.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS_REGISTRY = DeferredRegister.createBlocks(OvertailoredMod.MOD_ID);

    /*future block for separating irl Loom recipes into its own workbench?
    public static final DeferredBlock<ArcaneLoomBlock> ARCANE_LOOM_BLOCK = registerBlock("arcane_loom",
            () -> new ArcaneLoomBlock(BlockBehaviour.Properties.of().noOcclusion()));*/

    public static final DeferredBlock<BlueprintWorkbenchBlock> PATTERN_TABLE = registerBlock("pattern_table",
            () -> new BlueprintWorkbenchBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CRAFTING_TABLE)));

    public static final DeferredBlock<SimpleSewingStationBlock> SIMPLE_SEWING_STATION_BLOCK = registerBlock("simple_sewing_station",
            () -> new SimpleSewingStationBlock(SewingMachineTier.WELL, BlockBehaviour.Properties.of()
                    .strength(2.5f, 2.5f)
                    .sound(SoundType.WOOD)
                    .noOcclusion()));

    public static final DeferredBlock<SewingMachineBlock> SEWING_MACHINE_BLOCK = registerBlock("sewing_machine",
            () -> new SewingMachineBlock(SewingMachineTier.MASTER, BlockBehaviour.Properties.of()
                    .strength(2.5f, 2.5f)
                    .sound(SoundType.WOOD)
                    .noOcclusion()));

    public static final DeferredBlock<SewingMachinePartBlock> SEWING_MACHINE_EXTENSION_BLOCK = registerBlockOnly("sewing_machine_extension",
            () -> new SewingMachinePartBlock(BlockBehaviour.Properties.of()
                    .strength(2.5f, 2.5f)
                    .sound(SoundType.WOOD)
                    .noOcclusion()));


    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS_REGISTRY.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> DeferredBlock<T> registerBlockOnly(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS_REGISTRY.register(name, block);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ModItems.ITEMS_REGISTRY.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS_REGISTRY.register(eventBus);
    }
}
