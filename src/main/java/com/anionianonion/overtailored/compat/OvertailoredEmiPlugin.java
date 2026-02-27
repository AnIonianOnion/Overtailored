package com.anionianonion.overtailored.compat;

import com.anionianonion.overtailored.OvertailoredMod;
import com.anionianonion.overtailored.block.ModBlocks;
import com.anionianonion.overtailored.recipe.ModRecipeTypes;
import com.anionianonion.overtailored.recipe.SewingRecipe;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.common.Tags;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//copied from https://github.com/phuccom000/Overgeared/blob/1.21.1/src/main/java/net/stirdrem/overgeared/compat/emi/OvergearedEmiPlugin.java
/**
 * EMI integration for displaying Forging recipes.
 * This class is only loaded when EMI is present (handled by @EmiEntrypoint).
 */
@EmiEntrypoint
public class OvertailoredEmiPlugin implements EmiPlugin {

    public static final EmiStack WORKSTATION = EmiStack.of(ModBlocks.SEWING_MACHINE_BLOCK.get());

    public static final EmiRecipeCategory FORGING_CATEGORY = new EmiRecipeCategory(
            OvertailoredMod.loc("sewing"),
            WORKSTATION,
            new EmiTexture(OvertailoredMod.loc("textures/gui/smithing_anvil_jei.png"), 0, 0, 16, 16)
    ) {
        @Override
        public Component getName() {
            return Component.translatable("gui.overtailored.any_sewing_station");
        }
    };

    // Priority for sorting recipes by category
    private static final Map<String, Integer> CATEGORY_PRIORITY = Map.of(
            "tool_head", 0,
            "tools", 1,
            "armor", 2,
            "plate", 3,
            "misc", 4
    );

    @Override
    public void register(EmiRegistry registry) {
        OvertailoredMod.LOGGER.info("Registering EMI plugin for Overgeared recipes.");

        // Register the forging category
        registry.addCategory(FORGING_CATEGORY);

        // Register all smithing anvil blocks as workstations (ordered by tier: Stone -> Iron -> A -> B)
        registry.addWorkstation(FORGING_CATEGORY, EmiStack.of(ModBlocks.SIMPLE_SEWING_STATION_BLOCK.get()));
        registry.addWorkstation(FORGING_CATEGORY, EmiStack.of(ModBlocks.SEWING_MACHINE_BLOCK.get()));

        // Collect and sort all forging recipes
        List<RecipeHolder<SewingRecipe>> allRecipes = new ArrayList<>(
                registry.getRecipeManager().getAllRecipesFor(ModRecipeTypes.SEWING.get())
        );

        // Sort recipes by category priority, then alphabetically by output name
        allRecipes.sort((a, b) -> {
            String catA = categorizeRecipe(a.value());
            String catB = categorizeRecipe(b.value());

            int priorityA = CATEGORY_PRIORITY.getOrDefault(catA, 999);
            int priorityB = CATEGORY_PRIORITY.getOrDefault(catB, 999);

            if (priorityA != priorityB) {
                return Integer.compare(priorityA, priorityB);
            }

            // Fallback: alphabetical by display name
            return a.value().getResultItem(null).getDisplayName().getString()
                    .compareToIgnoreCase(b.value().getResultItem(null).getDisplayName().getString());
        });

        // Add sorted recipes
        for (RecipeHolder<SewingRecipe> holder : allRecipes) {
            registry.addRecipe(new SewingEmiRecipe(holder));
        }

        OvertailoredMod.LOGGER.info("EMI plugin registered successfully.");
    }

    /**
     * Categorize a recipe for sorting purposes.
     */
    private static String categorizeRecipe(SewingRecipe recipe) {
        ItemStack output = recipe.getResultItem(null);
        if (output.is(Tags.Items.ARMORS)) return "armor";
        return "misc";
    }
}
