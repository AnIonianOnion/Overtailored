package com.anionianonion.overtailored.item;

import com.anionianonion.overtailored.OvertailoredMod;
import com.anionianonion.overtailored.item.custom.PatternItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

//based on https://github.com/phuccom000/Overgeared/blob/1.21.1/src/main/java/net/stirdrem/overgeared/item/ModItems.java
public class ModItems {

    public static final DeferredRegister.Items ITEMS_REGISTRY = DeferredRegister.createItems(OvertailoredMod.MOD_ID);

    public static final DeferredItem<Item> SEWING_NEEDLE = registerSimpleItem("sewing_needle");
    public static final DeferredItem<Item> EMPTY_PATTERN = registerSimpleItem("empty_pattern");
    public static final DeferredItem<Item> CLOTHES_PATTERN = registerItem("pattern",
            () -> new PatternItem(new Item.Properties()));

    public static <T extends Item> DeferredItem<T> registerItem(String name, Supplier<T> itemSupplier) {
        return ITEMS_REGISTRY.register(name, itemSupplier);
    }

    public static DeferredItem<Item> registerSimpleItem(String name) {
        return ITEMS_REGISTRY.registerSimpleItem(name);
    }

    public static void register(IEventBus eventBus) {
        ITEMS_REGISTRY.register(eventBus);
    }
}
