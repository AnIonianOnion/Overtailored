package com.anionianonion.overtailored.components;

import com.anionianonion.overtailored.PatternQuality;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

//replaced String toolType parameter with String armorType parameter - anIonianOnion
//based on https://github.com/phuccom000/Overgeared/blob/1.21.1/src/main/java/net/stirdrem/overgeared/components/BlueprintData.java
public record PatternData(
        String quality,
        String armorType,
        int uses,
        int usesToLevel
) {
    private static final String DEFAULT_ARMOR_TYPE = "head";

    public static final Codec<PatternData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.optionalFieldOf("quality", PatternQuality.POOR.getId()).forGetter(PatternData::quality),
                    Codec.STRING.optionalFieldOf("armor_type", DEFAULT_ARMOR_TYPE).forGetter(PatternData::armorType),
                    Codec.INT.optionalFieldOf("uses", 0).forGetter(PatternData::uses),
                    Codec.INT.optionalFieldOf("uses_to_level", PatternQuality.POOR.getUse()).forGetter(PatternData::usesToLevel)
            ).apply(instance, PatternData::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, PatternData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, PatternData::quality,
            ByteBufCodecs.STRING_UTF8, PatternData::armorType,
            ByteBufCodecs.VAR_INT, PatternData::uses,
            ByteBufCodecs.VAR_INT, PatternData::usesToLevel,
            PatternData::new
    );

    /**
     * Creates a default PatternData instance.
     */
    public static PatternData createDefault() {
        return new PatternData(
                PatternQuality.NONE.getId(),
                DEFAULT_ARMOR_TYPE,
                0,
                PatternQuality.NONE.getUse()
        );
    }

    public PatternData withQuality(String quality) {
        return new PatternData(quality, this.armorType, this.uses, this.usesToLevel);
    }

    public PatternData withArmorType(String armorType) {
        return new PatternData(this.quality, armorType, this.uses, this.usesToLevel);
    }

    public PatternData withUses(int uses) {
        return new PatternData(this.quality, this.armorType, uses, this.usesToLevel);
    }

    public PatternData withUsesToLevel(int usesToLevel) {
        return new PatternData(this.quality, this.armorType, this.uses, usesToLevel);
    }

    public PatternQuality getQualityEnum() {
        return PatternQuality.fromString(quality);
    }
}
