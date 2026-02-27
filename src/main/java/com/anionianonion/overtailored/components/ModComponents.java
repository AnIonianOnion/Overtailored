package com.anionianonion.overtailored.components;

import com.anionianonion.overtailored.OvertailoredMod;
import com.anionianonion.overtailored.SewingQuality;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

//based on https://github.com/phuccom000/Overgeared/blob/1.21.1/src/main/java/net/stirdrem/overgeared/components/ModComponents.java
public class ModComponents {
    public static final DeferredRegister<DataComponentType<?>> COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, OvertailoredMod.MOD_ID);

    public static final Supplier<DataComponentType<PatternData>> PATTERN_DATA =
            COMPONENTS.register("blueprint_data", () -> DataComponentType.<PatternData>builder()
                    .persistent(PatternData.CODEC)
                    .networkSynchronized(PatternData.STREAM_CODEC)
                    .build());

    // Marks an item as a failed crafting result (used for JEI display)
    public static final Supplier<DataComponentType<Boolean>> FAILED_RESULT =
            COMPONENTS.register("failed_result", () -> DataComponentType.<Boolean>builder()
                    .persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL)
                    .build());

    // Forging quality stored on crafted items (sword, armor, etc.) - stored as enum
    public static final Supplier<DataComponentType<SewingQuality>> SEWING_QUALITY =
            COMPONENTS.register("sewing_quality", () -> DataComponentType.<SewingQuality>builder()
                    .persistent(SewingQuality.CODEC)
                    .networkSynchronized(SewingQuality.STREAM_CODEC)
                    .build());

    // Creator name for items
    public static final Supplier<DataComponentType<String>> CREATOR =
            COMPONENTS.register("creator", () -> DataComponentType.<String>builder()
                    .persistent(Codec.STRING)
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8)
                    .build());

    // Marks blueprint as required or not in recipe viewers
    public static final Supplier<DataComponentType<Boolean>> PATTERN_REQUIRED =
            COMPONENTS.register("pattern_required", () -> DataComponentType.<Boolean>builder()
                    .persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL)
                    .build());

    public static final Supplier<DataComponentType<List<AttributeModifier>>> QUALITY_MODIFIERS =
            COMPONENTS.register("quality_modifiers", () -> DataComponentType.<List<AttributeModifier>>builder()
                    .persistent(AttributeModifier.CODEC.listOf())
                    .networkSynchronized(AttributeModifier.STREAM_CODEC.apply(ByteBufCodecs.list()))
                    .build());


    public static void register(IEventBus eventBus) {
        COMPONENTS.register(eventBus);
    }
}
