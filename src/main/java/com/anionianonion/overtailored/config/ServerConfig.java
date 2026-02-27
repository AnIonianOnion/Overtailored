package com.anionianonion.overtailored.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.Arrays;
import java.util.List;

//based on https://github.com/phuccom000/Overgeared/blob/1.21.1/src/main/java/net/stirdrem/overgeared/config/ServerConfig.java
public class ServerConfig {

    public static final ModConfigSpec SERVER_CONFIG;

    // --- Core Sewing Machine Configs ---
    public static final ModConfigSpec.IntValue MAX_WORKBENCH_DISTANCE;
    public static final ModConfigSpec.BooleanValue ENABLE_STONE_TO_ANVIL;
    public static final ModConfigSpec.BooleanValue ENABLE_ANVIL_TO_SMITHING;
    // --- Tool/Blueprint Settings ---
    public static final ModConfigSpec.ConfigValue<List<? extends String>> AVAILABLE_ARMOR_TYPES;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> HIDDEN_TOOL_TYPES;

    public static final ModConfigSpec.IntValue MASTER_MAX_USE;
    public static final ModConfigSpec.IntValue PERFECT_MAX_USE;
    public static final ModConfigSpec.IntValue EXPERT_MAX_USE;
    public static final ModConfigSpec.IntValue WELL_MAX_USE;
    public static final ModConfigSpec.IntValue POOR_MAX_USE;

    // --- Minigame Settings ---
    public static final ModConfigSpec.BooleanValue ENABLE_MINIGAME;
    public static final ModConfigSpec.BooleanValue INGREDIENTS_DEFINE_MAX_QUALITY;
    public static final ModConfigSpec.DoubleValue MASTER_QUALITY_CHANCE;
    public static final ModConfigSpec.DoubleValue MASTER_FROM_INGREDIENT_CHANCE;


    public static final ModConfigSpec.DoubleValue PERFECT_QUALITY_SCORE;
    public static final ModConfigSpec.DoubleValue EXPERT_QUALITY_SCORE;
    public static final ModConfigSpec.DoubleValue WELL_QUALITY_SCORE;


    // --- Durability & Grinding ---
    public static final ModConfigSpec.DoubleValue BASE_DURABILITY_MULTIPLIER;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> BASE_DURABILITY_BLACKLIST;

    // --- Quality & Failure Chances ---
    public static final ModConfigSpec.DoubleValue FAIL_ON_WELL_QUALITY_CHANCE;
    public static final ModConfigSpec.DoubleValue FAIL_ON_EXPERT_QUALITY_CHANCE;

    // --- Durability Bonuses ---
    public static final ModConfigSpec.DoubleValue MASTER_DURABILITY_BONUS;
    public static final ModConfigSpec.DoubleValue PERFECT_DURABILITY_BONUS;
    public static final ModConfigSpec.DoubleValue EXPERT_DURABILITY_BONUS;
    public static final ModConfigSpec.DoubleValue WELL_DURABILITY_BONUS;
    public static final ModConfigSpec.DoubleValue POOR_DURABILITY_BONUS;

    public static ModConfigSpec.IntValue DEFAULT_ZONE_STARTING_SIZE;
    public static ModConfigSpec.DoubleValue DEFAULT_ZONE_SHRINK_FACTOR;
    public static ModConfigSpec.IntValue DEFAULT_MIN_PERFECT_ZONE;
    public static ModConfigSpec.DoubleValue DEFAULT_ARROW_SPEED;
    public static ModConfigSpec.DoubleValue DEFAULT_ARROW_SPEED_INCREASE;
    public static ModConfigSpec.DoubleValue DEFAULT_MAX_ARROW_SPEED;
    public static final ModConfigSpec.IntValue POOR_ZONE_STARTING_SIZE;
    public static final ModConfigSpec.IntValue POOR_MIN_PERFECT_ZONE;
    public static final ModConfigSpec.DoubleValue POOR_ZONE_SHRINK_FACTOR;
    public static final ModConfigSpec.DoubleValue POOR_ARROW_SPEED;
    public static final ModConfigSpec.DoubleValue POOR_ARROW_SPEED_INCREASE;
    public static final ModConfigSpec.DoubleValue POOR_MAX_ARROW_SPEED;
    public static ModConfigSpec.IntValue WELL_ZONE_STARTING_SIZE;
    public static ModConfigSpec.DoubleValue WELL_ZONE_SHRINK_FACTOR;
    public static ModConfigSpec.IntValue WELL_MIN_PERFECT_ZONE;
    public static ModConfigSpec.DoubleValue WELL_ARROW_SPEED;
    public static ModConfigSpec.DoubleValue WELL_ARROW_SPEED_INCREASE;
    public static ModConfigSpec.DoubleValue WELL_MAX_ARROW_SPEED;
    public static ModConfigSpec.IntValue EXPERT_ZONE_STARTING_SIZE;
    public static ModConfigSpec.DoubleValue EXPERT_ZONE_SHRINK_FACTOR;
    public static ModConfigSpec.IntValue EXPERT_MIN_PERFECT_ZONE;
    public static ModConfigSpec.DoubleValue EXPERT_ARROW_SPEED;
    public static ModConfigSpec.DoubleValue EXPERT_ARROW_SPEED_INCREASE;
    public static ModConfigSpec.DoubleValue EXPERT_MAX_ARROW_SPEED;
    public static ModConfigSpec.IntValue PERFECT_ZONE_STARTING_SIZE;
    public static ModConfigSpec.DoubleValue PERFECT_ZONE_SHRINK_FACTOR;
    public static ModConfigSpec.IntValue PERFECT_MIN_PERFECT_ZONE;
    public static ModConfigSpec.DoubleValue PERFECT_ARROW_SPEED;
    public static ModConfigSpec.DoubleValue PERFECT_ARROW_SPEED_INCREASE;
    public static ModConfigSpec.DoubleValue PERFECT_MAX_ARROW_SPEED;
    public static ModConfigSpec.IntValue MASTER_ZONE_STARTING_SIZE;
    public static ModConfigSpec.DoubleValue MASTER_ZONE_SHRINK_FACTOR;
    public static ModConfigSpec.IntValue MASTER_MIN_PERFECT_ZONE;
    public static ModConfigSpec.DoubleValue MASTER_ARROW_SPEED;
    public static ModConfigSpec.DoubleValue MASTER_ARROW_SPEED_INCREASE;
    public static ModConfigSpec.DoubleValue MASTER_MAX_ARROW_SPEED;

    public static ModConfigSpec.BooleanValue PLAYER_AUTHOR_TOOLTIPS;
    public static final ModConfigSpec.BooleanValue EXPERT_ABOVE_INCREASE_BLUEPRINT;

    static {
        final ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        // --- Anvil Conversion ---
        builder.push("Anvil Conversion");
        ENABLE_STONE_TO_ANVIL = builder.comment("Allow shift-right-clicking stone to convert into Stone Smithing Anvil").define("enableStoneToAnvil", true);
        ENABLE_ANVIL_TO_SMITHING = builder.comment("Allow shift-right-clicking vanilla anvil to convert into Smithing Anvil").define("enableAnvilToSmithing", true);
        builder.pop();

        builder.push("Minigame Common Settings");
        ENABLE_MINIGAME = builder.comment("Toggle for the forging minigame").define("enableMinigame", true);
        INGREDIENTS_DEFINE_MAX_QUALITY = builder.comment("Toggle for if ingredients' quality define the result's").define("ingredientsDefineQuality", true);
        MASTER_QUALITY_CHANCE = builder.comment("How likely it is for the player to get Masterwork when getting Perfectly Forged. Set to 0 to disable it.").defineInRange("masterQualityChance", 0.05, 0, 1);
        MASTER_FROM_INGREDIENT_CHANCE = builder.comment("Chance that using a Master-quality ingredient results in Master-quality result").defineInRange("masterFromIngredientChance", 0.5, 0.0, 1.0);
        MAX_WORKBENCH_DISTANCE = builder.comment("Maximum distance you can go from your Smithing Anvil before minigame reset").defineInRange("maxAnvilDistance", 100, 0, 1000);
        PLAYER_AUTHOR_TOOLTIPS = builder.comment("Toggle for if the result item has player's name").define("enableAuthorTooltips", true);

        PERFECT_QUALITY_SCORE = builder.comment("Lowest score required to get perfect quality").defineInRange("perfectQualityScore", 0.9, 0, 1.0);
        EXPERT_QUALITY_SCORE = builder.comment("Lowest score required to get expert quality").defineInRange("expertQualityScore", 0.6, 0, 1.0);
        WELL_QUALITY_SCORE = builder.comment("Lowest score required to get well quality").defineInRange("wellQualityScore", 0.3, 0, 1.0);

        builder.pop();

        builder.push("Durability & Grinding");
        BASE_DURABILITY_MULTIPLIER = builder.comment("Defines the base durability multiplier of all items that has durability.").defineInRange("durability", 1f, 0, 10000);
        BASE_DURABILITY_BLACKLIST = builder.comment("Items or tags that will NOT receive base durability multiplier").defineListAllowEmpty("base_durability_blacklist", List.of("minecraft:flint_and_steel", "overgeared:fired_tool_cast"), o -> o instanceof String);
        builder.pop();

        builder.push("Quality Failure Chances");
        FAIL_ON_WELL_QUALITY_CHANCE = builder.comment("Chance that forging with WELL quality fails").defineInRange("failOnWellQualityChance", 0.1, 0.0, 1.0);
        FAIL_ON_EXPERT_QUALITY_CHANCE = builder.comment("Chance that forging with EXPERT quality fails").defineInRange("failOnExpertQualityChance", 0.05, 0.0, 1.0);
        builder.pop();

        builder.push("Durability Bonuses");
        MASTER_DURABILITY_BONUS = builder.defineInRange("masterDurabilityBonus", 1.6, -5.0, 5.0);
        PERFECT_DURABILITY_BONUS = builder.defineInRange("perfectDurabilityBonus", 1.5, -5.0, 5.0);
        EXPERT_DURABILITY_BONUS = builder.defineInRange("expertDurabilityBonus", 1.3, -5.0, 5.0);
        WELL_DURABILITY_BONUS = builder.defineInRange("wellDurabilityBonus", 1, -5.0, 5.0);
        POOR_DURABILITY_BONUS = builder.defineInRange("poorDurabilityBonus", 0.7, 0, 5.0);
        builder.pop();

        builder.push("Blueprint & Armor Types");
        AVAILABLE_ARMOR_TYPES = builder.comment(
                        "List of available armor types for blueprints. Default options: head, chest, legs, feet. You may freely add or remove types.",
                        "To add a custom blueprint type: add it to availableArmorTypes, then define its display name in your lang file.",
                        "Template format: \"availableArmorTypes\": [\"sword\", \"axe\", \"your_custom_type\"]",
                        "Lang format: armor_type.overtailored.your_custom_type"
                )
                .defineList("availableArmorTypes",
                        Arrays.asList("head", "chest", "legs", "feet"),
                        entry -> entry instanceof String
                );

        HIDDEN_TOOL_TYPES = builder.comment(
                        "List of hidden tool types for blueprints. These exist but do NOT appear in the Drafting Table."
                )
                .defineList("hiddenToolTypes",
                        Arrays.asList(),
                        entry -> entry instanceof String
                );

        MASTER_MAX_USE = builder.comment("Uses required to reach the next quality after Master").defineInRange("masterMaxUse", 0, 0, Integer.MAX_VALUE);
        PERFECT_MAX_USE = builder.comment("Uses required to reach the next quality after Perfect").defineInRange("perfectMaxUse", 50, 0, 1000);
        EXPERT_MAX_USE = builder.comment("Uses required to reach the next quality after Expert").defineInRange("expertMaxUse", 20, 0, 1000);
        WELL_MAX_USE = builder.comment("Uses required to reach the next quality after Well").defineInRange("wellMaxUse", 10, 0, 1000);
        POOR_MAX_USE = builder.comment("Uses required to reach the next quality after Poor").defineInRange("poorMaxUse", 5, 0, 1000);

        EXPERT_ABOVE_INCREASE_BLUEPRINT = builder.comment("Only increase blueprint's use if you get Expert or above in minigame.").define("expertAboveIncreaseBlueprintToggle", true);

        builder.pop();



        builder.push("Default (No Blueprint)");
        DEFAULT_ZONE_STARTING_SIZE = builder.comment("Zone starting size for default forging (in % chance)").defineInRange("zoneStartingSize", 20, 0, 100);
        DEFAULT_ZONE_SHRINK_FACTOR = builder.comment("Zone shrink factor for default forging").defineInRange("zoneShrinkFactor", 0.9, 0, 1);
        DEFAULT_MIN_PERFECT_ZONE = builder.comment("Minimum perfect zone size for default forging").defineInRange("minPerfectZone", 8, 0, 100);
        DEFAULT_ARROW_SPEED = builder.comment("Arrow speed for default forging").defineInRange("arrowSpeed", 2.0, -5.0, 5.0);
        DEFAULT_ARROW_SPEED_INCREASE = builder.comment("Arrow speed increase per hit for default forging").defineInRange("arrowSpeedIncrease", 0.6, -5.0, 5.0);
        DEFAULT_MAX_ARROW_SPEED = builder.comment("Maximum arrow speed for default forging").defineInRange("maxArrowSpeed", 8.0, 0.0, 10.0);
        builder.pop();

        builder.push("Poorly Forged");
        POOR_ZONE_STARTING_SIZE = builder.comment("Zone starting size for POOR forging").defineInRange("zoneStartingSize", 30, 0, 100);
        POOR_ZONE_SHRINK_FACTOR = builder.comment("Zone shrink factor for POOR forging").defineInRange("zoneShrinkFactor", 0.9, 0.0, 1.0);
        POOR_MIN_PERFECT_ZONE = builder.comment("Minimum perfect zone size for POOR forging").defineInRange("minPerfectZone", 15, 0, 100);
        POOR_ARROW_SPEED = builder.comment("Arrow speed for POOR forging").defineInRange("arrowSpeed", 1.5, -5.0, 5.0);
        POOR_ARROW_SPEED_INCREASE = builder.comment("Arrow speed increase per hit for POOR forging").defineInRange("arrowSpeedIncrease", 0.5, -5.0, 5.0);
        POOR_MAX_ARROW_SPEED = builder.comment("Maximum arrow speed for POOR forging").defineInRange("maxArrowSpeed", 4.0, 0.0, 10.0);
        builder.pop();

        builder.push("Well Forged");
        WELL_ZONE_STARTING_SIZE = builder.comment("Zone starting size for WELL forging").defineInRange("zoneStartingSize", 20, 0, 100);
        WELL_ZONE_SHRINK_FACTOR = builder.comment("Zone shrink factor for WELL forging").defineInRange("zoneShrinkFactor", 0.8, 0, 1);
        WELL_MIN_PERFECT_ZONE = builder.comment("Minimum perfect zone size for WELL forging").defineInRange("minPerfectZone", 12, 0, 100);
        WELL_ARROW_SPEED = builder.comment("Arrow speed for WELL forging").defineInRange("arrowSpeed", 2.0, -5.0, 5.0);
        WELL_ARROW_SPEED_INCREASE = builder.comment("Arrow speed increase per hit for WELL forging").defineInRange("arrowSpeedIncrease", 0.7, -5.0, 5.0);
        WELL_MAX_ARROW_SPEED = builder.comment("Maximum arrow speed for WELL forging").defineInRange("maxArrowSpeed", 5.0, 0.0, 10.0);
        builder.pop();

        // EXPERT
        builder.push("Expertly Forged");
        EXPERT_ZONE_STARTING_SIZE = builder.comment("Zone starting size for EXPERT forging").defineInRange("zoneStartingSize", 18, 0, 100);
        EXPERT_ZONE_SHRINK_FACTOR = builder.comment("Zone shrink factor for EXPERT forging").defineInRange("zoneShrinkFactor", 0.8, 0, 1);
        EXPERT_MIN_PERFECT_ZONE = builder.comment("Minimum perfect zone size for EXPERT forging").defineInRange("minPerfectZone", 10, 0, 100);
        EXPERT_ARROW_SPEED = builder.comment("Arrow speed for EXPERT forging").defineInRange("arrowSpeed", 2.5, -5.0, 5.0);
        EXPERT_ARROW_SPEED_INCREASE = builder.comment("Arrow speed increase per hit for EXPERT forging").defineInRange("arrowSpeedIncrease", 0.85, -5.0, 5.0);
        EXPERT_MAX_ARROW_SPEED = builder.comment("Maximum arrow speed for EXPERT forging").defineInRange("maxArrowSpeed", 6.0, 0.0, 10.0);
        builder.pop();

        // PERFECT
        builder.push("Perfectly Forged");
        PERFECT_ZONE_STARTING_SIZE = builder.comment("Zone starting size for PERFECT forging").defineInRange("zoneStartingSize", 15, 0, 100);
        PERFECT_ZONE_SHRINK_FACTOR = builder.comment("Zone shrink factor for PERFECT forging").defineInRange("zoneShrinkFactor", 0.8, 0, 1);
        PERFECT_MIN_PERFECT_ZONE = builder.comment("Minimum perfect zone size for PERFECT forging").defineInRange("minPerfectZone", 10, 0, 100);
        PERFECT_ARROW_SPEED = builder.comment("Arrow speed for PERFECT forging").defineInRange("arrowSpeed", 3.0, -5.0, 5.0);
        PERFECT_ARROW_SPEED_INCREASE = builder.comment("Arrow speed increase per hit for PERFECT forging").defineInRange("arrowSpeedIncrease", 1.0, -5.0, 5.0);
        PERFECT_MAX_ARROW_SPEED = builder.comment("Maximum arrow speed for PERFECT forging").defineInRange("maxArrowSpeed", 7.0, 0.0, 10.0);
        builder.pop();

        // MASTER
        builder.push("Masterwork");
        MASTER_ZONE_STARTING_SIZE = builder.comment("Zone starting size for MASTER forging").defineInRange("zoneStartingSize", 12, 0, 100);
        MASTER_ZONE_SHRINK_FACTOR = builder.comment("Zone shrink factor for MASTER forging").defineInRange("zoneShrinkFactor", 0.7, 0, 1);
        MASTER_MIN_PERFECT_ZONE = builder.comment("Minimum perfect zone size for MASTER forging").defineInRange("minPerfectZone", 8, 0, 100);
        MASTER_ARROW_SPEED = builder.comment("Arrow speed for MASTER forging").defineInRange("arrowSpeed", 3.5, -5.0, 5.0);
        MASTER_ARROW_SPEED_INCREASE = builder.comment("Arrow speed increase per hit for MASTER forging").defineInRange("arrowSpeedIncrease", 1.2, -5.0, 5.0);
        MASTER_MAX_ARROW_SPEED = builder.comment("Maximum arrow speed for MASTER forging").defineInRange("maxArrowSpeed", 8.0, 0.0, 10.0);
        builder.pop();

        SERVER_CONFIG = builder.build();
    }

}
