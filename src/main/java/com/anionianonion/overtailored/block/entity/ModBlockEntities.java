package com.anionianonion.overtailored.block.entity;

import com.anionianonion.overtailored.OvertailoredMod;
import com.anionianonion.overtailored.block.ModBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES_REGISTER =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, OvertailoredMod.MOD_ID);

    /*
    public static final Supplier<BlockEntityType<ArcaneLoomBlockEntity>> ARCANE_LOOM_BE =
            BLOCK_ENTITIES_REGISTER.register("arcane_loom_be", () -> BlockEntityType.Builder.of(
                    ArcaneLoomBlockEntity::new, ModBlocks.ARCANE_LOOM_BLOCK.get()).build(null));
     */

    public static final Supplier<BlockEntityType<SimpleSewingStationBlockEntity>> SIMPLE_SEWING_STATION =
            BLOCK_ENTITIES_REGISTER.register("simple_sewing_station_be", () -> BlockEntityType.Builder.of(
                    SimpleSewingStationBlockEntity::new, ModBlocks.SIMPLE_SEWING_STATION_BLOCK.get()).build(null));

    public static final Supplier<BlockEntityType<SewingMachineBlockEntity>> SEWING_MACHINE =
            BLOCK_ENTITIES_REGISTER.register("sewing_machine_be", () -> BlockEntityType.Builder.of(
                    SewingMachineBlockEntity::new, ModBlocks.SEWING_MACHINE_BLOCK.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES_REGISTER.register(eventBus);
    }
}
