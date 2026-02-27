package com.anionianonion.overtailored.item;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Locale;

//copy of ToolType from https://github.com/phuccom000/Overgeared/blob/1.21.1/src/main/java/net/stirdrem/overgeared/item/ToolType.java
public class ArmorType {

  public static final ArmorType HEAD = new ArmorType("HEAD");
  public static final ArmorType CHEST = new ArmorType("CHEST");
  public static final ArmorType LEGS = new ArmorType("LEGS");
  public static final ArmorType FEET = new ArmorType("FEET");

  private final String id;
  private final String translationKey;

  public ArmorType(String id) {
    if (id == null || id.isEmpty())
      throw new IllegalArgumentException("Armor type ID cannot be null or empty");

    if (!id.matches("^[A-Za-z0-9_]+$"))
      throw new IllegalArgumentException("Armor type ID must be alphanumeric with underscores");

    this.id = id.toLowerCase(Locale.ROOT);

    this.translationKey = "armor_type.overtailored." + this.id;
  }

  public String getId() {
    return id.toLowerCase();
  }

  public MutableComponent getDisplayName() {
    Component trans = Component.translatable(translationKey);
    return trans.copy();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ArmorType)) return false;
    return id.equals(((ArmorType) o).id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
