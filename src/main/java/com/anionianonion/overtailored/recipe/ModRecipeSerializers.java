package com.anionianonion.overtailored.recipe;

import com.anionianonion.overtailored.OvertailoredMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

//based on https://github.com/phuccom000/Overgeared/blob/1.21.1/src/main/java/net/stirdrem/overgeared/recipe/ModRecipeSerializers.java
public class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, OvertailoredMod.MOD_ID);

    public static final Supplier<RecipeSerializer<SewingRecipe>> SEWING_SERIALIZER =
            RECIPE_SERIALIZERS.register("sewing", () -> SewingRecipe.Serializer.INSTANCE);

    public static final Supplier<RecipeSerializer<BlueprintCloningRecipe>> CRAFTING_BLUEPRINTCLONING =
            RECIPE_SERIALIZERS.register("crafting_cloning", BlueprintCloningRecipe.Serializer::new);

    public static void register(IEventBus eventBus) {
        RECIPE_SERIALIZERS.register(eventBus);
    }
}