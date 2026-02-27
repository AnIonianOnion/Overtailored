package com.anionianonion.overtailored.datagen;

import com.anionianonion.overtailored.SewingMachineTier;
import com.anionianonion.overtailored.SewingQuality;
import com.anionianonion.overtailored.recipe.SewingRecipe;
import com.google.common.collect.Lists;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import javax.annotation.Nullable;
import java.util.*;

//based on https://github.com/phuccom000/Overgeared/blob/1.21.1/src/main/java/net/stirdrem/overgeared/datagen/ShapedForgingRecipeBuilder.java
public class ShapedSewingRecipeBuilder implements RecipeBuilder {
    private final RecipeCategory category;
    private final Item result;

    private final int count;
    private final int stitches;
    private final List<String> rows = Lists.newArrayList();
    private final Map<Character, SewingRecipe.SewingIngredient> key = new LinkedHashMap<>();
    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();
    private final Advancement.Builder advancement = Advancement.Builder.recipeAdvancement();

    @Nullable
    private final List<String> patternTypes = new ArrayList<>();
    @Nullable
    private Boolean requiresPattern;
    @Nullable
    private Boolean hasQuality;
    @Nullable
    private Boolean needsMinigame;
    @Nullable
    private String group;
    @Nullable
    private String minSewingMachineTier;
    @Nullable
    private Item failedResult;
    @Nullable
    private int failedResultCount;

    @Nullable
    private SewingQuality minimumQuality;
    @Nullable
    private SewingQuality qualityDifficulty;
    @Nullable
    private SewingRecipe.QualityModifiers qualityModifiers;

    private boolean showNotification = true;


    public ShapedSewingRecipeBuilder(RecipeCategory category, ItemLike result, int count, int stitches) {
        this.category = category;
        this.result = result.asItem();
        this.count = count;
        this.stitches = stitches;
    }

    private static boolean isTools(Item item) {
        return item instanceof SwordItem ||
                item instanceof DiggerItem ||
                item instanceof ProjectileWeaponItem;
    }

    public static ShapedSewingRecipeBuilder shaped(RecipeCategory category, ItemLike result, int stitches) {
        return new ShapedSewingRecipeBuilder(category, result, 1, stitches);
    }

    public static ShapedSewingRecipeBuilder shaped(RecipeCategory category, ItemLike result, int count, int stitches) {
        return new ShapedSewingRecipeBuilder(category, result, count, stitches);
    }

    public ShapedSewingRecipeBuilder define(Character pSymbol, TagKey<Item> pTag) {
        return this.define(pSymbol, Ingredient.of(pTag));
    }

    public ShapedSewingRecipeBuilder define(Character symbol, ItemLike item) {
        return this.define(symbol, Ingredient.of(item));
    }

    public ShapedSewingRecipeBuilder define(Character symbol, Ingredient ingredient) {
        return define(symbol, ingredient, false);
    }

    public ShapedSewingRecipeBuilder define(Character symbol, Ingredient ingredient,
                                             boolean transferNBT) {
        if (this.key.containsKey(symbol)) {
            throw new IllegalArgumentException("Symbol '" + symbol + "' is already defined!");
        } else if (symbol == ' ') {
            throw new IllegalArgumentException("Symbol ' ' (whitespace) is reserved and cannot be defined");
        } else {
            this.key.put(symbol, new SewingRecipe.SewingIngredient(ingredient, transferNBT));
            return this;
        }
    }


    public ShapedSewingRecipeBuilder pattern(String pPattern) {
        if (!this.rows.isEmpty() && pPattern.length() != this.rows.get(0).length()) {
            throw new IllegalArgumentException("Pattern must be the same width on every line!");
        } else {
            this.rows.add(pPattern);
            return this;
        }
    }

    @Override
    public ShapedSewingRecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        this.advancement.addCriterion(name, criterion);
        return this;
    }

    @Override
    public ShapedSewingRecipeBuilder group(@Nullable String pGroupName) {
        this.group = pGroupName;
        return this;
    }

    public ShapedSewingRecipeBuilder minSewingMachineTierRequired(@Nullable SewingMachineTier pTier) {
        this.minSewingMachineTier = pTier.getDisplayName();
        return this;
    }

    public ShapedSewingRecipeBuilder setHasQuality(@Nullable boolean hasQuality) {
        this.hasQuality = hasQuality;
        return this;
    }

    public ShapedSewingRecipeBuilder requiresBlueprint(@Nullable boolean requiresBlueprint) {
        this.requiresPattern = requiresBlueprint;
        return this;
    }

    public ShapedSewingRecipeBuilder needsMinigame(@Nullable boolean needsMinigame) {
        this.needsMinigame = needsMinigame;
        return this;
    }

    public ShapedSewingRecipeBuilder failedResult(ItemLike result) {
        this.failedResult = result.asItem();
        this.failedResultCount = 1;
        return this;
    }

    public ShapedSewingRecipeBuilder failedResult(ItemLike result, int count) {
        this.failedResult = result.asItem();
        this.failedResultCount = count;
        return this;
    }

    public ShapedSewingRecipeBuilder setBlueprint(String blueprintType) {
        if (blueprintType != null && !blueprintType.isBlank()) {
            this.patternTypes.add(blueprintType.toLowerCase());
        }
        return this;
    }

    public ShapedSewingRecipeBuilder minimumQuality(@Nullable SewingQuality minimumQuality) {
        this.minimumQuality = minimumQuality;
        return this;
    }

    public ShapedSewingRecipeBuilder qualityDifficulty(@Nullable SewingQuality qualityDifficulty) {
        this.qualityDifficulty = qualityDifficulty;
        return this;
    }

    public ShapedSewingRecipeBuilder showNotification(boolean pShowNotification) {
        this.showNotification = pShowNotification;
        return this;
    }

    public ShapedSewingRecipeBuilder setQualityModifiers(SewingRecipe.QualityModifiers qualityModifiers) {
        this.qualityModifiers = qualityModifiers;
        return this;
    }

    @Override
    public Item getResult() {
        return this.result;
    }

    public Item getFailedResult() {
        return this.failedResult;
    }

    @Override
    public void save(RecipeOutput output, ResourceLocation id) {
        this.ensureValid(id);

        Advancement.Builder advBuilder = Advancement.Builder.recipeAdvancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .requirements(AdvancementRequirements.Strategy.OR);
        this.criteria.forEach(advBuilder::addCriterion);

        // Pattern parsing
        int width = this.rows.getFirst().length();
        int height = this.rows.size();

        NonNullList<SewingRecipe.SewingIngredient> ingredients =
                NonNullList.withSize(width * height, SewingRecipe.SewingIngredient.EMPTY);

        for (int y = 0; y < height; ++y) {
            String row = this.rows.get(y);
            for (int x = 0; x < width; ++x) {
                char symbol = row.charAt(x);
                SewingRecipe.SewingIngredient ingredient = this.key.getOrDefault(symbol, SewingRecipe.SewingIngredient.EMPTY);
                ingredients.set(y * width + x, ingredient);
            }
        }

        // Resolve defaults
        boolean actualHasQuality = this.hasQuality == null || this.hasQuality;
        boolean actualRequiresPattern = this.requiresPattern != null && this.requiresPattern;
        boolean actualNeedsMinigame = this.needsMinigame != null && this.needsMinigame;

        Set<String> actualBlueprintTypes = new LinkedHashSet<>(this.patternTypes);

        SewingQuality actualMinQuality = this.minimumQuality != null ? this.minimumQuality : SewingQuality.POOR;
        SewingQuality actualQualityDiff = this.qualityDifficulty != null ? this.qualityDifficulty : SewingQuality.NONE;
        String actualSewingMachineTier = this.minSewingMachineTier == null ? SewingMachineTier.WELL.getDisplayName() : this.minSewingMachineTier;

        ItemStack actualFailedResult = this.failedResult != null
                ? new ItemStack(this.failedResult, this.failedResultCount)
                : ItemStack.EMPTY;

        // Convert char key → string key for JSON
        Map<String, SewingRecipe.SewingIngredient> stringKeyMap = new LinkedHashMap<>();
        for (Map.Entry<Character, SewingRecipe.SewingIngredient> entry : this.key.entrySet()) {
            stringKeyMap.put(String.valueOf(entry.getKey()), entry.getValue());
        }

        SewingRecipe.QualityModifiers actualQualityModifiers = this.qualityModifiers != null
                ? this.qualityModifiers
                : new SewingRecipe.QualityModifiers(List.of(), List.of(), List.of(), List.of(), List.of());

        // Build recipe
        SewingRecipe recipe = new SewingRecipe(

                new ArrayList<>(this.rows),
                stringKeyMap,
                ingredients,
                new ItemStack(this.result, this.count),
                this.stitches,
                actualFailedResult,
                actualSewingMachineTier,
                actualRequiresPattern,
                actualBlueprintTypes,
                actualHasQuality,
                actualNeedsMinigame,
                this.showNotification,
                actualMinQuality,
                actualQualityDiff,
                actualQualityModifiers,
                this.group == null ? "" : this.group,
                width,
                height

        );

        output.accept(id, recipe,
                advBuilder.build(id.withPrefix("recipes/" + this.category.getFolderName() + "/")));
    }


    private void ensureValid(ResourceLocation pRecipeId) {
        if (this.rows.isEmpty()) {
            throw new IllegalStateException("No pattern is defined for shaped forging recipe " + pRecipeId + "!");
        }
        int width = this.rows.getFirst().length();
        for (String row : this.rows) {
            if (row.length() != width) {
                throw new IllegalStateException("Pattern must be the same width on every line!");
            }
        }
    }
}
