package com.anionianonion.overtailored.client;

import com.anionianonion.overtailored.OvertailoredMod;
import com.anionianonion.overtailored.block.entity.ModBlockEntities;
import com.anionianonion.overtailored.block.entity.renderer.SewingMachineBlockEntityRenderer;
import com.anionianonion.overtailored.block.entity.renderer.SimpleSewingStationBlockEntityRenderer;
import com.anionianonion.overtailored.recipe.SewingRecipe;
import com.anionianonion.overtailored.screens.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RecipesUpdatedEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

//trimmed https://github.com/phuccom000/Overgeared/blob/1.21.1/src/main/java/net/stirdrem/overgeared/client/ClientModEvents.java
@EventBusSubscriber(modid = OvertailoredMod.MOD_ID, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        ModList.get().getModContainerById(OvertailoredMod.MOD_ID).orElseThrow()
                .registerExtensionPoint(
                        IConfigScreenFactory.class,
                        (container, parent) -> new OvertailoredConfigScreen(parent));
    }

    @SubscribeEvent
    public static void registerBER(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.SIMPLE_SEWING_STATION.get(), SimpleSewingStationBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.SEWING_MACHINE.get(), SewingMachineBlockEntityRenderer::new);
    }

    @SubscribeEvent
    public static void registerMenuScreens(RegisterMenuScreensEvent e) {
        e.register(ModMenuTypes.PATTERN_WORKBENCH_MENU.get(), PatternWorkbenchScreen::new);
        e.register(ModMenuTypes.SIMPLE_SEWING_STATION_MENU.get(), SimpleSewingStationScreen::new);
        e.register(ModMenuTypes.SEWING_MACHINE_MENU.get(), SewingMachineScreen::new);
    }

    @SubscribeEvent
    public static void onRecipesReceived(RecipesUpdatedEvent event) {
        boolean sewingSent = event.getRecipeManager().getOrderedRecipes().stream()
                .anyMatch(t -> t.value().getSerializer().equals(SewingRecipe.Serializer.INSTANCE));
        OvertailoredMod.LOGGER.info("Sewing recipe type sent? {}", sewingSent);
        ResourceLocation arcaneClothId = ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "magic_cloth");
        boolean present = event.getRecipeManager().byKey(arcaneClothId).isPresent();
        OvertailoredMod.LOGGER.info("Recipe {} present? {}", arcaneClothId, present);
    }

    /**
     * MOD bus events for client-side registration
     */
    @EventBusSubscriber(modid = OvertailoredMod.MOD_ID, value = Dist.CLIENT)
    public static class ModBusEvents {
        @SubscribeEvent
        public static void registerGuiLayers(RegisterGuiLayersEvent event) {
            // Register anvil minigame overlay below the hotbar
            event.registerBelow(VanillaGuiLayers.HOTBAR, AnvilMinigameOverlay.ID, AnvilMinigameOverlay.INSTANCE);
            // Register popup overlay above the minigame overlay
            event.registerAbove(AnvilMinigameOverlay.ID, PopupOverlay.ID, PopupOverlay.INSTANCE);
        }
    }
}
