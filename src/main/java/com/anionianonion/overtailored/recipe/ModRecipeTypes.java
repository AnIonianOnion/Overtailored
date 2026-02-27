package com.anionianonion.overtailored.recipe;

import com.anionianonion.overtailored.OvertailoredMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

//based on https://github.com/phuccom000/Overgeared/blob/1.21.1/src/main/java/net/stirdrem/overgeared/recipe/ModRecipeTypes.java
public class ModRecipeTypes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, OvertailoredMod.MOD_ID);

    public static final Supplier<RecipeType<SewingRecipe>> SEWING =
            RECIPE_TYPES.register("sewing",
                    () -> RecipeType.simple(OvertailoredMod.loc("sewing")));

    public static void register(IEventBus eventBus) {
        RECIPE_TYPES.register(eventBus);
    }
}
