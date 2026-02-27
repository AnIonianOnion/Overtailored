package com.anionianonion.overtailored.block.entity;

import com.anionianonion.overtailored.PatternQuality;
import com.anionianonion.overtailored.OvertailoredMod;
import com.anionianonion.overtailored.SewingQuality;
import com.anionianonion.overtailored.advancement.ModAdvancementTriggers;
import com.anionianonion.overtailored.block.custom.AbstractSewingMachineBlock;
import com.anionianonion.overtailored.components.PatternData;
import com.anionianonion.overtailored.components.ModComponents;
import com.anionianonion.overtailored.config.ServerConfig;
import com.anionianonion.overtailored.events.ModEvents;
import com.anionianonion.overtailored.recipe.SewingRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;
import com.anionianonion.overtailored.SewingMachineTier;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;
import org.jetbrains.annotations.Nullable;

import java.util.*;


//copied from https://github.com/phuccom000/Overgeared/blob/1.21.1/src/main/java/net/stirdrem/overgeared/block/entity/AbstractSewingMachineBlockEntity.java
public abstract class AbstractSewingMachineBlockEntity extends BlockEntity implements MenuProvider, WorldlyContainer {

    protected static final int INPUT_SLOT = 0;
    protected static final int OUTPUT_SLOT = 10;
    protected final ItemStackHandler itemHandler = new ItemStackHandler(12) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level == null || level.isClientSide()) return;
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    };
    protected final ContainerData data;
    protected int progress;
    protected int maxProgress;
    protected int hitRemains;
    protected long busyUntilGameTime = 0L;
    protected UUID ownerUUID = null;
    protected Map<BlockPos, UUID> occupiedAnvils = Collections.synchronizedMap(new HashMap<>());
    protected SewingMachineTier sewingMachineTier;
    protected long sessionStartTime = 0L; // optional, for timeout logic
    protected ItemStack failedResult;
    protected Player player;
    protected SewingRecipe lastRecipe = null;
    protected ItemStack lastBlueprint = ItemStack.EMPTY;
    private boolean minigameOn = false;
    protected AbstractSewingMachineBlock sewingMachine;
    protected static final int BLUEPRINT_SLOT = 11;
    // Define slot indices
    private static final int[] TOP_SLOTS = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8}; // input grid
    private static final int[] BOTTOM_SLOTS = new int[]{OUTPUT_SLOT};
    private static final int[] SIDE_SLOTS = new int[0]; // nothing on sides

    public AbstractSewingMachineBlockEntity(AbstractSewingMachineBlock sewingMachine, SewingMachineTier tier, BlockEntityType<?> type, BlockPos pPos, BlockState pBlockState) {
        super(type, pPos, pBlockState);
        this.sewingMachineTier = tier;
        this.sewingMachine = sewingMachine;
        this.data = new ContainerData() {
            @Override
            public int get(int pIndex) {
                return switch (pIndex) {
                    case 0 -> AbstractSewingMachineBlockEntity.this.progress;
                    case 1 -> AbstractSewingMachineBlockEntity.this.maxProgress;
                    case 2 -> AbstractSewingMachineBlockEntity.this.hitRemains;
                    default -> 0;
                };
            }

            @Override
            public void set(int pIndex, int pValue) {
                switch (pIndex) {
                    case 0 -> AbstractSewingMachineBlockEntity.this.progress = pValue;
                    case 1 -> AbstractSewingMachineBlockEntity.this.maxProgress = pValue;
                }
            }

            @Override
            public int getCount() {
                return 3;
            }
        };
    }

    public static void applySewingQuality(ItemStack stack, SewingQuality quality) {
        stack.set(ModComponents.SEWING_QUALITY.get(), quality);
    }

    public ItemStack getRenderStack(int index) {
        return itemHandler.getStackInSlot(index);
    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("gui.overtailored.any_sewing_station");
    }


    @Override
    public int[] getSlotsForFace(Direction side) {
        return switch (side) {
            case UP -> TOP_SLOTS;
            case DOWN -> BOTTOM_SLOTS;
            default -> SIDE_SLOTS;
        };
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack itemStack, @Nullable Direction side) {
        // Only allow insertion from the top into input grid
        if (side == Direction.UP) {
            // Input slots should be indices 0–8
            return index >= 0 && index < 9;
        }
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction side) {
        // Only allow extraction from bottom from the output slot
        return side == Direction.DOWN && index == OUTPUT_SLOT;
    }


    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("hitRemains", hitRemains);
        tag.putInt("progress", progress);
        tag.putInt("maxProgress", maxProgress);
        tag.put("inventory", itemHandler.serializeNBT(registries));

        if (ownerUUID != null) {
            tag.putUUID("ownerUUID", ownerUUID);
            tag.putLong("sessionStartTime", sessionStartTime);
        }

        // Save occupiedAnvils
        ListTag occupiedList = new ListTag();
        for (Map.Entry<BlockPos, UUID> entry : occupiedAnvils.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            BlockPos pos = entry.getKey();
            entryTag.putInt("x", pos.getX());
            entryTag.putInt("y", pos.getY());
            entryTag.putInt("z", pos.getZ());
            entryTag.putUUID("uuid", entry.getValue());
            occupiedList.add(entryTag);
        }
        tag.put("occupiedAnvils", occupiedList);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("inventory")) {
            itemHandler.deserializeNBT(registries, tag.getCompound("inventory"));
        }
        if (tag.contains("hitRemains")) {
            hitRemains = tag.getInt("hitRemains");
        }
        if (tag.contains("progress")) {
            progress = tag.getInt("progress");
        }
        if (tag.contains("maxProgress")) {
            maxProgress = tag.getInt("maxProgress");
        }
        if (tag.hasUUID("ownerUUID")) {
            ownerUUID = tag.getUUID("ownerUUID");
            sessionStartTime = tag.getLong("sessionStartTime");
        } else {
            ownerUUID = null;
        }

        // Load occupiedAnvils
        occupiedAnvils.clear();
        if (tag.contains("occupiedAnvils", CompoundTag.TAG_LIST)) {
            ListTag occupiedList = tag.getList("occupiedAnvils", CompoundTag.TAG_COMPOUND);
            for (int i = 0; i < occupiedList.size(); i++) {
                CompoundTag entryTag = occupiedList.getCompound(i);
                int x = entryTag.getInt("x");
                int y = entryTag.getInt("y");
                int z = entryTag.getInt("z");
                UUID uuid = entryTag.getUUID("uuid");
                occupiedAnvils.put(new BlockPos(x, y, z), uuid);
            }
        }
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void increaseForgingProgress(Level pLevel, BlockPos pPos, BlockState pState) {
        Optional<SewingRecipe> recipe = getCurrentRecipe();
        if (hasRecipe()) {
            SewingRecipe currentRecipe = recipe.get();
            maxProgress = currentRecipe.getStitchesRequired();
            increaseCraftingProgress();
            setChanged(pLevel, pPos, pState);

            if (hasProgressFinished()) {
                craftItem();
                resetProgress(pPos);
            }
        } else {
            resetProgress(pPos);
        }
    }

    public void resetProgress(BlockPos pos) {
        progress = 0;
        maxProgress = 0;
        lastRecipe = null;
        if (level != null && !level.isClientSide()) {
            ModEvents.resetMinigameForPlayer((ServerPlayer) player);
        }
        player = null;
    }

    //todo
    protected void craftItem() {
        Optional<SewingRecipe> recipeOptional = getCurrentRecipe();
        if (recipeOptional.isEmpty()) return;

        SewingRecipe recipe = recipeOptional.get();
        ItemStack result = recipe.getResultItem(getLevel().registryAccess());
        failedResult = recipe.getFailedResultItem(getLevel().registryAccess());
        SewingQuality minimumQuality = recipe.getMinimumQuality();
        // Only set quality if recipe supports it

        SewingQuality maxIngredientQuality = null;
        for (int i = 0; i < 9; i++) {
            ItemStack ingredient = itemHandler.getStackInSlot(i);
            SewingQuality quality = ingredient.get(ModComponents.SEWING_QUALITY);
            if (quality != null && (maxIngredientQuality == null || quality.ordinal() > maxIngredientQuality.ordinal())) {
                maxIngredientQuality = quality;
            }
        }

        if (recipe.hasQuality() && player != null && ServerConfig.PLAYER_AUTHOR_TOOLTIPS.get()) {
            result.set(ModComponents.CREATOR, player.getName().getString());
        }

        if (recipe.hasQuality()) {
            if (ServerConfig.ENABLE_MINIGAME.get()) {
                SewingQuality quality = determineForgingQuality();
                if (!Objects.equals(quality, SewingQuality.NONE)) {
                    if (quality != null) {
                        // Clamp to minimum quality if needed
                        if (minimumQuality != null && quality.ordinal() < minimumQuality.ordinal()) {
                            quality = minimumQuality;
                        }
                        // Clamp to maximum quality
                        if (maxIngredientQuality != null && quality.ordinal() > maxIngredientQuality.ordinal() && ServerConfig.INGREDIENTS_DEFINE_MAX_QUALITY.get()) {
                            quality = maxIngredientQuality;
                        }

                        if (quality == SewingQuality.PERFECT) {
                            if (ServerConfig.MASTER_QUALITY_CHANCE.get() != 0 &&
                                    new Random().nextFloat() < ServerConfig.MASTER_QUALITY_CHANCE.get()) {
                                quality = SewingQuality.MASTER;
                            }
                        }
                        result.set(ModComponents.SEWING_QUALITY.get(), quality);
                        //todo: set quality based attribute modifiers here
                        var mods = recipe.getModifiersFor(quality);
                        if (!mods.isEmpty())
                        {
                            result.set(ModComponents.QUALITY_MODIFIERS.get(), mods);
                        }

                        if (player instanceof ServerPlayer serverPlayer) {
                            ModAdvancementTriggers.SEWING_QUALITY.get()
                                    .trigger(serverPlayer, quality.getDisplayName());
                        }
                    }
                }
            }
        } else if (recipe.needsMinigame()) {
            // Handle minigame result without quality
            if (ServerConfig.ENABLE_MINIGAME.get()) {
                SewingQuality quality = determineForgingQuality();
                if (!Objects.equals(quality, SewingQuality.NONE)) {
                    boolean fail = false;

                    if (quality.equals(SewingQuality.POOR)) {
                        fail = true;
                    } else if (quality.equals(SewingQuality.WELL)) {
                        float failChance = ServerConfig.FAIL_ON_WELL_QUALITY_CHANCE.get().floatValue();
                        fail = new Random().nextFloat() < failChance;
                    } else if (quality.equals(SewingQuality.EXPERT)) {
                        float failChance = ServerConfig.FAIL_ON_EXPERT_QUALITY_CHANCE.get().floatValue();
                        fail = new Random().nextFloat() < failChance;
                    }

                    if (fail) {
                        result = failedResult.copy();
                    }
                }
            }
        }

        // Extract ingredients
        for (int i = 0; i < 9; i++) {
            this.itemHandler.extractItem(i, 1, false);
        }

        ItemStack existing = this.itemHandler.getStackInSlot(OUTPUT_SLOT);

        if (existing.isEmpty()) {
            // If the output slot is empty, just set the result
            this.itemHandler.setStackInSlot(OUTPUT_SLOT, result);
        } else if (ItemStack.isSameItemSameComponents(existing, result)) {
            // If the same item (with same NBT), try to stack them
            int total = existing.getCount() + result.getCount();
            int maxSize = Math.min(existing.getMaxStackSize(), this.itemHandler.getSlotLimit(OUTPUT_SLOT));

            if (total <= maxSize) {
                existing.grow(result.getCount());
                this.itemHandler.setStackInSlot(OUTPUT_SLOT, existing);
            } else {
                // If not all items fit, grow to max and optionally handle overflow
                int remainder = total - maxSize;
                existing.setCount(maxSize);
                this.itemHandler.setStackInSlot(OUTPUT_SLOT, existing);

                // Handle remainder if needed (e.g. drop, store elsewhere, etc.)
                ItemStack overflow = result.copy();
                overflow.setCount(remainder);
                // Example: drop the overflow into the world
                Containers.dropItemStack(getLevel(), getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ(), overflow);
            }
        }
    }

    protected void craftItemWithBlueprint() {

        // Get the crafted output item
        ItemStack result = this.itemHandler.getStackInSlot(OUTPUT_SLOT);

        // Skip blueprint progression if crafting failed
        if (result.isEmpty()) return;

        // Handle blueprint progression (slot 11)
        ItemStack blueprint = this.itemHandler.getStackInSlot(BLUEPRINT_SLOT);
        PatternData patternData = blueprint.get(ModComponents.PATTERN_DATA);
        if (blueprint.isEmpty() || patternData == null) return;
        String currentQualityStr = patternData.quality();
        int uses = patternData.uses();
        int usesToLevel = patternData.usesToLevel();

        PatternQuality currentQuality = PatternQuality.fromString(currentQualityStr);

        // Attempt to read the SewingQuality from result
        SewingQuality resultQuality = sewingMachine.getQuality();

        if (currentQuality == null
                || currentQuality == PatternQuality.PERFECT
                || currentQuality == PatternQuality.MASTER) return;
        if (!ServerConfig.EXPERT_ABOVE_INCREASE_BLUEPRINT.get() || resultQuality.ordinal() >= SewingQuality.EXPERT.ordinal()) {
            uses += switch (resultQuality) {
                case PERFECT -> 2;
                case MASTER -> 3;
                default -> 1;
            };
        }

        // Level up if threshold reached
        if (uses >= usesToLevel) {
            PatternQuality nextQuality = PatternQuality.getNext(currentQuality);
            if (nextQuality != null) {
                PatternData newData = patternData
                        .withQuality(nextQuality.getDisplayName())
                        .withUses(0)
                        .withUsesToLevel(nextQuality.getUse());
                blueprint.set(ModComponents.PATTERN_DATA, newData);
                if (player instanceof ServerPlayer serverPlayer) {
                    if (nextQuality.equals(PatternQuality.PERFECT) || nextQuality.equals(PatternQuality.MASTER))
                        ModAdvancementTriggers.MAX_LEVEL_PATTERN.get().trigger(serverPlayer);
                    ModAdvancementTriggers.PATTERN_QUALITY.get().trigger(serverPlayer, nextQuality.getDisplayName());
                }
            } else {
                blueprint.set(ModComponents.PATTERN_DATA, patternData.withUses(usesToLevel)); // Clamp
            }
        } else {
            blueprint.set(ModComponents.PATTERN_DATA, patternData.withUses(uses)); // Just increment
        }

        this.itemHandler.setStackInSlot(BLUEPRINT_SLOT, blueprint);
    }

    public boolean isFailedResult() {
        ItemStack result = this.itemHandler.getStackInSlot(OUTPUT_SLOT);

        return ItemStack.isSameItem(result, failedResult);
    }

    public boolean hasRecipe() {
        Optional<SewingRecipe> recipeOptional = getCurrentRecipe();
        if (recipeOptional.isEmpty()) return false;

        SewingRecipe recipe = recipeOptional.get();
        SewingMachineTier requiredTier = SewingMachineTier.fromDisplayName(recipe.getSewingMachineTier());

        // Safely skip if tier is invalid
        if (requiredTier == null || !requiredTier.isEqualOrLowerThan(this.sewingMachineTier)) {
            return false;
        }

        ItemStack resultStack = recipe.getResultItem(level.registryAccess());
        return canInsertItemIntoOutputSlot(resultStack)
                && canInsertAmountIntoOutputSlot(resultStack.getCount());
    }

    public boolean hasRecipeWithBlueprint() {
        Optional<SewingRecipe> recipeOptional = getCurrentRecipe();
        if (recipeOptional.isEmpty()) return false;

        SewingRecipe recipe = recipeOptional.get();

        // Tier check
        SewingMachineTier requiredTier = SewingMachineTier.fromDisplayName(recipe.getSewingMachineTier());
        if (requiredTier == null || !requiredTier.isEqualOrLowerThan(this.sewingMachineTier)) {
            return false;
        }

        ItemStack blueprint = this.itemHandler.getStackInSlot(BLUEPRINT_SLOT);
        PatternData patternData = blueprint.get(ModComponents.PATTERN_DATA);

        if (recipe.requiresClothPattern()) {
            // Must have a valid matching blueprint
            if (blueprint.isEmpty() || patternData == null) {
                return false;
            }

            String blueprintToolType = patternData.armorType().toLowerCase(Locale.ROOT);
            if (!recipe.getPatternTypes().contains(blueprintToolType)) {
                return false;
            }
        } else {
            // Optional blueprint: if present, it must match
            if (!blueprint.isEmpty() && patternData != null) {
                String blueprintToolType = patternData.armorType().toLowerCase(Locale.ROOT);
                if (!recipe.getPatternTypes().contains(blueprintToolType)) {
                    return false;
                }
            }
        }

        ItemStack resultStack = recipe.getResultItem(level.registryAccess());
        return canInsertItemIntoOutputSlot(resultStack)
                && canInsertAmountIntoOutputSlot(resultStack.getCount());
    }

    public Optional<SewingRecipe> getCurrentRecipe() {
        return getCurrentRecipeHolder().map(RecipeHolder::value);
    }

    public Optional<RecipeHolder<SewingRecipe>> getCurrentRecipeHolder() {
        // Create a wrapper that only exposes the slots needed for recipe matching
        ItemStackHandler recipeHandler = new ItemStackHandler(12);
        for (int i = 0; i < 9; i++) {
            recipeHandler.setStackInSlot(i, itemHandler.getStackInSlot(i));
        }
        recipeHandler.setStackInSlot(11, itemHandler.getStackInSlot(11));

        RecipeWrapper recipeInput = new RecipeWrapper(recipeHandler);


        //todo: Polymorph
        /*
        // Try to use Polymorph's selected recipe if available
        Optional<RecipeHolder<SewingRecipe>> polymorphRecipe = Polymorph.getSelectedRecipe(this, recipeInput);
        if (polymorphRecipe.isPresent()) {
            return polymorphRecipe.filter(holder -> matchesRecipeExactly(holder.value()));
        }
        */

        // Fallback to best match when Polymorph is not available
        return SewingRecipe.findBestMatchHolder(level, recipeInput)
                .filter(holder -> matchesRecipeExactly(holder.value()));
    }


    protected boolean canInsertItemIntoOutputSlot(ItemStack stackToInsert) {
        ItemStack existing = this.itemHandler.getStackInSlot(OUTPUT_SLOT);
        return (existing.isEmpty() || ItemStack.isSameItemSameComponents(existing, stackToInsert));
    }

    protected boolean canInsertAmountIntoOutputSlot(int count) {
        return this.itemHandler.getStackInSlot(OUTPUT_SLOT).getCount() + count <= this.itemHandler.getStackInSlot(OUTPUT_SLOT).getMaxStackSize();
    }

    public boolean hasProgressFinished() {
        return progress >= maxProgress;
    }

    public void increaseCraftingProgress() {
        progress++;

        setChanged();

        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }

        if (data != null) {
            data.set(0, progress);
            data.set(1, maxProgress);
            data.set(2, hitRemains);
        }
    }

    public boolean isBusy(long currentGameTime) {
        return currentGameTime < busyUntilGameTime;
    }

    public void setBusyUntil(long time) {
        this.busyUntilGameTime = time;
        setChanged(level, worldPosition, getBlockState());
    }

    public void tick(Level lvl, BlockPos pos, BlockState st) {
        if (!pos.equals(this.worldPosition)) return; // sanity check
        try {
            // Check if blueprint changed mid-forging
            ItemStack currentBlueprint = this.itemHandler.getStackInSlot(11);
            if (!ItemStack.isSameItemSameComponents(currentBlueprint, lastBlueprint)) {
                if (progress > 0 || lastRecipe != null || isMinigameOn()) {
                    resetProgress(pos);
                    setMinigameOn(false);
                    OvertailoredMod.LOGGER.debug("Blueprint changed at {}, minigame reset", pos);
                }
            }
            lastBlueprint = currentBlueprint.copy();

            Optional<SewingRecipe> currentRecipeOpt = getCurrentRecipe();
            if (currentRecipeOpt.isEmpty()) {
                if (progress > 0 || lastRecipe != null) {
                    resetProgress(pos);
                }
                return;
            }

            SewingRecipe currentRecipe = currentRecipeOpt.get();

            boolean recipeChanged = false;
            if (lastRecipe != null) {
                // Compare recipes by their result items since Recipe.getId() is not available in 1.21+
                // Recipes are wrapped in RecipeHolder, but we're working with the raw recipe here
                ItemStack currentResult = currentRecipe.getResultItem(level.registryAccess());
                ItemStack lastResult = lastRecipe.getResultItem(level.registryAccess());
                recipeChanged = !ItemStack.isSameItemSameComponents(currentResult, lastResult);
            } else if (maxProgress > 0) {
                recipeChanged = true;
            }

            if (recipeChanged) {
                resetProgress(pos);
                lastRecipe = currentRecipe;
                return;
            }

            lastRecipe = currentRecipe;

            if (hasRecipe()) {
                maxProgress = currentRecipe.getStitchesRequired();
                hitRemains = maxProgress - progress;
                setChanged(lvl, pos, st);

                if (hasProgressFinished()) {
                    craftItem();
                    resetProgress(pos);
                }
            } else {
                if (progress > 0 || maxProgress > 0) {
                    resetProgress(pos);
                }
            }
        } catch (Exception e) {
            OvertailoredMod.LOGGER.error("Error ticking smithing anvil at {}", pos, e);
            resetProgress(pos);
        }
    }

    public int getHitsRemaining() {
        return hitRemains;
    }

    // Add this method to ensure data sync
    public ContainerData getContainerData() {
        return data;
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.put("inventory", itemHandler.serializeNBT(registries));
        tag.putInt("progress", progress);
        tag.putInt("maxProgress", maxProgress);
        tag.putInt("hitRemains", hitRemains);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        super.handleUpdateTag(tag, registries);
        if (tag.contains("progress")) {
            this.progress = tag.getInt("progress");
        }
        if (tag.contains("maxProgress")) {
            this.maxProgress = tag.getInt("maxProgress");
        }
        if (tag.contains("hitRemains")) {
            this.hitRemains = tag.getInt("hitRemains");
        }
    }

    protected boolean matchesRecipeExactly(SewingRecipe recipe) {
        // Create a wrapper for recipe matching
        ItemStackHandler recipeHandler = new ItemStackHandler(12);
        // Copy items from input slots (0-8) to our 3x3 grid
        for (int i = 0; i < 9; i++) {
            recipeHandler.setStackInSlot(i, this.itemHandler.getStackInSlot(i));
        }
        recipeHandler.setStackInSlot(11, this.itemHandler.getStackInSlot(11));
        RecipeWrapper recipeInput = new RecipeWrapper(recipeHandler);
        return recipe.matches(recipeInput, level);
    }

    protected SewingQuality determineForgingQuality() {
        SewingQuality quality = sewingMachine.getQuality();
        if (quality == null) return SewingQuality.WELL;
        Optional<SewingRecipe> recipeOptional = getCurrentRecipe();
        SewingRecipe recipe = recipeOptional.get();
        if (!recipe.getPatternTypes().isEmpty()) {

            ItemStack blueprint = this.itemHandler.getStackInSlot(BLUEPRINT_SLOT);
            PatternData patternData = blueprint.get(ModComponents.PATTERN_DATA);

            // Define tool quality tiers in order of strength
            List<String> qualityTiers = List.of("poor", "well", "expert", "perfect", "master");

            // If blueprint is missing or invalid, fallback logic
            if (blueprint.isEmpty() || patternData == null) {
                return switch (quality) {
                    case SewingQuality.POOR -> SewingQuality.POOR;
                    default -> SewingQuality.WELL; // Cap quality at 'well' without blueprint
                };
            }

            String blueprintQualityStr = patternData.quality().toLowerCase();

            // Determine capped quality
            int sewingMachineTierIndex = qualityTiers.indexOf(quality.getDisplayName().toLowerCase());
            int blueprintTierIndex = qualityTiers.indexOf(blueprintQualityStr);

            // Default to lowest if any tier is missing
            if (sewingMachineTierIndex == -1 || blueprintTierIndex == -1) {
                return SewingQuality.NONE;
            }

            int finalIndex = Math.min(sewingMachineTierIndex, blueprintTierIndex);

            switch (qualityTiers.get(finalIndex)) {
                case "poor":
                    return SewingQuality.POOR;
                case "expert":
                    return SewingQuality.EXPERT;
                case "perfect": {
                    Random random = new Random();

                    // Check if any crafting slot contains a Master-quality ingredient
                    boolean hasMasterIngredient = false;
                    for (int i = 0; i < this.itemHandler.getSlots(); i++) {
                        if (i == OUTPUT_SLOT || i == BLUEPRINT_SLOT) continue; // skip output + blueprint
                        ItemStack stack = this.itemHandler.getStackInSlot(i);
                        SewingQuality ingQuality = stack.get(ModComponents.SEWING_QUALITY);
                        if (!stack.isEmpty() && ingQuality == SewingQuality.MASTER) {
                            hasMasterIngredient = true;
                            break;
                        }
                    }

                    // Normal Master roll from config
                    boolean masterRoll = ServerConfig.MASTER_QUALITY_CHANCE.get() != 0
                            && random.nextFloat() < ServerConfig.MASTER_QUALITY_CHANCE.get();

                    // Ingredient-based boost
                    boolean ingredientMasterRoll = hasMasterIngredient
                            && random.nextFloat() < ServerConfig.MASTER_FROM_INGREDIENT_CHANCE.get();

                    if ("master".equals(blueprintQualityStr) || masterRoll || ingredientMasterRoll) {
                        return SewingQuality.MASTER;
                    } else {
                        return SewingQuality.PERFECT;
                    }
                }
                case "master":
                    return SewingQuality.MASTER;
                default:
                    return SewingQuality.WELL;
            }
        }
        return quality;
    }

    public SewingQuality minigameQuality() {
        Optional<SewingRecipe> recipeOptional = getCurrentRecipe();
        if (recipeOptional.isEmpty()) {
            return SewingQuality.NONE; // no recipe = base fallback
        }

        SewingRecipe recipe = recipeOptional.get();
        if (!recipe.getPatternTypes().isEmpty()) {
            if (!recipe.getQualityDifficulty().equals(SewingQuality.NONE))
                return recipe.getQualityDifficulty();
            else return qualityFromBlueprint();
        } else return recipe.getQualityDifficulty();
    }

    public SewingQuality qualityFromBlueprint() {
        SewingQuality quality = AbstractSewingMachineBlock.getQuality();
        if (quality == null) {
            return SewingQuality.NONE; // fallback when global quality is missing
        }

        Optional<SewingRecipe> recipeOptional = getCurrentRecipe();
        if (recipeOptional.isEmpty()) {
            return SewingQuality.POOR; // no recipe = base fallback
        }

        SewingRecipe recipe = recipeOptional.get();
        if (!recipe.getPatternTypes().isEmpty()) {
            if (!recipe.getQualityDifficulty().equals(SewingQuality.NONE))
                return recipe.getQualityDifficulty();
            ItemStack blueprint = this.itemHandler.getStackInSlot(BLUEPRINT_SLOT);

            // Quality tiers in order
            List<SewingQuality> qualityTiers = List.of(SewingQuality.POOR, SewingQuality.WELL, SewingQuality.EXPERT, SewingQuality.PERFECT, SewingQuality.MASTER);

            // Missing or invalid blueprint → cap quality
            String poor = quality.equals(SewingQuality.POOR)
                    ? SewingQuality.POOR.getDisplayName()
                    : SewingQuality.NONE.getDisplayName();
            PatternData patternData = blueprint.get(ModComponents.PATTERN_DATA);
            if (blueprint.isEmpty() || patternData == null) {
                return SewingQuality.POOR;
            }

            String bpQuality = patternData.quality().toLowerCase();
            // ensure it’s in our tier list, otherwise default
            return qualityTiers.contains(SewingQuality.fromString(bpQuality)) ? SewingQuality.fromString(bpQuality) : SewingQuality.NONE;
        }

        return SewingQuality.NONE; // fallback if no blueprint types
    }

    public void setProgress(int progress) {
        this.progress = progress;
        this.setChanged();

        // Force sync to client
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }

        if (this.data != null) {
            this.data.set(0, progress);
        }
    }

    public int getRequiredProgress() {
        return getCurrentRecipe()
                .map(SewingRecipe::getStitchesRequired)
                .orElse(0); // default to 0 if recipe is empty
    }

    public int getProgress() {
        if (level != null && level.isClientSide() && data != null) {
            // On client, get from synced container data
            return data.get(0);
        }
        return this.progress;
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
    }

    public void setOwner(UUID uuid) {
        ownerUUID = uuid;
        sessionStartTime = level.getGameTime();
        setChanged();
    }

    public void clearOwner() {
        ownerUUID = null;
        sessionStartTime = 0L;
        setChanged();
    }

    public boolean isOwnedBy(Player player) {
        return ownerUUID != null && ownerUUID.equals(player.getUUID());
    }

    public boolean isOwned() {
        return ownerUUID != null;
    }

    public UUID getOccupiedAnvil(BlockPos pos) {
        return occupiedAnvils.get(pos);
    }

    public void putOccupiedAnvil(BlockPos pos, UUID me) {
        occupiedAnvils.put(pos, me);
    }

    public boolean hasQuality() {
        Optional<SewingRecipe> recipeOptional = getCurrentRecipe();
        if (recipeOptional.isEmpty()) return false;

        SewingRecipe recipe = recipeOptional.get();

        // Only set quality if recipe supports it
        return recipe.hasQuality();
    }

    public boolean needsMinigame() {
        Optional<SewingRecipe> recipeOptional = getCurrentRecipe();
        if (recipeOptional.isEmpty()) return false;

        SewingRecipe recipe = recipeOptional.get();

        // Only set quality if recipe supports it
        return !recipe.hasQuality() && recipe.needsMinigame();
    }

    public IItemHandlerModifiable getItemHandler() {
        return itemHandler;
    }

    public BlockPos getPos(ServerPlayer serverPlayer) {
        UUID playerUUID = serverPlayer.getUUID();
        for (Map.Entry<BlockPos, UUID> entry : occupiedAnvils.entrySet()) {
            if (entry.getValue().equals(playerUUID)) {
                return entry.getKey();
            }
        }
        return null; // Not found
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public boolean isMinigameOn() {
        return minigameOn;
    }

    public void setMinigameOn(boolean value) {
        this.minigameOn = value;
        setChanged(); // mark dirty for save
    }

    @Override
    public int getContainerSize() {
        return this.itemHandler.getSlots();
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            ItemStack stack = itemHandler.getStackInSlot(i);
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return itemHandler.getStackInSlot(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return itemHandler.extractItem(slot, amount, false);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        // Extract the entire stack regardless of amount
        ItemStack stack = itemHandler.getStackInSlot(slot).copy();
        itemHandler.setStackInSlot(slot, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        itemHandler.setStackInSlot(slot, stack);
    }

    @Override
    public boolean stillValid(Player player) {
        // Standard check: player must be no farther than 8 blocks
        if (this.level == null) return false;
        return player.distanceToSqr(
                this.worldPosition.getX() + 0.5,
                this.worldPosition.getY() + 0.5,
                this.worldPosition.getZ() + 0.5
        ) <= 64.0;
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            itemHandler.setStackInSlot(i, ItemStack.EMPTY);
        }
    }
}
