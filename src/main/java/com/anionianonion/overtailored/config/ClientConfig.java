package com.anionianonion.overtailored.config;

import net.neoforged.neoforge.common.ModConfigSpec;

//copy of https://github.com/phuccom000/Overgeared/blob/1.21.1/src/main/java/net/stirdrem/overgeared/config/ClientConfig.java
public class ClientConfig {

    public static final ModConfigSpec CLIENT_CONFIG;

    public static final ModConfigSpec.IntValue MINIGAME_OVERLAY_HEIGHT;
    public static final ModConfigSpec.BooleanValue POP_UP_TOGGLE;

    static {
        final ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("Minigame Config");

        MINIGAME_OVERLAY_HEIGHT = builder
                .comment("Vertical position of the minigame overlay")
                .defineInRange("overlayHeight", 55, -10000, 10000);
        POP_UP_TOGGLE = builder
                .comment("If minigame's pop up appear during minigame.")
                .define("PopupVisible", true);

        builder.pop();

        CLIENT_CONFIG = builder.build();
    }
}
