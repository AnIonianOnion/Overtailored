package com.anionianonion.overtailored.compat;

import com.anionianonion.overtailored.SewingMachineTier;
import com.anionianonion.overtailored.components.PatternData;
import com.anionianonion.overtailored.components.ModComponents;
import com.anionianonion.overtailored.item.ModItems;
import com.anionianonion.overtailored.recipe.SewingRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


//based 99% on https://github.com/phuccom000/Overgeared/blob/1.21.1/src/main/java/net/stirdrem/overgeared/compat/emi/ForgingEmiRecipe.java
public class SewingEmiRecipe implements EmiRecipe {
    private static final int DISPLAY_WIDTH = 150;
    private static final int DISPLAY_HEIGHT = 66;
    private static final int X_OFFSET = 8; // Offset to center the recipe

    private final ResourceLocation id;
    private final SewingRecipe recipe;
    private final List<EmiIngredient> inputs;
    private final List<EmiStack> outputs;
    private final List<EmiStack> outputFailure;
    private final List<EmiStack> blueprintStacks;

    public SewingEmiRecipe(RecipeHolder<SewingRecipe> holder) {
        this.id = holder.id();
        this.recipe = holder.value();

        // Convert ingredients to EMI format
        this.inputs = recipe.getIngredients().stream()
                .map(EmiIngredient::of)
                .toList();

        this.outputs = List.of(EmiStack.of(recipe.getResultItem(null)));
        ItemStack failureStack = recipe.getFailedResultItem(null).copy();
        failureStack.set(ModComponents.FAILED_RESULT, true);
        this.outputFailure = List.of(EmiStack.of(failureStack));

        // Create blueprint stacks for recipes that support blueprints
        this.blueprintStacks = createBlueprintStacks();
    }

    /**
     * Create blueprint ItemStacks for this recipe's valid blueprint types.
     */
    private List<EmiStack> createBlueprintStacks() {
        Set<String> types = recipe.getPatternTypes();
        if (types.isEmpty()) {
            return List.of();
        }

        List<EmiStack> stacks = new ArrayList<>();
        for (String type : types) {
            ItemStack stack = new ItemStack(ModItems.CLOTHES_PATTERN.get());
            // Use PatternData with builder pattern
            PatternData data = PatternData.createDefault()
                    .withArmorType(type);
            stack.set(ModComponents.PATTERN_DATA, data);
            stack.set(ModComponents.PATTERN_REQUIRED, recipe.requiresClothPattern());
            stacks.add(EmiStack.of(stack));
        }
        return stacks;
    }


    @Override
    public EmiRecipeCategory getCategory() {
        return OvertailoredEmiPlugin.FORGING_CATEGORY;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return inputs;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return outputs;
    }

    @Override
    public int getDisplayWidth() {
        return DISPLAY_WIDTH;
    }

    @Override
    public int getDisplayHeight() {
        return DISPLAY_HEIGHT;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        // Blueprint slot on the left
        int blueprintX = X_OFFSET;
        int blueprintY = 24;
        if (!blueprintStacks.isEmpty()) {
            // Show blueprint variants cycling through
            widgets.addSlot(EmiIngredient.of(blueprintStacks), blueprintX, blueprintY);
        } else {
            widgets.addSlot(EmiStack.EMPTY, blueprintX, blueprintY);
        }

        // 3x3 crafting grid
        int gridStartX = X_OFFSET + 24;
        int gridStartY = 6;
        int slotSize = 18;

        int recipeWidth = recipe.width;
        int recipeHeight = recipe.height;

        NonNullList<SewingRecipe.SewingIngredient> forgingIngredients =
                recipe.getSewingIngredients();

        // Create all 9 slots of the 3x3 grid
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int x = gridStartX + col * slotSize;
                int y = gridStartY + row * slotSize;

                int recipeIndex = row * recipeWidth + col;

                if (col < recipeWidth && row < recipeHeight && recipeIndex < forgingIngredients.size()) {

                    SewingRecipe.SewingIngredient forgingIngredient =
                            forgingIngredients.get(recipeIndex);

                    Ingredient ingredient = forgingIngredient.ingredient();

                    EmiIngredient emiIngredient = EmiIngredient.of(ingredient);

                    widgets.addSlot(emiIngredient, x, y);
                } else {
                    widgets.addSlot(EmiStack.EMPTY, x, y);
                }
            }
        }


        // Arrow texture
        widgets.addTexture(EmiTexture.EMPTY_ARROW, X_OFFSET + 82, 24);

        // Output slot
        int outputX = X_OFFSET + 110;
        int outputY = 20;
        if (outputFailure.getFirst().isEmpty())
            widgets.addSlot(outputs.getFirst(), outputX, outputY).large(true).recipeContext(this);
        else {
            widgets.addSlot(outputs.getFirst(), outputX, outputY + 4 - 9).large(false).recipeContext(this);
            widgets.addSlot(outputFailure.getFirst(), outputX, outputY + 4 + 9).large(false).recipeContext(this);
        }
        // Draw "Hits: X" text (top right, above arrow)
        String hitsText = Component.translatable("tooltip.overtailored.recipe.stitches", recipe.getRemainingHits()).getString();
        widgets.addText(Component.literal(hitsText), X_OFFSET + 82, 6, 0xFF808080, false);

        // Draw "Tier: X" text (bottom, below grid)
        String tierRaw = recipe.getSewingMachineTier();
        SewingMachineTier tierEnum = SewingMachineTier.fromDisplayName(tierRaw);
        Component tierText = Component.translatable("tooltip.overtailored.recipe.tier")
                .append(Component.literal(" "))
                .append(Component.translatable(tierEnum.getLang()));
        widgets.addText(tierText, X_OFFSET + 82, 54, 0xFF808080, false);
    }
}
