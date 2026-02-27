package com.anionianonion.overtailored;

import com.anionianonion.overtailored.config.ServerConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.util.StringRepresentable;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.function.Supplier;

//modified from https://github.com/phuccom000/Overgeared/blob/1.21.1/src/main/java/net/stirdrem/overgeared/BlueprintQuality.java
public enum PatternQuality implements StringRepresentable {
    NONE("none", 0, ChatFormatting.GRAY, () -> null),

    POOR("poor", 10, ChatFormatting.RED, () -> ServerConfig.POOR_MAX_USE),
    WELL("well", 15, ChatFormatting.YELLOW, () -> ServerConfig.WELL_MAX_USE),
    EXPERT("expert", 20, ChatFormatting.BLUE, () -> ServerConfig.EXPERT_MAX_USE),
    PERFECT("perfect", 25, ChatFormatting.GOLD, () -> ServerConfig.PERFECT_MAX_USE),
    MASTER("master", 30, ChatFormatting.LIGHT_PURPLE, () -> ServerConfig.MASTER_MAX_USE); // Final tier

    private final String id;
    private final int defaultUse;
    private final ChatFormatting color;
    private final Supplier<ModConfigSpec.IntValue> configSupplier;

    PatternQuality(String id, int defaultUse, ChatFormatting color, Supplier<ModConfigSpec.IntValue> configSupplier) {
        this.id = id;
        this.defaultUse = defaultUse;
        this.color = color;
        this.configSupplier = configSupplier;
    }

    @Override
    public String getSerializedName() {
        return id;
    }

    public static int compare(String q1, String q2) {
        PatternQuality a = fromString(q1);
        PatternQuality b = fromString(q2);

        if (a == NONE && b == NONE) return 0;
        if (a == NONE) return -1;
        if (b == NONE) return 1;

        return Integer.compare(a.ordinal(), b.ordinal());
    }

    /**
     * Match a quality string safely.
     */
    public static PatternQuality fromString(String id) {
        for (PatternQuality q : values()) {
            if (q.id.equalsIgnoreCase(id)) return q;
        }
        return NONE;
    }

    /**
     * Get the next tier of blueprint quality.
     */
    public static PatternQuality getNext(PatternQuality current) {
        if (current == NONE) return POOR;

        int index = current.ordinal();
        if (index + 1 < values().length) {
            return values()[index + 1];
        }
        return MASTER; // already max
    }

    /**
     * Get the previous tier of blueprint quality.
     */
    public static PatternQuality getPrevious(PatternQuality current) {
        if (current == NONE) return NONE;

        int index = current.ordinal();
        if (index - 1 > 0) { // skip NONE
            return values()[index - 1];
        }
        return NONE;
    }

    public static ChatFormatting getColor(String qualityName) {
        for (PatternQuality q : values()) {
            if (q.name().equalsIgnoreCase(qualityName)) {
                return q.color; // assuming the color field exists in your enum
            }
        }
        return ChatFormatting.GRAY;
    }

    public String getDisplayName() {
        return id;
    }

    public int getUse() {
        if (this == NONE) return 0;

        try {
            return configSupplier.get().get();
        } catch (IllegalStateException e) {
            // Config not loaded yet, return default
            return defaultUse;
        }
    }

    public ChatFormatting getColor() {
        return color;
    }

    public String getTranslationKey() {
        return "tooltip.overtailored.pattern.quality." + name().toLowerCase();
    }

    public String getId() {
        return id;
    }

}
