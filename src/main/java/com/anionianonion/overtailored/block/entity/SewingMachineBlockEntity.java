package com.anionianonion.overtailored.block.entity;

import com.anionianonion.overtailored.SewingMachineTier;
import com.anionianonion.overtailored.block.custom.SewingMachineBlock;
import com.anionianonion.overtailored.screens.SewingMachineMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class SewingMachineBlockEntity extends AbstractSewingMachineBlockEntity {

    //todo: finish this class
    public SewingMachineBlockEntity(BlockPos pos, BlockState blockState) {
        super((SewingMachineBlock) blockState.getBlock(), SewingMachineTier.MASTER,
                ModBlockEntities.SEWING_MACHINE.get(), pos, blockState);
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory arg, Player player)
    {
        if (!player.isCrouching()) {
            return new SewingMachineMenu(i, arg, this, this.data);
        } else return null;
    }

    @Override
    protected void craftItem() {
        super.craftItem();
        super.craftItemWithBlueprint();
    }
}
