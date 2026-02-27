package com.anionianonion.overtailored.screens;

import com.anionianonion.overtailored.OvertailoredMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, OvertailoredMod.MOD_ID);


    public static final DeferredHolder<MenuType<?>, MenuType<SimpleSewingStationMenu>> SIMPLE_SEWING_STATION_MENU =
            registerMenuType("simple_sewing_station_menu", SimpleSewingStationMenu::new);

    public static final DeferredHolder<MenuType<?>, MenuType<SewingMachineMenu>> SEWING_MACHINE_MENU =
            registerMenuType("sewing_machine_menu", SewingMachineMenu::new);

    public static final DeferredHolder<MenuType<?>, MenuType<PatternWorkbenchMenu>> PATTERN_WORKBENCH_MENU =
            MENUS.register("pattern_workbench",
                    () -> new MenuType<>(PatternWorkbenchMenu::new, FeatureFlagSet.of()));

    private static <T extends AbstractContainerMenu> DeferredHolder<MenuType<?>, MenuType<T>> registerMenuType(String name, IContainerFactory<T> factory) {
        return MENUS.register(name, () -> IMenuTypeExtension.create(factory));
    }

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}