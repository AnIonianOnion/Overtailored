package com.anionianonion.overtailored.sound;

import com.anionianonion.overtailored.OvertailoredMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

//100% copy of https://github.com/phuccom000/Overgeared/blob/1.21.1/src/main/java/net/stirdrem/overgeared/sound/ModSounds.java
public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, OvertailoredMod.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> ANVIL_HIT = registerSoundEvents("anvil_hit");
    public static final DeferredHolder<SoundEvent, SoundEvent> FORGING_COMPLETE = registerSoundEvents("forging_complete");
    public static final DeferredHolder<SoundEvent, SoundEvent> FORGING_FAILED = registerSoundEvents("forging_failed");

    private static DeferredHolder<SoundEvent, SoundEvent> registerSoundEvents(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(OvertailoredMod.loc(name)));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}