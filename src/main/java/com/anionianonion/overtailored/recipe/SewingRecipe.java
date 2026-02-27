package com.anionianonion.overtailored.recipe;

import com.anionianonion.overtailored.SewingQuality;
import com.anionianonion.overtailored.components.PatternData;
import com.anionianonion.overtailored.components.ModComponents;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import com.anionianonion.overtailored.SewingMachineTier;

import java.util.*;
import java.util.stream.Collectors;

//based on https://github.com/phuccom000/Overgeared/blob/1.21.1/src/main/java/net/stirdrem/overgeared/recipe/ForgingRecipe.java
//added new QualityModifiers for customizable quality attributes, per item.
public class SewingRecipe implements Recipe<RecipeInput> {
    private static final int BLUEPRINT_SLOT = 11;
    private final String group;
    private final Set<String> patternTypes;
    private final List<String> pattern;
    private final String tier;
    private final Map<String, SewingIngredient> key;
    private final NonNullList<SewingIngredient> ingredients;
    private final ItemStack result;
    private final ItemStack failedResult;
    private final int stitches;

    private final boolean hasQuality;
    private final boolean needsMinigame;
    private final boolean requiresClothPattern;
    private final boolean showNotification;
    private final SewingQuality minimumQuality;
    private final SewingQuality qualityDifficulty;
    private final QualityModifiers qualityModifiers;
    public final int width;
    public final int height;


    //constructor's parameters must be and is in order from necessary to optional
    public SewingRecipe(List<String> pattern, Map<String, SewingIngredient> key,
                        NonNullList<SewingIngredient> ingredients,
                        ItemStack result, int stitches,
                        ItemStack failedResult,
                        String tier,
                        boolean requiresClothPattern, Set<String> patternTypes,
                        boolean hasQuality, boolean needsMinigame,
                        boolean showNotification, SewingQuality minimumQuality,
                        SewingQuality qualityDifficulty, QualityModifiers qualityModifiers,
                        String group, int width, int height) {


        this.group = group;
        this.requiresClothPattern = requiresClothPattern;
        this.patternTypes = patternTypes;
        this.tier = tier;

        this.pattern = pattern;
        this.key = key;
        this.ingredients = ingredients;

        this.result = result;
        this.failedResult = failedResult;
        this.stitches = stitches;
        this.hasQuality = hasQuality;
        this.needsMinigame = needsMinigame;

        this.showNotification = showNotification;
        this.minimumQuality = minimumQuality;
        this.qualityDifficulty = qualityDifficulty;

        this.qualityModifiers = qualityModifiers;

        this.width = width;
        this.height = height;


    }

    public List<AttributeModifier> getModifiersFor(SewingQuality q) {
        return switch (q) {
            case POOR -> this.qualityModifiers.poor();
            case WELL -> this.qualityModifiers.well();
            case EXPERT -> this.qualityModifiers.expert();
            case PERFECT -> this.qualityModifiers.perfect();
            case MASTER -> this.qualityModifiers.master();
            case NONE -> List.of(); // always empty
        };
    }


    public static Optional<SewingRecipe> findBestMatch(Level world, RecipeInput recipeInput) {
        return findBestMatchHolder(world, recipeInput).map(RecipeHolder::value);
    }

    public static Optional<RecipeHolder<SewingRecipe>> findBestMatchHolder(Level world, RecipeInput recipeInput) {
        return world.getRecipeManager().getAllRecipesFor(ModRecipeTypes.SEWING.get())
                .stream()
                .filter(holder -> holder.value().matches(recipeInput, world))
                .max(Comparator.comparingInt(holder -> holder.value().getRecipeSize()));
    }

    private boolean checkBlueprint(RecipeInput recipeInput) {
        ItemStack blueprintStack = recipeInput.getItem(BLUEPRINT_SLOT);

        // If no blueprints required
        if (patternTypes.isEmpty()) {
            return blueprintStack.isEmpty();
        }

        // Blueprint required, but slot empty
        if (blueprintStack.isEmpty()) return false;

        PatternData data = blueprintStack.get(ModComponents.PATTERN_DATA);
        if (data == null) return false;

        String toolType = data.armorType();
        if (toolType.isEmpty()) return false;

        return patternTypes.contains(toolType);
    }

    @Override
    public boolean matches(RecipeInput recipeInput, Level world) {
        SewingRecipe bestMatch = null;
        int bestPriority = -1;

        // Check all possible positions for all possible recipes
        for (int y = 0; y <= 3 - height; y++) {
            for (int x = 0; x <= 3 - width; x++) {
                if (matchesPattern(recipeInput, x, y) && checkSurroundingBlanks(recipeInput, x, y)) {
                    int currentPriority = calculatePriority();
                    if (currentPriority > bestPriority) {
                        bestPriority = currentPriority;
                        bestMatch = this;
                    }
                }
            }
        }

        return bestMatch == this;
    }

    private boolean checkSurroundingBlanks(RecipeInput recipeInput, int xOffset, int yOffset) {
        // Check if slots outside the recipe pattern are empty
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                // Skip slots that are part of the recipe
                if (x >= xOffset && x < xOffset + width &&
                        y >= yOffset && y < yOffset + height) {
                    continue;
                }

                // Check if non-recipe slots are empty
                int invSlot = y * 3 + x;
                if (!recipeInput.getItem(invSlot).isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private int calculatePriority() {
        // Calculate priority based on recipe size (bigger recipes have higher priority)
        // Add a small bonus for recipes that use more items to break ties
        int itemCount = 0;
        for (SewingIngredient ingredient : ingredients) {
            if (!ingredient.ingredient.isEmpty()) {
                itemCount++;
            }
        }
        return width * height * 100 + itemCount; // Multiplier ensures size dominates
    }

    private boolean matchesPattern(RecipeInput recipeInput, int xOffset, int yOffset) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int invSlot = (y + yOffset) * 3 + (x + xOffset);
                SewingIngredient ingredient = ingredients.get(y * width + x);

                if (ingredient.ingredient().isEmpty()) {
                    if (!recipeInput.getItem(invSlot).isEmpty()) return false;
                } else {
                    if (!ingredient.test(recipeInput.getItem(invSlot))) return false;
                }
            }
        }
        return true;
    }

    @Override
    public ItemStack assemble(RecipeInput recipeInput, HolderLookup.Provider registries) {
        ItemStack output = result.copy();

        for (int i = 0; i < ingredients.size(); i++) {
            SewingIngredient ingredient = ingredients.get(i);
            if (ingredient.ingredient.isEmpty()) continue;

            if (!ingredient.transferNBT()) continue;

            ItemStack input = recipeInput.getItem(i);
            if (input.isEmpty()) continue;

            // Merge data components instead of NBT
            mergeComponents(output, input);
        }

        // Apply quality-based attribute modifiers
        SewingQuality quality = output.get(ModComponents.SEWING_QUALITY.get());
        if (quality != null && quality != SewingQuality.NONE) {
            // Get the correct modifier list from the recipe
            List<AttributeModifier> mods = this.getModifiersFor(quality);
            if (!mods.isEmpty()) {
                // Store modifiers directly on the item
                output.set(ModComponents.QUALITY_MODIFIERS.get(), mods);
            }
        }

        return output;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= this.width && height >= this.height;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result.copy();
    }

    public ItemStack getFailedResultItem(HolderLookup.Provider registries) {
        return failedResult == null || failedResult.isEmpty() || failedResult.is(result.getItem()) ? ItemStack.EMPTY : failedResult.copy();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list =
                NonNullList.withSize(ingredients.size(), Ingredient.EMPTY);

        for (int i = 0; i < ingredients.size(); i++) {
            list.set(i, ingredients.get(i).ingredient);
        }

        return list;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.SEWING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.SEWING.get();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SewingRecipe that = (SewingRecipe) obj;
        return Objects.equals(this.result.getItem(), that.result.getItem()) &&
                Objects.equals(this.ingredients, that.ingredients);
    }

    @Override
    public int hashCode() {
        return Objects.hash(result.getItem(), ingredients);
    }

    public int getStitchesRequired() {
        return stitches;
    }

    public String getSewingMachineTier() {
        return tier;
    }

    public boolean hasQuality() {
        return hasQuality;
    }

    public boolean needsMinigame() {
        return needsMinigame;
    }

    public String getGroup() {
        return group;
    }

    public SewingQuality getMinimumQuality() {
        return minimumQuality;
    }

    public SewingQuality getQualityDifficulty() {
        return qualityDifficulty;
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    public int getRemainingHits() {
        return stitches;
    }

    public Set<String> getPatternTypes() {
        return patternTypes.stream()
                .map(s -> s.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
    }

    public boolean requiresClothPattern() {
        return requiresClothPattern;
    }

    public List<String> getPattern() {
        return pattern;
    }

    public Map<String, SewingIngredient> getKey() {
        return key;
    }

    private int getRecipeSize() {
        return width * height;
    }

    public NonNullList<SewingIngredient> getSewingIngredients() {
        return ingredients;
    }

    public static void mergeComponents(ItemStack target, ItemStack source) {
        DataComponentMap sourceComponents = source.getComponents();
        DataComponentMap targetComponents = target.getComponents();

        for (DataComponentType<?> type : sourceComponents.keySet()) {
            // If target already has component → overwrite
            if (targetComponents.has(type)) {
                target.remove(type);
            }

            // Copy component value
            copyComponent(target, source, type);
        }
    }

    private static <T> void copyComponent(ItemStack target, ItemStack source, DataComponentType<T> type) {
        T value = source.get(type);
        if (value != null) {
            target.set(type, value);
        }
    }

    public record SewingIngredient(Ingredient ingredient, boolean transferNBT) {
        public static final SewingIngredient EMPTY =
                new SewingIngredient(Ingredient.EMPTY, false);

        public boolean test(ItemStack stack) {
            return ingredient.test(stack); //changed test
        }

        public static final Codec<SewingIngredient> CODEC = new Codec<>() {
            private <T> boolean getBool(DynamicOps<T> ops, T input, String key) {
                return ops.get(input, key).flatMap(ops::getBooleanValue).result().orElse(false);
            }

            @Override
            public <T> DataResult<Pair<SewingIngredient, T>> decode(DynamicOps<T> ops, T input) {
                return Ingredient.CODEC.parse(ops, input).map(ing -> Pair.of(
                        new SewingIngredient(ing, getBool(ops, input, "transfer_nbt")),
                        input
                ));
            }

            @Override
            public <T> DataResult<T> encode(SewingIngredient input, DynamicOps<T> ops, T prefix) {
                return Ingredient.CODEC.encode(input.ingredient, ops, prefix).flatMap(encoded -> {
                    if (!input.transferNBT) return DataResult.success(encoded);

                    DataResult<T> result = DataResult.success(encoded);
                    if (input.transferNBT) {
                        result = result.flatMap(m -> ops.mergeToMap(m, ops.createString("transfer_nbt"), ops.createBoolean(true)));
                    }
                    return result;
                });
            }
        };

        /* ---------------- Network Codec ---------------- */

        public static final StreamCodec<RegistryFriendlyByteBuf, SewingIngredient> STREAM_CODEC =
                StreamCodec.of(SewingIngredient::write, SewingIngredient::read);

        private static SewingIngredient read(RegistryFriendlyByteBuf buf) {
            Ingredient ingredient = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
            boolean transferNBT = buf.readBoolean();
            return new SewingIngredient(ingredient, transferNBT);
        }

        private static void write(RegistryFriendlyByteBuf buf, SewingIngredient ingredient) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(buf, ingredient.ingredient());
            buf.writeBoolean(ingredient.transferNBT());
        }
    }

    public record QualityModifiers(
            List<AttributeModifier> poor,
            List<AttributeModifier> well,
            List<AttributeModifier> expert,
            List<AttributeModifier> perfect,
            List<AttributeModifier> master
    ) {
        public static final Codec<QualityModifiers> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                AttributeModifier.CODEC.listOf().optionalFieldOf("poor_modifiers", List.of()).forGetter(QualityModifiers::poor),
                AttributeModifier.CODEC.listOf().optionalFieldOf("well_modifiers", List.of()).forGetter(QualityModifiers::well),
                AttributeModifier.CODEC.listOf().optionalFieldOf("expert_modifiers", List.of()).forGetter(QualityModifiers::expert),
                AttributeModifier.CODEC.listOf().optionalFieldOf("perfect_modifiers", List.of()).forGetter(QualityModifiers::perfect),
                AttributeModifier.CODEC.listOf().optionalFieldOf("master_modifiers", List.of()).forGetter(QualityModifiers::master)
        ).apply(inst, QualityModifiers::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, QualityModifiers> STREAM_CODEC = StreamCodec.composite(
                AttributeModifier.STREAM_CODEC.apply(ByteBufCodecs.list()), QualityModifiers::poor,
                AttributeModifier.STREAM_CODEC.apply(ByteBufCodecs.list()), QualityModifiers::well,
                AttributeModifier.STREAM_CODEC.apply(ByteBufCodecs.list()), QualityModifiers::expert,
                AttributeModifier.STREAM_CODEC.apply(ByteBufCodecs.list()), QualityModifiers::perfect,
                AttributeModifier.STREAM_CODEC.apply(ByteBufCodecs.list()), QualityModifiers::master,
                QualityModifiers::new
        );
    }

    public static class Serializer implements RecipeSerializer<SewingRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        // Codec for SewingQuality enum
        private static final Codec<SewingQuality> SEWING_QUALITY_CODEC = Codec.STRING.xmap(
                SewingQuality::fromString,
                SewingQuality::getDisplayName
        );

        ///--------------------
        // Codec for Set<String> of blueprint types
        private static final Codec<Set<String>> CLOTH_PATTERN_TYPES_CODEC = Codec.either(
                Codec.STRING,
                Codec.STRING.listOf()
        ).xmap(
                either -> either.map(
                        s -> s.isBlank() ? Set.of() : Set.of(s.toLowerCase(Locale.ROOT)),
                        list -> list.stream()
                                .filter(s -> !s.isBlank())
                                .map(s -> s.toLowerCase(Locale.ROOT))
                                .collect(Collectors.toCollection(LinkedHashSet::new))
                ),
                set -> com.mojang.datafixers.util.Either.right(new ArrayList<>(set))
        );

        // StreamCodec for Set<String>
        private static final StreamCodec<RegistryFriendlyByteBuf, Set<String>> CLOTH_PATTERN_TYPES_STREAM_CODEC =
                new StreamCodec<>() {
                    @Override
                    public Set<String> decode(RegistryFriendlyByteBuf buffer) {
                        int count = buffer.readVarInt();
                        Set<String> set = new LinkedHashSet<>();
                        for (int i = 0; i < count; i++) {
                            set.add(buffer.readUtf());
                        }
                        return set;
                    }

                    @Override
                    public void encode(RegistryFriendlyByteBuf buffer, Set<String> value) {
                        buffer.writeVarInt(value.size());
                        for (String s : value) {
                            buffer.writeUtf(s);
                        }
                    }
                };
        // it seems like StreamCodecs need an encode and a decode function
        /// -------------------------------

        // StreamCodec for SewingQuality
        private static final StreamCodec<RegistryFriendlyByteBuf, SewingQuality> SEWING_QUALITY_STREAM_CODEC = new StreamCodec<>() {
            @Override
            public SewingQuality decode(RegistryFriendlyByteBuf buffer) {
                return SewingQuality.fromString(buffer.readUtf());
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buffer, SewingQuality value) {
                buffer.writeUtf(value.getDisplayName());
            }
        };

        // StreamCodec for NonNullList<Ingredient>
        private static final StreamCodec<RegistryFriendlyByteBuf, NonNullList<SewingIngredient>> INGREDIENTS_STREAM_CODEC = new StreamCodec<>() {
            @Override
            public NonNullList<SewingIngredient> decode(RegistryFriendlyByteBuf buffer) {
                int size = buffer.readVarInt();
                NonNullList<SewingIngredient> list = NonNullList.withSize(size, SewingIngredient.EMPTY);

                for (int i = 0; i < size; i++) {
                    list.set(i, SewingIngredient.STREAM_CODEC.decode(buffer));
                }

                return list;
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buffer, NonNullList<SewingIngredient> value) {
                buffer.writeVarInt(value.size());
                for (SewingIngredient ingredient : value) {
                    SewingIngredient.STREAM_CODEC.encode(buffer, ingredient);
                }
            }
        };


        @Override
        public MapCodec<SewingRecipe> codec() {
            return RecordCodecBuilder.mapCodec(instance -> instance.group(

                    /*
                    public SewingRecipe(List<String> pattern, Map<String, SewingIngredient> key,
                        NonNullList<SewingIngredient> ingredients,
                        ItemStack result, ItemStack failedResult,
                        int stitches, String tier,
                        boolean requiresClothPattern, Set<String> patternTypes,
                         boolean hasQuality, boolean needsMinigame,
                        boolean showNotification, SewingQuality minimumQuality,
                        SewingQuality qualityDifficulty, QualityModifiers qualityModifiers,
                        String group, int width, int height)
                     */

                    Codec.list(Codec.STRING).fieldOf("pattern").forGetter(SewingRecipe::getPattern),
                    Codec.unboundedMap(Codec.STRING, SewingIngredient.CODEC).fieldOf("key").forGetter(SewingRecipe::getKey),
                    //ingredients missing...
                    ItemStack.CODEC.fieldOf("result").forGetter(r -> r.result),
                    Codec.INT.fieldOf("stitches").forGetter(SewingRecipe::getStitchesRequired),
                    ItemStack.CODEC.optionalFieldOf("result_on_fail", ItemStack.EMPTY).forGetter(r -> r.failedResult),
                    Codec.STRING.optionalFieldOf("minimum_sewing_machine_tier", SewingMachineTier.WELL.getDisplayName()).forGetter(SewingRecipe::getSewingMachineTier),
                    Codec.BOOL.optionalFieldOf("requires_cloth_pattern", false).forGetter(SewingRecipe::requiresClothPattern),
                    CLOTH_PATTERN_TYPES_CODEC.optionalFieldOf("cloth_pattern", Set.of()).forGetter(SewingRecipe::getPatternTypes),
                    Codec.BOOL.optionalFieldOf("has_quality", false).forGetter(SewingRecipe::hasQuality),
                    Codec.BOOL.optionalFieldOf("needs_minigame", false).forGetter(SewingRecipe::needsMinigame),
                    Codec.BOOL.optionalFieldOf("show_notification", true).forGetter(SewingRecipe::showNotification),
                    SEWING_QUALITY_CODEC.optionalFieldOf("minimum_quality", SewingQuality.POOR).forGetter(SewingRecipe::getMinimumQuality),
                    SEWING_QUALITY_CODEC.optionalFieldOf("quality_difficulty", SewingQuality.NONE).forGetter(SewingRecipe::getQualityDifficulty),
                    QualityModifiers.CODEC.optionalFieldOf("quality_modifiers",
                            new QualityModifiers(List.of(), List.of(), List.of(), List.of(), List.of())).forGetter(r -> r.qualityModifiers),
                    Codec.STRING.optionalFieldOf("group", "").forGetter(SewingRecipe::getGroup)
            ).apply(instance, (pattern, key, result, stitches, failedResult, tier,
                               requiresBlueprint, blueprintTypes,
                                hasQuality, needsMinigame, showNotification,
                               minimumQuality, qualityDifficulty,
                               qualityMods, group)
        -> {
                // Parse the pattern using the key map
                if (pattern.isEmpty()) {
                    throw new IllegalArgumentException("Pattern cannot be empty");
                }
                int width = pattern.getFirst().length();
                int height = pattern.size();

                NonNullList<SewingIngredient> ingredients =
                        NonNullList.withSize(width * height, new SewingIngredient(Ingredient.EMPTY, false));

                for (int y = 0; y < height; y++) {
                    String row = pattern.get(y);
                    if (row.length() != width) {
                        throw new IllegalArgumentException("Pattern row width mismatch");
                    }

                    for (int x = 0; x < width; x++) {
                        char c = row.charAt(x);
                        String keyStr = String.valueOf(c);

                        SewingIngredient ingredient =
                                c == ' ' ? new SewingIngredient(Ingredient.EMPTY,  false)
                                        : key.getOrDefault(keyStr, new SewingIngredient(Ingredient.EMPTY, false));

                        ingredients.set(y * width + x, ingredient);
                    }
                }

                //ItemStack actualFailedResult = failedResult.isEmpty() ? ItemStack.EMPTY : failedResult;
                ItemStack actualFailedResult = failedResult.isEmpty() ? ItemStack.EMPTY : failedResult;

                return new SewingRecipe(new ArrayList<>(pattern), new LinkedHashMap<>(key),
                        ingredients, result, stitches, actualFailedResult,
                         tier,
                        requiresBlueprint, new LinkedHashSet<>(blueprintTypes),
                        hasQuality, needsMinigame,
                        showNotification, minimumQuality, qualityDifficulty, qualityMods, group, width, height);
            }));
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, SewingRecipe> streamCodec() {
            return new StreamCodec<>() {
                @Override
                public SewingRecipe decode(RegistryFriendlyByteBuf buffer) {

                        /*
                    public SewingRecipe(List<String> pattern, Map<String, SewingIngredient> key,
                        NonNullList<SewingIngredient> ingredients,
                        ItemStack result,
                        int stitches,
                        ItemStack failedResult,
                        String tier,
                        boolean requiresClothPattern, Set<String> patternTypes,
                         boolean hasQuality, boolean needsMinigame,
                        boolean showNotification, SewingQuality minimumQuality,
                        SewingQuality qualityDifficulty, QualityModifiers qualityModifiers,
                        String group, int width, int height)
                     */

                    NonNullList<SewingIngredient> ingredients = INGREDIENTS_STREAM_CODEC.decode(buffer);
                    ItemStack result = ItemStack.STREAM_CODEC.decode(buffer);
                    int hammering = buffer.readVarInt();
                    ItemStack failedResult = ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer);
                    String tier = buffer.readUtf();
                    boolean requiresBlueprint = buffer.readBoolean();
                    Set<String> blueprintTypes = CLOTH_PATTERN_TYPES_STREAM_CODEC.decode(buffer);
                    boolean hasQuality = buffer.readBoolean();
                    boolean needsMinigame = buffer.readBoolean();
                    boolean showNotification = buffer.readBoolean();
                    SewingQuality minimumQuality = SEWING_QUALITY_STREAM_CODEC.decode(buffer);
                    SewingQuality qualityDifficulty = SEWING_QUALITY_STREAM_CODEC.decode(buffer);
                    QualityModifiers qualityModifiers = QualityModifiers.STREAM_CODEC.decode(buffer);
                    String group = buffer.readUtf();

                    int width = buffer.readVarInt();
                    int height = buffer.readVarInt();


                    // Pattern and key are not synced over network - only ingredients are needed at runtime
                    return new SewingRecipe(List.of(), Map.of(), ingredients, result, hammering, failedResult,  tier,
                            requiresBlueprint, blueprintTypes,
                            hasQuality, needsMinigame,
                            showNotification, minimumQuality, qualityDifficulty, qualityModifiers, group, width, height);
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buffer, SewingRecipe recipe) {
                    /*
                    public SewingRecipe(List<String> pattern, Map<String, SewingIngredient> key,
                        NonNullList<SewingIngredient> ingredients,
                        ItemStack result, ItemStack failedResult,
                        int stitches, String tier,
                        boolean requiresClothPattern, Set<String> patternTypes,
                         boolean hasQuality, boolean needsMinigame,
                        boolean showNotification, SewingQuality minimumQuality,
                        SewingQuality qualityDifficulty, QualityModifiers qualityModifiers,
                        String group, int width, int height)
                     */

                    INGREDIENTS_STREAM_CODEC.encode(buffer, recipe.ingredients);
                    ItemStack.STREAM_CODEC.encode(buffer, recipe.result);
                    buffer.writeVarInt(recipe.stitches);
                    ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, recipe.failedResult);
                    buffer.writeUtf(recipe.tier);
                    buffer.writeBoolean(recipe.requiresClothPattern);
                    CLOTH_PATTERN_TYPES_STREAM_CODEC.encode(buffer, recipe.patternTypes);
                    buffer.writeBoolean(recipe.hasQuality);
                    buffer.writeBoolean(recipe.needsMinigame);
                    buffer.writeBoolean(recipe.showNotification);
                    SEWING_QUALITY_STREAM_CODEC.encode(buffer, recipe.minimumQuality);
                    SEWING_QUALITY_STREAM_CODEC.encode(buffer, recipe.qualityDifficulty);
                    QualityModifiers.STREAM_CODEC.encode(buffer, recipe.qualityModifiers);
                    buffer.writeUtf(recipe.group);
                    buffer.writeVarInt(recipe.width);
                    buffer.writeVarInt(recipe.height);

                }
            };
        }
    }

}