package com.anionianonion.overtailored.common;

import com.anionianonion.overtailored.OvertailoredMod;
import com.anionianonion.overtailored.item.ArmorTypeRegistry;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;


//trimmed https://github.com/phuccom000/Overgeared/blob/1.21.1/src/main/java/net/stirdrem/overgeared/common/CommonModEvents.java
@EventBusSubscriber(modid = OvertailoredMod.MOD_ID)
public class CommonModEvents {
  @SubscribeEvent
  public static void commonSetup(final FMLCommonSetupEvent event) {
    ArmorTypeRegistry.init();
  }
}
