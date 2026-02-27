package com.anionianonion.overtailored;

import com.anionianonion.overtailored.advancement.ModAdvancementTriggers;
import com.anionianonion.overtailored.block.ModBlocks;
import com.anionianonion.overtailored.block.entity.ModBlockEntities;
import com.anionianonion.overtailored.components.ModComponents;
import com.anionianonion.overtailored.config.ClientConfig;
import com.anionianonion.overtailored.config.ServerConfig;
import com.anionianonion.overtailored.item.ModItems;
import com.anionianonion.overtailored.recipe.ModRecipeSerializers;
import com.anionianonion.overtailored.recipe.ModRecipeTypes;
import com.anionianonion.overtailored.screens.ModMenuTypes;
import com.anionianonion.overtailored.sound.ModSounds;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(OvertailoredMod.MOD_ID)
public final class OvertailoredMod {
    public static final String MOD_ID = "overtailored";
    public static final Logger LOGGER = LogUtils.getLogger();

    public OvertailoredMod(IEventBus modEventBus, ModContainer modContainer) {
        /*

        ModCapabilities.register(modEventBus);

        ModCreativeModeTabs.register(modEventBus);
        ModEntities.register(modEventBus);
        ModLootModifiers.register(modEventBus);
         */
        ModAdvancementTriggers.register(modEventBus);
        ModComponents.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModRecipeTypes.register(modEventBus);
        ModRecipeSerializers.register(modEventBus);
        ModItems.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        ModSounds.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, ServerConfig.SERVER_CONFIG);
        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.CLIENT_CONFIG);

        //ModCompat.init();
    }

    public static ResourceLocation loc(String name) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, name);
    }
}
