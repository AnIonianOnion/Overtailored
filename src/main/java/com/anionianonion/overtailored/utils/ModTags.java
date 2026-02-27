package com.anionianonion.overtailored.utils;

import com.anionianonion.overtailored.OvertailoredMod;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

//removed old keys from Overgeared
//added keys of tags for this mod
//replaced OvergearedMod.loc() with OvertailoredMod.loc();
//- anIonianOnion
public class ModTags {

    public static class Blocks {
        public static final TagKey<Block> SEWING_MACHINE = tag("sewing_machine");
        public static final TagKey<Block> SEWING_STATION_BASES = tag("sewing_station_bases");

        private static TagKey<Block> tag(String name) {
            return BlockTags.create(OvertailoredMod.loc(name));
        }
    }

    public static class Items {
        public static final TagKey<Item> SEWING_UTENSILS = tag("sewing_utensils");

        private static TagKey<Item> tag(String name) {
            return ItemTags.create(OvertailoredMod.loc(name));
        }
    }
}
