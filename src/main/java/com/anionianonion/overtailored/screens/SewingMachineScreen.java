package com.anionianonion.overtailored.screens;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

//copy of https://github.com/phuccom000/Overgeared/blob/1.21.1/src/main/java/net/stirdrem/overgeared/screen/SteelSmithingAnvilScreen.java
public class SewingMachineScreen extends AbstractSewingMachineScreen<SewingMachineMenu> {

    public SewingMachineScreen(SewingMachineMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
    }
}
