package com.anionianonion.overtailored.events;

import com.anionianonion.overtailored.PatternQuality;
import com.anionianonion.overtailored.OvertailoredMod;
import com.anionianonion.overtailored.SewingQuality;
import com.anionianonion.overtailored.block.entity.AbstractSewingMachineBlockEntity;
import com.anionianonion.overtailored.components.ModComponents;
import com.anionianonion.overtailored.config.ServerConfig;
import com.anionianonion.overtailored.utils.ModTags;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import com.anionianonion.overtailored.networking.packet.*;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

//based on https://github.com/phuccom000/Overgeared/blob/1.21.1/src/main/java/net/stirdrem/overgeared/event/ModEvents.java
@EventBusSubscriber(modid = OvertailoredMod.MOD_ID)
public class ModEvents {

    private static int serverTick = 0;

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        serverTick++;
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        Level level = player.level();
        handleAnvilDistance(player, level);
    }

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        List<Component> tooltip = event.getToolTip();
        int insertOffset = 1;


        // Add Sewing Quality
        SewingQuality quality = stack.get(ModComponents.SEWING_QUALITY);
        if (quality != null) {
            Component qualityComponent = switch (quality) {
                case MASTER ->
                        Component.translatable("tooltip.overtailored.master").withStyle(ChatFormatting.LIGHT_PURPLE);
                case PERFECT -> Component.translatable("tooltip.overtailored.perfect").withStyle(ChatFormatting.GOLD);
                case EXPERT -> Component.translatable("tooltip.overtailored.expert").withStyle(ChatFormatting.BLUE);
                case WELL -> Component.translatable("tooltip.overtailored.well").withStyle(ChatFormatting.YELLOW);
                case POOR -> Component.translatable("tooltip.overtailored.poor").withStyle(ChatFormatting.RED);
                default -> null;
            };
            if (qualityComponent != null) {
                tooltip.add(insertOffset++, qualityComponent);
            }
        }

        // Smithing Hammer special tooltip
        if (stack.is(ModTags.Items.SEWING_UTENSILS)) {
            if (!Screen.hasShiftDown()) {
                tooltip.add(insertOffset, Component.translatable("tooltip.overtailored.sewing_needle.hold_shift")
                        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            } else {
                tooltip.add(insertOffset++, Component.translatable("tooltip.overtailored.sewing_needle.advanced_tooltip.line1")
                        .withStyle(ChatFormatting.GRAY));
                tooltip.add(insertOffset++, Component.translatable("tooltip.overtailored.sewing_needle.advanced_tooltip.line2")
                        .withStyle(ChatFormatting.GRAY));
                if (ServerConfig.ENABLE_STONE_TO_ANVIL.get())
                    tooltip.add(insertOffset++, Component.translatable("tooltip.overtailored.sewing_needle.advanced_tooltip.line3")
                            .withStyle(ChatFormatting.GRAY));
                if (ServerConfig.ENABLE_ANVIL_TO_SMITHING.get())
                    tooltip.add(insertOffset++, Component.translatable("tooltip.overtailored.sewing_needle.advanced_tooltip.line4")
                            .withStyle(ChatFormatting.GRAY));
            }
        }

        String creatorName = stack.get(ModComponents.CREATOR);
        if (creatorName != null) {
            Component creatorComponent = Component.translatable("tooltip.overtailored.made_by")
                    .append(" ")
                    .append(creatorName)
                    .withStyle(ChatFormatting.GRAY);
            tooltip.add(insertOffset++, creatorComponent);
        }
    }

    private static void handleAnvilDistance(ServerPlayer player, Level level) {
        if (AnvilMinigameEvents.hasAnvilPosition(player.getUUID())) {
            BlockPos anvilPos = AnvilMinigameEvents.getAnvilPos(player.getUUID());
            BlockEntity be = level.getBlockEntity(anvilPos);
            if ((be instanceof AbstractSewingMachineBlockEntity)) {
                double distSq = player.blockPosition().distSqr(anvilPos);
                int maxDist = ServerConfig.MAX_WORKBENCH_DISTANCE.get(); // e.g. 7
                if (distSq > maxDist * maxDist) {
                    resetMinigameForPlayer(player);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onItemAttributes(ItemAttributeModifierEvent event) {

        // Add this first
        //does fire
        //System.out.println("ItemAttributeModifierEvent fired for: " + event.getItemStack().getItem());

        ItemStack stack = event.getItemStack();

        if (!isArmor(stack.getItem())) return;

        SewingQuality quality = stack.get(ModComponents.SEWING_QUALITY.get());
        if (quality == null || quality == SewingQuality.NONE) {
            return;
        }

        List<AttributeModifier> mods = stack.get(ModComponents.QUALITY_MODIFIERS.get());

        ResourceLocation resourceLocation = BuiltInRegistries.ITEM.getKey(stack.getItem());
        EquipmentSlotGroup slotGroup = getEquipmentSlotGroup(resourceLocation);

        if(mods == null) return;

        for (AttributeModifier modifier : mods) {
            var attrId = modifier.id();
            var holder = BuiltInRegistries.ATTRIBUTE.getHolder(attrId).orElse(null);
            if (holder == null) {
                OvertailoredMod.LOGGER.warn(String.format("Holder %s is null", attrId));
                continue;
            }

            event.addModifier(holder, modifier, slotGroup);
        }
    }

    private static @NotNull EquipmentSlotGroup getEquipmentSlotGroup(ResourceLocation resourceLocation) {
        //assumes that the item you equip can only be equipped into Minecraft's 4 armor slots.

        String id = resourceLocation.getPath();

        EquipmentSlotGroup slotGroup;

        //todo: expose this to an API
        if(id.contains("helm") || id.contains("_hat") || id.contains("_cap") || id.contains("hood") || id.contains("circlet") || id.contains("crown") || id.contains("tricorne")) slotGroup = EquipmentSlotGroup.HEAD;
        else if(id.contains("chestplate") || id.contains("robe") || id.contains("vest")) slotGroup = EquipmentSlotGroup.BODY;
        else if(id.contains("leggings") || id.contains("pants") || id.contains("trousers")) slotGroup = EquipmentSlotGroup.LEGS;
        else if(id.contains("boots") || id.contains("shoes") || id.contains("soles") || id.contains("sandals")) slotGroup = EquipmentSlotGroup.FEET;
        else slotGroup = EquipmentSlotGroup.ARMOR;
        return slotGroup;
    }

    private static AttributeModifier createModifiedAttribute(AttributeModifier original, double bonus) {
        return new AttributeModifier(
                original.id(),
                original.amount() + bonus,
                original.operation()
        );
    }

    private static boolean isArmor(Item item) {
        return item instanceof ArmorItem;
    }

    @SubscribeEvent
    public static void onPlayerJoinWorld(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // Reset minigame state when joining any world
            resetMinigameForPlayer(player);

            // Start timeout counter if needed
            //startTimeoutCounter(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerDisconnect(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            resetMinigameForPlayer(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            resetMinigameForPlayer(player);
            //startTimeoutCounter(player);
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        MinecraftServer server = event.getServer();

        // Iterate over all players and reset their minigame
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            resetMinigameForPlayer(player);
        }

        // Optional: Log for debugging
        OvertailoredMod.LOGGER.info("Reset all minigames on server stop.");
    }

    public static void resetMinigameForPlayer(ServerPlayer player) {
        if (player == null) return;
        UUID playerId = player.getUUID();
        PacketDistributor.sendToPlayer(player, new OnlyResetMinigameS2CPacket());
        String blueprintQuality = PatternQuality.PERFECT.getDisplayName();
        if (ModItemInteractEvents.playerAnvilPositions.containsKey(player.getUUID())) {
            BlockPos anvilPos = ModItemInteractEvents.playerAnvilPositions.get(player.getUUID());
            BlockEntity be = player.level().getBlockEntity(anvilPos);

            // Only execute on server side
            if (be instanceof AbstractSewingMachineBlockEntity sewingMachine) {
                sewingMachine.setProgress(0);
                sewingMachine.setChanged();
                sewingMachine.setMinigameOn(false);

                // Send reset packet to the specific player
                PacketDistributor.sendToPlayer(player, new ResetMinigameS2CPacket(anvilPos));
                //ModItemInteractEvents.releaseAnvil(player, anvilPos);
                ModItemInteractEvents.playerAnvilPositions.remove(playerId);
                ModItemInteractEvents.playerMinigameVisibility.remove(playerId);
                Block block = player.level().getBlockState(anvilPos).getBlock();
                blueprintQuality = sewingMachine.minigameQuality().getDisplayName();
                /*if (block instanceof AbstractSmithingAnvilNew anvilNew) {
                    anvilNew.setMinigameOn(false);
                }*/
            }


        }

        AnvilMinigameEvents.reset(blueprintQuality);
        //playerTimeoutCounters.remove(player.getUUID());
    }

    public static void resetMinigameForPlayer(ServerPlayer player, BlockPos anvilPos) {
        // Only execute on server side
        if (player == null) return;
        PacketDistributor.sendToPlayer(player, new OnlyResetMinigameS2CPacket());
        BlockEntity be = player.level().getBlockEntity(anvilPos);
        String quality = "perfect";
        if (be instanceof AbstractSewingMachineBlockEntity anvil) {
            anvil.setProgress(0);
            anvil.setChanged();
            anvil.setMinigameOn(false);
            quality = anvil.minigameQuality().getDisplayName();
        }
        AnvilMinigameEvents.reset(quality);
        Block block = player.level().getBlockState(anvilPos).getBlock();

        // Send reset packet to the specific player
        ModItemInteractEvents.playerAnvilPositions.remove(player.getUUID());
        ModItemInteractEvents.playerMinigameVisibility.remove(player.getUUID());
    }

    public static void resetMinigameForAnvil(Level level, BlockPos anvilPos) {
        // Only execute on server side
        String quality = "perfect";
        // Reset the anvil block entity
        BlockEntity be = level.getBlockEntity(anvilPos);
        if (be instanceof AbstractSewingMachineBlockEntity anvil) {
            anvil.setProgress(0);
            anvil.setChanged();
            anvil.setMinigameOn(false);
            anvil.clearOwner(); // Clear ownership from the anvil itself
            quality = anvil.minigameQuality().getDisplayName();
        }

        AnvilMinigameEvents.reset(quality);
        // Find the specific player using this anvil and reset only them
        if (level instanceof ServerLevel serverLevel) {

            for (ServerPlayer player : serverLevel.getServer().getPlayerList().getPlayers()) {
                UUID playerId = player.getUUID();
                PacketDistributor.sendToPlayer(player, new ResetMinigameS2CPacket(anvilPos));
                if (ModItemInteractEvents.playerAnvilPositions.getOrDefault(playerId, BlockPos.ZERO).equals(anvilPos)) {
                    // Send reset packet only to this specific player

                    // Clear server-side tracking for this player
                    ModItemInteractEvents.playerAnvilPositions.remove(playerId);
                    ModItemInteractEvents.playerMinigameVisibility.remove(playerId);
                    break; // Only reset the first player found (should only be one)
                }
            }
        }
    }
}