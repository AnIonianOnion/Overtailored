package com.anionianonion.overtailored.recipe;

import com.anionianonion.overtailored.PatternQuality;
import com.anionianonion.overtailored.components.PatternData;
import com.anionianonion.overtailored.components.ModComponents;
import com.anionianonion.overtailored.item.ModItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

//based on https://github.com/phuccom000/Overgeared/blob/1.21.1/src/main/java/net/stirdrem/overgeared/recipe/BlueprintCloningRecipe.java
public class BlueprintCloningRecipe extends CustomRecipe {
    public BlueprintCloningRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        int blueprintCount = 0;
        ItemStack emptyBlueprint = ItemStack.EMPTY;

        for (int j = 0; j < input.size(); ++j) {
            ItemStack stack = input.getItem(j);
            if (!stack.isEmpty()) {
                if (stack.is(ModItems.EMPTY_PATTERN.get())) {
                    if (!emptyBlueprint.isEmpty()) {
                        return false; // Only 1 empty blueprint allowed
                    }
                    emptyBlueprint = stack;
                } else {
                    if (!stack.is(ModItems.CLOTHES_PATTERN.get())) {
                        return false;
                    }

                    ++blueprintCount;
                }
            }
        }

        // Change from > 0 to == 1 (Only 1 Blueprint Source Allowed)
        return !emptyBlueprint.isEmpty() && blueprintCount == 1;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack source = ItemStack.EMPTY;

        for (int j = 0; j < input.size(); ++j) {
            ItemStack stack = input.getItem(j);
            if (!stack.isEmpty() && stack.is(ModItems.CLOTHES_PATTERN.get())) {
                if (!source.isEmpty()) return ItemStack.EMPTY; // only 1 blueprint source allowed
                source = stack;
            }
        }

        if (source.isEmpty()) return ItemStack.EMPTY;
        
        ItemStack result = source.copyWithCount(2);

        // Reduce quality using data components
        PatternData patternData = source.get(ModComponents.PATTERN_DATA);
        if (patternData != null) {
            PatternQuality current = PatternQuality.fromString(patternData.quality());
            PatternQuality downgraded = PatternQuality.getPrevious(current);

            if (downgraded != null) {
                // Update the blueprint data with the new quality
                PatternData newData = patternData.withQuality(downgraded.getId());
                result.set(ModComponents.PATTERN_DATA, newData);
            }
        }

        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 3 && height >= 3;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.CRAFTING_BLUEPRINTCLONING.get();
    }

    public static class Serializer implements RecipeSerializer<BlueprintCloningRecipe> {
        private static final MapCodec<BlueprintCloningRecipe> CODEC = 
            CraftingBookCategory.CODEC.fieldOf("category")
                .xmap(BlueprintCloningRecipe::new, BlueprintCloningRecipe::category);

        private static final StreamCodec<RegistryFriendlyByteBuf, BlueprintCloningRecipe> STREAM_CODEC = StreamCodec.composite(
                CraftingBookCategory.STREAM_CODEC, BlueprintCloningRecipe::category,
                BlueprintCloningRecipe::new
            );

        @Override
        public MapCodec<BlueprintCloningRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, BlueprintCloningRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}