package com.anionianonion.overtailored.datagen;

import com.anionianonion.overtailored.SewingMachineTier;
import com.anionianonion.overtailored.SewingQuality;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;

import io.redspace.ironsspellbooks.registries.ItemRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

//based on https://github.com/phuccom000/Overgeared/blob/1.21.1/src/main/java/net/stirdrem/overgeared/datagen/ModRecipeProvider.java
//and ShapedSewingRecipeBuilder
public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {

    public ModRecipeProvider(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pRegistries) {
        super(pOutput, pRegistries);
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput recipeOutput) {

        Item arcaneEssence = ItemRegistry.ARCANE_ESSENCE.get();
        Item arcaneCloth = ItemRegistry.MAGIC_CLOTH.get();

        Item leather = Items.LEATHER;
        Item rottenFlesh = Items.ROTTEN_FLESH;
        Item leatherHelmet = Items.LEATHER_HELMET;
        Item leatherChestplate = Items.LEATHER_CHESTPLATE;
        Item leatherLeggings = Items.LEATHER_LEGGINGS;
        Item leatherBoots = Items.LEATHER_BOOTS;


        //---------Ingredients---------
        ShapedSewingRecipeBuilder.shaped(RecipeCategory.MISC, arcaneCloth, 3)
                .pattern("eee")
                .pattern("ewe")
                .pattern("eee")
                .define('e', arcaneEssence)
                .define('w', ItemTags.WOOL)
                .minSewingMachineTierRequired(SewingMachineTier.WELL)
                .unlockedBy(getHasName(arcaneEssence), has(arcaneEssence))
        .save(recipeOutput);

        ShapedSewingRecipeBuilder.shaped(RecipeCategory.MISC, leather, 10)
                .pattern("rrr")
                .pattern("rrr")
                .pattern("rrr")
                .define('r', rottenFlesh)
                .setHasQuality(false)
                .unlockedBy(getHasName(rottenFlesh), has(rottenFlesh))
        .save(recipeOutput);

        //---------Armor---------
        ShapedSewingRecipeBuilder.shaped(RecipeCategory.COMBAT, leatherHelmet, 5)
                .pattern("lll")
                .pattern("l l")
                .define('l', leather)
                .needsMinigame(true)
                .unlockedBy(getHasName(leather), has(leather))
        .save(recipeOutput);

        ShapedSewingRecipeBuilder.shaped(RecipeCategory.COMBAT, leatherChestplate, 5)
                .pattern("l l")
                .pattern("lll")
                .pattern("lll")
                .define('l', leather)
                .needsMinigame(true)
                .unlockedBy(getHasName(leather), has(leather))
        .save(recipeOutput);

        ShapedSewingRecipeBuilder.shaped(RecipeCategory.COMBAT, leatherLeggings, 5)
                .pattern("lll")
                .pattern("l l")
                .pattern("l l")
                .define('l', leather)
                .needsMinigame(true)
                .unlockedBy(getHasName(leather), has(leather))
        .save(recipeOutput);

        ShapedSewingRecipeBuilder.shaped(RecipeCategory.COMBAT, leatherBoots, 5)
                .pattern("l l")
                .pattern("l l")
                .define('l', leather)
                .needsMinigame(true)
                .unlockedBy(getHasName(leather), has(leather))
        .save(recipeOutput);
    }
}
