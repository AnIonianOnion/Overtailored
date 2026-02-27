package com.anionianonion.overtailored.datapack;

import net.minecraft.resources.ResourceLocation;

import java.util.List;

//100% based on https://github.com/phuccom000/Overgeared/blob/1.21.1/src/main/java/net/stirdrem/overgeared/datapack/BlueprintTooltypesData.java
public class PatternArmorTypesData {

    private final ResourceLocation id;
    private final List<String> armorTypes;

    public PatternArmorTypesData(ResourceLocation id, List<String> armorTypes) {
        this.id = id;
        this.armorTypes = armorTypes;
    }

    public ResourceLocation getId() {
        return id;
    }

    public List<String> getArmorTypes() {
        return armorTypes;
    }
}
