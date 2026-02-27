package com.anionianonion.overtailored.screens;

import com.anionianonion.overtailored.block.entity.SimpleSewingStationBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;

//copy of https://github.com/phuccom000/Overgeared/blob/1.21.1/src/main/java/net/stirdrem/overgeared/screen/StoneSmithingAnvilMenu.java
public class SimpleSewingStationMenu extends AbstractSewingMachineMenu {
    // Create a constructor that matches the parent class
    public SimpleSewingStationMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, (SimpleSewingStationBlockEntity) inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(11));
    }

    public SimpleSewingStationMenu(int containerId, Inventory inv, SimpleSewingStationBlockEntity entity, ContainerData data) {
        super(ModMenuTypes.SIMPLE_SEWING_STATION_MENU.get(), containerId, inv, entity, data, false);
    }

    // You can override any methods from the parent class here if you need custom behavior
    // specific to the steel smithing anvil
}