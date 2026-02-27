package com.anionianonion.overtailored.events;

import com.anionianonion.overtailored.OvertailoredMod;
import com.anionianonion.overtailored.datapack.PatternArmorTypesReloadListener;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

//based on https://github.com/phuccom000/Overgeared/blob/1.21.1/src/main/java/net/stirdrem/overgeared/event/ReloadListenerRegistry.java
@EventBusSubscriber(modid = OvertailoredMod.MOD_ID)
public class ReloadListenerRegistry {
    @SubscribeEvent
    public static void onReload(AddReloadListenerEvent event) {
        event.addListener(new PatternArmorTypesReloadListener());
    }
}
