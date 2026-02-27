package com.anionianonion.overtailored.screens;

import com.anionianonion.overtailored.block.entity.AbstractSewingMachineBlockEntity;
import com.anionianonion.overtailored.item.ModItems;
import com.anionianonion.overtailored.recipe.ModRecipeTypes;
import com.anionianonion.overtailored.recipe.SewingRecipe;
import com.anionianonion.overtailored.utils.ModTags;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

//copy of https://github.com/phuccom000/Overgeared/blob/1.21.1/src/main/java/net/stirdrem/overgeared/screen/AbstractSmithingAnvilMenu.java
public class AbstractSewingMachineMenu extends AbstractContainerMenu {
    private final Container container = new SimpleContainer();
    public final AbstractSewingMachineBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;
    private final ResultContainer resultContainer = new ResultContainer();
    private Slot resultSlot;
    private final Player player;
    private final boolean hasBlueprint;
    private final List<Integer> craftingSlotIndices = new ArrayList<>();

    public AbstractSewingMachineMenu(MenuType<?> pMenuType, int pContainerId, Inventory inv, AbstractSewingMachineBlockEntity entity, ContainerData data, boolean hasBlueprint) {
        super(pMenuType, pContainerId);
        checkContainerSize(inv, 12);
        blockEntity = entity;
        this.level = inv.player.level();
        this.data = data;
        this.player = inv.player;
        addPlayerInventory(inv);
        addPlayerHotbar(inv);
        this.hasBlueprint = hasBlueprint;

        IItemHandler iItemHandler = this.blockEntity.getItemHandler();
        
        this.addSlot(new SlotItemHandler(iItemHandler, 9, 152, 61) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ModTags.Items.SEWING_UTENSILS);
            }

            @Override
            public boolean mayPickup(Player player) {
                return true; // This is crucial for JEI transfers
            }
        }); //hammer

        if (hasBlueprint)
            this.addSlot(new SlotItemHandler(iItemHandler, 11, 111, 53) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return stack.is(ModItems.CLOTHES_PATTERN.get()) || stack.getItem() instanceof SmithingTemplateItem;
                }

                @Override
                public boolean mayPickup(Player player) {
                    return true; // This is crucial for JEI transfers
                }

                @Override
                public int getMaxStackSize() {
                    return 1;
                }

                @Override
                public int getMaxStackSize(ItemStack stack) {
                    return 1;
                }

                @Override
                public void set(ItemStack stack) {
                    // Always limit to a single item
                    super.set(stack.copyWithCount(1));
                }
            });

        //crafting slot
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                Slot slotIndex = this.addSlot(new SlotItemHandler(iItemHandler, j + i * 3, 30 + j * 18, 17 + i * 18) {
                    @Override
                    public boolean mayPickup(Player player) {
                        return true; // This is crucial for JEI transfers
                    }
                });
                craftingSlotIndices.add(slotIndex.index); // Store the index, not the Slot object
            }
        }

        //output slot
        this.resultSlot = new SlotItemHandler(iItemHandler, 10, 124, 35) {
            private int removeCount;

            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public boolean mayPickup(Player player) {
                return true; // This is crucial for JEI transfers
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                this.checkTakeAchievements(stack);
                
                // Create a RecipeInput wrapper for the block entity's item handler
                IItemHandler handler = AbstractSewingMachineMenu.this.blockEntity.getItemHandler();
                ItemStackHandler tempHandler = new ItemStackHandler(handler.getSlots());
                for (int i = 0; i < handler.getSlots(); i++) {
                    tempHandler.setStackInSlot(i, handler.getStackInSlot(i));
                }
                RecipeInput recipeInput = new RecipeWrapper(tempHandler);
                
                NonNullList<ItemStack> remainders = player.level()
                        .getRecipeManager().getRemainingItemsFor(ModRecipeTypes.SEWING.get(), recipeInput, player.level());
                
                // Only process remainders if there are items to process
                // Use the handler slots 0-8 for crafting (not the empty container)
                for (int i = 0; i < Math.min(remainders.size(), 9); ++i) {
                    ItemStack inputStack = handler.getStackInSlot(i);
                    ItemStack remainder = remainders.get(i);

                    // 1. Consume exactly one item from the input slot
                    if (!inputStack.isEmpty()) {
                        handler.extractItem(i, 1, false);
                    }

                    // 2. Handle the remainder item
                    if (!remainder.isEmpty()) {
                        ItemStack current = handler.getStackInSlot(i);

                        if (current.isEmpty()) {
                            // Slot is empty → put the remainder directly
                            ((ItemStackHandler) handler).setStackInSlot(i, remainder.copy());
                        } else {
                            // Slot is not empty → remainder goes to player inventory
                            if (!player.getInventory().add(remainder.copy())) {
                                player.drop(remainder.copy(), false);
                            }
                        }
                    }
                }

            }

            @Override
            public ItemStack remove(int amount) {
                if (this.hasItem())
                    this.removeCount += Math.min(amount, this.getItem().getCount());
                return super.remove(amount);
            }

            @Override
            public void onQuickCraft(ItemStack output, int amount) {
                this.removeCount += amount;
                this.checkTakeAchievements(output);
            }

            @Override
            protected void onSwapCraft(int amount) {
                this.removeCount = amount;
            }

            @Override
            protected void checkTakeAchievements(ItemStack stack) {
                if (this.removeCount > 0)
                    stack.onCraftedBy(AbstractSewingMachineMenu.this.player.level(), AbstractSewingMachineMenu.this.player, this.removeCount);
                // Award recipe to player for recipe book integration
                AbstractSewingMachineMenu.this.blockEntity.getCurrentRecipeHolder().ifPresent(holder ->
                    AbstractSewingMachineMenu.this.player.awardRecipes(List.of(holder))
                );
                this.removeCount = 0;
            }
        };
        this.addSlot(this.resultSlot);

        addDataSlots(data);
    }

    public List<Integer> getInputSlots() {
        return new ArrayList<>(craftingSlotIndices);
    }
    
    /**
     * Gets the result slot directly.
     * This is preferred over using slot index which varies based on blueprint presence.
     */
    public Slot getResultSlot() {
        return this.resultSlot;
    }


    // CREDIT GOES TO: diesieben07 | https://github.com/diesieben07/SevenCommons
    // must assign a slot number to each of the slots used by the GUI.
    // For this container, we can see both the tile inventory's slots as well as the player inventory slots and the hotbar.
    // Each time we add a Slot to the container, it automatically increases the slotIndex, which means
    //  0 - 8 = hotbar slots (which will map to the InventoryPlayer slot numbers 0 - 8)
    //  9 - 35 = player inventory slots (which map to the InventoryPlayer slot numbers 9 - 35)
    //  36 - 44 = TileInventory slots, which map to our TileEntity slot numbers 0 - 8)
    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
    private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
    private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;
    private static final int VANILLA_FIRST_SLOT_INDEX = 0;
    private static final int TE_INVENTORY_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;

    // THIS YOU HAVE TO DEFINE!
    private static final int TE_INVENTORY_SLOT_COUNT = 12;  // must be the number of slots you have!

    @Override
    public ItemStack quickMoveStack(Player playerIn, int pIndex) {
        Slot sourceSlot = slots.get(pIndex);
        if (!sourceSlot.hasItem()) return ItemStack.EMPTY;  //EMPTY_ITEM
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        // Check if the slot clicked is one of the vanilla container slots
        if (pIndex < VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT) {
            // This is a vanilla container slot so merge the stack into the tile inventory
            if (!moveItemStackTo(sourceStack, TE_INVENTORY_FIRST_SLOT_INDEX, TE_INVENTORY_FIRST_SLOT_INDEX
                    + TE_INVENTORY_SLOT_COUNT - 1, false)) {
                return ItemStack.EMPTY;  // EMPTY_ITEM
            }
        } else if (pIndex < TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT) {
            // This is a TE slot so merge the stack into the players inventory
            if (!moveItemStackTo(sourceStack, VANILLA_FIRST_SLOT_INDEX, VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            System.out.println("Invalid slotIndex:" + pIndex);
            return ItemStack.EMPTY;
        }
        // If stack size == 0 (the entire stack was moved) set slot contents to null
        if (sourceStack.getCount() == 0) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }
        sourceSlot.onTake(playerIn, sourceStack);
        return copyOfSourceStack;
    }

    public boolean isCrafting() {
        return data.get(0) > 0;
    }

    public int getScaledProgress() {
        int progress = this.data.get(0);
        int maxProgress = this.data.get(1);  // Max Progress
        int progressArrowSize = 24; // This is the height in pixels of your arrow

        return maxProgress != 0 && progress != 0 ? progress * progressArrowSize / maxProgress : 0;
    }

    @Override
    public boolean stillValid(Player player) {
        // get block at the container’s position
        BlockState state = player.level().getBlockState(blockEntity.getBlockPos());
        Block block = state.getBlock();

        // check if the block at that position is in your tag
        boolean isValid = state.is(ModTags.Blocks.SEWING_MACHINE);

        return AbstractContainerMenu.stillValid(
                ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                player,
                block  // only passed here for distance check
        ) && isValid;
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    public int getRemainingHits() {
        int progress = this.data.get(0);
        int maxProgress = this.data.get(1);
        return maxProgress - progress;
    }

    public ItemStack getResultItem() {
        // Check if the block entity exists and has an item handler
        if (blockEntity != null) {
            IItemHandler handler = blockEntity.getItemHandler();
            // Slot 10 is the output slot based on your menu setup
            ItemStack result = handler.getStackInSlot(10);
            // Return a copy to prevent modification of the original stack
            return result.copy();
        }
        return ItemStack.EMPTY;
    }

    public ItemStack getGhostResult() {
        //got rid of Polymorph Compat

        // Default behavior: return the expected result based on current inputs
        Optional<SewingRecipe> recipeOptional = blockEntity.getCurrentRecipe();
        if (recipeOptional.isPresent()) {
            SewingRecipe recipe = recipeOptional.get();
            if (blockEntity.hasRecipe()) {
                return recipe.getResultItem(level.registryAccess()).copy();
            }
        }
        return ItemStack.EMPTY;
    }
}
