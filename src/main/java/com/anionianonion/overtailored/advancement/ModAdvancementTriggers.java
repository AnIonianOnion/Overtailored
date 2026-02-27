package com.anionianonion.overtailored.advancement;

import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import com.anionianonion.overtailored.OvertailoredMod;

//copied and modified from https://github.com/phuccom000/Overgeared/blob/1.21.1/src/main/java/net/stirdrem/overgeared/advancement/ModAdvancementTriggers.java
public class ModAdvancementTriggers {

    public static final DeferredRegister<CriterionTrigger<?>> TRIGGERS = DeferredRegister
            .create(Registries.TRIGGER_TYPE, OvertailoredMod.MOD_ID);

    //renamed make smithing anvil to make sewing station
    public static final DeferredHolder<CriterionTrigger<?>, MakeSewingStationTrigger> MAKE_SEWING_STATION = TRIGGERS
            .register("make_sewing_station", MakeSewingStationTrigger::new);

    //reginamed forging quality to sewing quality
    public static final DeferredHolder<CriterionTrigger<?>, SewingQualityTrigger> SEWING_QUALITY = TRIGGERS
            .register("sewing_quality", SewingQualityTrigger::new);

    public static final DeferredHolder<CriterionTrigger<?>, PatternQualityTrigger> PATTERN_QUALITY = TRIGGERS
            .register("pattern_quality", PatternQualityTrigger::new);

    public static final DeferredHolder<CriterionTrigger<?>, MaxLevelPatternAdvancementTrigger> MAX_LEVEL_PATTERN = TRIGGERS
            .register("max_level_pattern", MaxLevelPatternAdvancementTrigger::new);

    public static void register(IEventBus eventBus) {
        TRIGGERS.register(eventBus);
    }
}
