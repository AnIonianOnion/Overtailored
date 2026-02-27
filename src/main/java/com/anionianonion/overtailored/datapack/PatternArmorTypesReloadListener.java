package com.anionianonion.overtailored.datapack;

import com.anionianonion.overtailored.OvertailoredMod;
import com.anionianonion.overtailored.item.ArmorTypeRegistry;
import com.google.gson.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.*;

//100% based on https://github.com/phuccom000/Overgeared/blob/1.21.1/src/main/java/net/stirdrem/overgeared/datapack/BlueprintTooltypesReloadListener.java
public class PatternArmorTypesReloadListener
        extends SimpleJsonResourceReloadListener {

    public static final Map<ResourceLocation, PatternArmorTypesData> DATA = new HashMap<>();
    
    private static final Gson GSON = new Gson();

    public PatternArmorTypesReloadListener() {
        super(GSON, "pattern_armor_types");
    }

    @Override
    protected void apply(
            Map<ResourceLocation, JsonElement> objects,
            ResourceManager resourceManager,
            ProfilerFiller profiler
    ) {
        DATA.clear();

        for (Map.Entry<ResourceLocation, JsonElement> entry : objects.entrySet()) {
            ResourceLocation id = entry.getKey();
            JsonObject json = entry.getValue().getAsJsonObject();

            try {
                if (!json.has("armor_types")) {
                    throw new JsonSyntaxException("Missing 'armor_types' array: " + id);
                }

                JsonArray arr = json.getAsJsonArray("armor_types");
                List<String> armor_types = new ArrayList<>();

                for (JsonElement e : arr) {
                    armor_types.add(e.getAsString().toLowerCase(Locale.ROOT));
                }

                PatternArmorTypesData data =
                        new PatternArmorTypesData(id, armor_types);

                DATA.put(id, data);

            } catch (Exception e) {
                OvertailoredMod.LOGGER.error(
                        "Failed to load blueprint armor_types: {}", id, e
                );
            }
        }

        OvertailoredMod.LOGGER.info(
                "Loaded {} blueprint armor_type packs",
                DATA.size()
        );

        // ✅ IMPORTANT: rebuild registry after /reload
        ArmorTypeRegistry.init();
    }
}
