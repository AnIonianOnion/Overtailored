package com.anionianonion.overtailored.block.entity;

import com.anionianonion.overtailored.SewingMachineTier;
import com.anionianonion.overtailored.block.custom.SimpleSewingStationBlock;
import com.anionianonion.overtailored.screens.SimpleSewingStationMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class SimpleSewingStationBlockEntity extends AbstractSewingMachineBlockEntity {

    //todo: finish this class
    public SimpleSewingStationBlockEntity(BlockPos pos, BlockState blockState) {
        super((SimpleSewingStationBlock) blockState.getBlock(), SewingMachineTier.WELL,
                ModBlockEntities.SIMPLE_SEWING_STATION.get(), pos, blockState);
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory arg, Player player)
    {
        if (!player.isCrouching()) {
            return new SimpleSewingStationMenu(i, arg, this, this.data);
        } else return null;
    }
}
