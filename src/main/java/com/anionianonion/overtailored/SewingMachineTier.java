package com.anionianonion.overtailored;

import net.minecraft.util.StringRepresentable;

//based on https://github.com/phuccom000/Overgeared/blob/master/src/main/java/net/stirdrem/overgeared/AnvilTier.java
//just renamed enums and class name, which is also changed in method parameters.

public enum SewingMachineTier implements StringRepresentable {
    POOR("poor", "gui.overtailored.tier.poor"),
    WELL("well", "gui.overtailored.tier.well"),
    EXPERT("expert", "gui.overtailored.tier.expert"),
    PERFECT("perfect", "gui.overtailored.tier.perfect"),
    MASTER("master", "gui.overtailored.tier.master");

    private final String displayName;
    private final String lang;

    SewingMachineTier(String displayName, String lang) {
        this.displayName = displayName;
        this.lang = lang;
    }

    @Override
    public String getSerializedName() {
        return displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getLang() {
        return lang;
    }

    public static SewingMachineTier fromDisplayName(String name) {
        for (SewingMachineTier tier : values()) {
            if (tier.displayName.equalsIgnoreCase(name)) {
                return tier;
            }
        }
        return null; // or throw IllegalArgumentException
    }

    public boolean isEqualOrLowerThan(SewingMachineTier other) {
        return this.ordinal() <= other.ordinal();
    }
}
