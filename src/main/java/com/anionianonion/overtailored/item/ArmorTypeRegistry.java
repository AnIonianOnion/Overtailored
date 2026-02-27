package com.anionianonion.overtailored.item;

import com.anionianonion.overtailored.OvertailoredMod;
import com.anionianonion.overtailored.config.ServerConfig;
import com.anionianonion.overtailored.datapack.PatternArmorTypesReloadListener;

import java.util.*;
import java.util.stream.Collectors;

//based on https://github.com/phuccom000/Overgeared/blob/1.21.1/src/main/java/net/stirdrem/overgeared/item/ToolTypeRegistry.java
public class ArmorTypeRegistry {

  private static final List<ArmorType> HIDDEN_TYPES = new ArrayList<>();
  private static final Map<String, ArmorType> BY_ID = new HashMap<>();

  public static void init() {
    BY_ID.clear();
    HIDDEN_TYPES.clear();

    PatternArmorTypesReloadListener.DATA.values().forEach(data -> {
      for (String id : data.getArmorTypes()) {
        BY_ID.computeIfAbsent(id.toLowerCase(Locale.ROOT), ArmorType::new);
      }
    });

    // Load available types from config
    List<? extends String> availableIds = ServerConfig.AVAILABLE_ARMOR_TYPES.get();
    for (String id : availableIds) {
      BY_ID.computeIfAbsent(id.toLowerCase(Locale.ROOT), ArmorType::new);
    }

    // Load hidden types from config (same format: just IDs)
    List<? extends String> hiddenIds = ServerConfig.HIDDEN_TOOL_TYPES.get();
    for (String id : hiddenIds) {
      BY_ID.computeIfPresent(id.toLowerCase(Locale.ROOT), (k, t) -> {
        HIDDEN_TYPES.add(t);
        return t;
      });
    }

    System.out.println("Registered armor types: " +
            BY_ID.keySet().stream().collect(Collectors.joining(", "))
    );
    OvertailoredMod.LOGGER.info("Armor types initialized: {}",
            ArmorTypeRegistry.getRegisteredTypes().size());
  }

  public static List<ArmorType> getRegisteredTypes() {
    List<ArmorType> result = new ArrayList<>();
    List<? extends String> allowed = ServerConfig.AVAILABLE_ARMOR_TYPES.get();

    // If config list is EMPTY → allow everything (defaults + datapack)
    if (allowed.isEmpty()) {
      for (ArmorType type : BY_ID.values()) {
        if (!HIDDEN_TYPES.contains(type)) {
          result.add(type);
        }
      }
      return result;
    }

    // If config list is present → include ALL registered types, but filter out:
    // 1. Types not in the allowed list (unless allowed list is empty)
    // 2. Hidden types
    for (ArmorType type : BY_ID.values()) {
      boolean isHidden = HIDDEN_TYPES.contains(type);

      if (!isHidden) {
        result.add(type);
      }
    }

    return result;
  }


  public static List<ArmorType> getRegisteredTypesAll() {
    List<ArmorType> list = getRegisteredTypes();
    list.addAll(HIDDEN_TYPES);
    return list;
  }

  public static Optional<ArmorType> byId(String id) {
    if (id == null) return Optional.empty();
    return Optional.ofNullable(BY_ID.get(id.toLowerCase(Locale.ROOT)));
  }
}
