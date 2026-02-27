package com.anionianonion.overtailored.item.custom;

import com.anionianonion.overtailored.PatternQuality;
import com.anionianonion.overtailored.components.PatternData;
import com.anionianonion.overtailored.components.ModComponents;
import com.anionianonion.overtailored.item.ArmorType;
import com.anionianonion.overtailored.item.ArmorTypeRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

//based on https://github.com/phuccom000/Overgeared/blob/1.21.1/src/main/java/net/stirdrem/overgeared/item/custom/BlueprintItem.java
public class PatternItem extends Item {

    public PatternItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);

        PatternData data = stack.get(ModComponents.PATTERN_DATA);
        if (data == null) return;

        PatternQuality quality = data.getQualityEnum();

        // Only show quality & progress if not NONE
        if (quality != PatternQuality.NONE) {
            // Show quality
            tooltip.add(Component.translatable("tooltip.overtailored.pattern.quality")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.translatable(quality.getTranslationKey()).withStyle(quality.getColor())));

            if (quality == PatternQuality.PERFECT || quality == PatternQuality.MASTER) {
                tooltip.add(Component.translatable("tooltip.overtailored.pattern.maxlevel")
                        .withStyle(ChatFormatting.LIGHT_PURPLE));
            } else {
                // Show progress
                tooltip.add(Component.translatable(
                        "tooltip.overtailored.pattern.progress",
                        data.uses(),
                        data.usesToLevel()
                ).withStyle(ChatFormatting.GRAY));
            }
        }

        // Show tool type (always shown)
        ArmorType armorType = getArmorType(stack);
        tooltip.add(Component.translatable("tooltip.overtailored.pattern.armor_type")
                .withStyle(ChatFormatting.GRAY)
                .append(armorType.getDisplayName().withStyle(ChatFormatting.BLUE)));

        // Show required status
        if (stack.has(ModComponents.PATTERN_REQUIRED)) {
            if (Boolean.TRUE.equals(stack.get(ModComponents.PATTERN_REQUIRED)))
                tooltip.add(Component.translatable("tooltip.overtailored.pattern.required")
                        .withStyle(ChatFormatting.RED));
            else tooltip.add(Component.translatable("tooltip.overtailored.pattern.optional")
                    .withStyle(ChatFormatting.BLUE));
        }
    }

    public static PatternQuality getQuality(ItemStack stack) {
        PatternData data = stack.get(ModComponents.PATTERN_DATA);
        if (data == null) return PatternQuality.POOR;
        return data.getQualityEnum();
    }

    public static int getUses(ItemStack stack) {
        PatternData data = stack.get(ModComponents.PATTERN_DATA);
        return data == null ? 0 : data.uses();
    }

    public static int getUsesToNextLevel(ItemStack stack) {
        return getUsesToNextLevel(getQuality(stack));
    }

    public static ArmorType getArmorType(ItemStack stack) {
        PatternData data = stack.get(ModComponents.PATTERN_DATA);
        if (data == null || data.armorType().isEmpty()) {
            List<ArmorType> types = ArmorTypeRegistry.getRegisteredTypesAll();
            return !types.isEmpty() ? types.getFirst() : ArmorType.HEAD;
        }
        return ArmorTypeRegistry.byId(data.armorType()).orElse(ArmorType.HEAD);
    }

    public static void setDefaultData(ItemStack stack) {
        List<ArmorType> types = ArmorTypeRegistry.getRegisteredTypesAll();
        String defaultArmorType = !types.isEmpty() ? types.getFirst().getId() : "sword";

        PatternData data = new PatternData(
                PatternQuality.POOR.name(),
                defaultArmorType,
                0,
                getUsesToNextLevel(PatternQuality.POOR)
        );
        stack.set(ModComponents.PATTERN_DATA, data);
    }

    public static void cycleArmorType(ItemStack stack) {
        List<ArmorType> available = ArmorTypeRegistry.getRegisteredTypesAll();
        if (available.isEmpty()) return;

        ArmorType current = getArmorType(stack);
        int currentIndex = available.indexOf(current);
        int nextIndex = (currentIndex + 1) % available.size();

        PatternData data = stack.getOrDefault(ModComponents.PATTERN_DATA, PatternData.createDefault());
        stack.set(ModComponents.PATTERN_DATA, data.withArmorType(available.get(nextIndex).getId()));
    }

    private static int getUsesToNextLevel(PatternQuality quality) {
        return switch (quality) {
            case POOR -> PatternQuality.POOR.getUse();
            case WELL -> PatternQuality.WELL.getUse();
            case EXPERT -> PatternQuality.EXPERT.getUse();
            case PERFECT -> PatternQuality.PERFECT.getUse();
            case MASTER -> PatternQuality.MASTER.getUse();
            default -> PatternQuality.NONE.getUse();
        };
    }
}