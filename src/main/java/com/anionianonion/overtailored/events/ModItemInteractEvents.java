package com.anionianonion.overtailored.events;


import com.anionianonion.overtailored.OvertailoredMod;
import com.anionianonion.overtailored.advancement.ModAdvancementTriggers;
import com.anionianonion.overtailored.block.ModBlocks;
import com.anionianonion.overtailored.block.custom.SewingMachineBlock;
import com.anionianonion.overtailored.block.entity.AbstractSewingMachineBlockEntity;
import com.anionianonion.overtailored.client.ClientAnvilMinigameData;
import com.anionianonion.overtailored.config.ServerConfig;
import com.anionianonion.overtailored.networking.packet.HideMinigameS2CPacket;
import com.anionianonion.overtailored.networking.packet.MinigameSetStartedC2SPacket;
import com.anionianonion.overtailored.networking.packet.MinigameSyncS2CPacket;
import com.anionianonion.overtailored.networking.packet.SetMinigameVisibleC2SPacket;
import com.anionianonion.overtailored.recipe.SewingRecipe;
import com.anionianonion.overtailored.utils.ModTags;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

//based on https://github.com/phuccom000/Overgeared/blob/1.21.1/src/main/java/net/stirdrem/overgeared/event/ModItemInteractEvents.java
@EventBusSubscriber(modid = OvertailoredMod.MOD_ID)
public class ModItemInteractEvents {
    public static final Map<UUID, BlockPos> playerAnvilPositions = new HashMap<>();
    public static final Map<UUID, Boolean> playerMinigameVisibility = new HashMap<>();
    private static final Set<ItemEntity> trackedEntities = ConcurrentHashMap.newKeySet();

    private static final ConcurrentMap<ItemEntity, Long> trackedSinceMs = new ConcurrentHashMap<>();
    private static final long TRACKED_PRUNE_MS = 2 * 60 * 1000L;
    private static final Random RANDOM = new Random();

    private static final Map<ServerLevel, List<ItemEntity>> trackedEntitiesPerWorld = new HashMap<>();

    @SubscribeEvent
    public static void onEntityPickupItem(ItemEntityPickupEvent.Post event) {
        if (!(event.getItemEntity().level() instanceof ServerLevel serverLevel)) return;

        List<ItemEntity> tracked = trackedEntitiesPerWorld.get(serverLevel);
        if (tracked != null) {
            tracked.remove(event.getItemEntity());
            if (tracked.isEmpty()) trackedEntitiesPerWorld.remove(serverLevel);
        }

        trackedSinceMs.remove(event.getItemEntity());
    }

    @SubscribeEvent
    public static void onUseSewingUtensil(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        ItemStack heldItem = event.getItemStack();

        if (!heldItem.is(ModTags.Items.SEWING_UTENSILS)) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        BlockEntity be = level.getBlockEntity(pos);
        BlockState clickedState = level.getBlockState(pos);

        // Shift-right-click to convert stone into smithing anvil
        if (!level.isClientSide && player.isCrouching() && clickedState.is(ModTags.Blocks.SEWING_STATION_BASES)
                && ServerConfig.ENABLE_STONE_TO_ANVIL.get()) {
            BlockState newState = ModBlocks.SIMPLE_SEWING_STATION_BLOCK.get()
                    .defaultBlockState()
                    //.setValue(StoneSmithingAnvil.FACING, player.getDirection().getClockWise())
            ;
            level.setBlock(pos, newState, 3);
            level.playSound(null, pos, SoundEvents.STONE_BREAK, SoundSource.BLOCKS, 1.0f, 1.0f);
            if (player instanceof ServerPlayer serverPlayer) {
                ModAdvancementTriggers.MAKE_SEWING_STATION.get()
                        .trigger(serverPlayer, "stone");
            }

            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
            return;
        }

        if (!level.isClientSide && player.isCrouching() && clickedState.is(Blocks.QUARTZ_BLOCK)
                && ServerConfig.ENABLE_ANVIL_TO_SMITHING.get()) {
            BlockState newState = ModBlocks.SEWING_MACHINE_BLOCK.get()
                    .defaultBlockState()
                    .setValue(SewingMachineBlock.FACING, player.getDirection().getClockWise());
            level.setBlock(pos, newState, 3);
            level.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.0f, 1.0f);
            if (player instanceof ServerPlayer serverPlayer) {
                ModAdvancementTriggers.MAKE_SEWING_STATION.get()
                        .trigger(serverPlayer, "iron");
            }
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
            return;
        }

        if (!level.isClientSide()) {
            if (!(be instanceof AbstractSewingMachineBlockEntity)) {
                hideMinigame((ServerPlayer) player);
            }
        }

        if (!(be instanceof
                AbstractSewingMachineBlockEntity abstractSewingMachineBlockEntity)) return;
        UUID playerUUID = player.getUUID();

        if (!player.isCrouching()) return;

        if (!level.isClientSide) {
            if (!(player instanceof ServerPlayer serverPlayer)) return;
            // Server-side ownership logic
            if (abstractSewingMachineBlockEntity.hasRecipe() && !ServerConfig.ENABLE_MINIGAME.get()) {
                serverPlayer.sendSystemMessage(Component.translatable("message.overtailored.no_minigame").withStyle(ChatFormatting.RED), true);
                return;
            }
            if (!abstractSewingMachineBlockEntity.hasRecipe()) {
                serverPlayer.sendSystemMessage(Component.translatable("message.overtailored.no_recipe").withStyle(ChatFormatting.RED), true);
                return;
            }

            if (!abstractSewingMachineBlockEntity.hasQuality() && !abstractSewingMachineBlockEntity.needsMinigame()) {
                serverPlayer.sendSystemMessage(Component.translatable("message.overtailored.item_has_no_quality").withStyle(ChatFormatting.RED), true);
                return;
            }

            UUID currentOwner = abstractSewingMachineBlockEntity.getOwnerUUID();
            if (currentOwner != null && !currentOwner.equals(playerUUID)) {
                serverPlayer.sendSystemMessage(Component.translatable("message.overtailored.sewing_station_in_use_by_another").withStyle(ChatFormatting.RED), true);
                return;
            }

            if (currentOwner == null && !playerAnvilPositions.containsKey(player.getUUID())) {
                abstractSewingMachineBlockEntity.setOwner(playerUUID);

                // ADD SERVER-SIDE TRACKING
                playerAnvilPositions.put(playerUUID, pos);
                playerMinigameVisibility.put(playerUUID, true);

                CompoundTag sync = new CompoundTag();
                sync.putUUID("anvilOwner", playerUUID);
                sync.putLong("anvilPos", pos.asLong());
                PacketDistributor.sendToAllPlayers(new MinigameSyncS2CPacket(sync));
                return;
            }

            if (playerAnvilPositions.get(player.getUUID()) != null && !pos.equals(playerAnvilPositions.get(player.getUUID()))) {
                serverPlayer.sendSystemMessage(Component.translatable("message.overtailored.another_sewing_station_in_use").withStyle(ChatFormatting.RED), true);
                return;
            }
        } else {
            if (abstractSewingMachineBlockEntity.hasRecipe() && !ServerConfig.ENABLE_MINIGAME.get()) {
                //player.sendSystemMessage(Component.translatable("message.overgeared.no_minigame").withStyle(ChatFormatting.RED), true);
                return;
            }
            if (!abstractSewingMachineBlockEntity.hasRecipe()) {
                //player.sendSystemMessage(Component.translatable("message.overgeared.no_recipe").withStyle(ChatFormatting.RED));
                return;
            }

            if (!abstractSewingMachineBlockEntity.hasQuality() && !abstractSewingMachineBlockEntity.needsMinigame()) {
                //player.sendSystemMessage(Component.translatable("message.overgeared.item_has_no_quality").withStyle(ChatFormatting.RED));
                return;
            }

            // Client should trust the server's sync data in ClientAnvilMinigameData
            UUID currentOwner = ClientAnvilMinigameData.getOccupiedAnvil(pos);
            if (currentOwner != null && !currentOwner.equals(player.getUUID())) {
                //player.sendSystemMessage(Component.translatable("message.overgeared.anvil_in_use_by_another").withStyle(ChatFormatting.RED));
                return;
            }

            if (player.getUUID().equals(currentOwner)
                    || currentOwner == null
                    && ClientAnvilMinigameData.getPendingMinigamePos() == null) {
                BlockPos pos1 = pos;
                BlockPos anvilPos = playerAnvilPositions.get(player.getUUID());
                if (playerAnvilPositions.get(player.getUUID()) != null && !pos.equals(playerAnvilPositions.get(player.getUUID()))) {
                    //player.sendSystemMessage(Component.translatable("message.overgeared.another_anvil_in_use").withStyle(ChatFormatting.RED));
                    event.setCanceled(true);
                    event.setCancellationResult(InteractionResult.PASS);
                    return;
                }
                if (abstractSewingMachineBlockEntity.hasRecipe() || abstractSewingMachineBlockEntity.needsMinigame()) {
                    abstractSewingMachineBlockEntity.setOwner(playerUUID);
                    AtomicReference<String> quality = new AtomicReference<>("perfect");
                    Optional<SewingRecipe> recipeOpt = abstractSewingMachineBlockEntity.getCurrentRecipe();
                    recipeOpt.ifPresent(recipe -> {
                        if (AnvilMinigameEvents.minigameStarted) {
                            boolean isVisible = AnvilMinigameEvents.isVisible();
                            AnvilMinigameEvents.setIsVisible(pos, !isVisible);
                            PacketDistributor.sendToServer(new SetMinigameVisibleC2SPacket(!isVisible, pos));
                            playerMinigameVisibility.put(player.getUUID(), !isVisible);
                        } else {
                            quality.set(abstractSewingMachineBlockEntity.minigameQuality().getDisplayName());
                            AnvilMinigameEvents.reset(quality.get());
                            playerAnvilPositions.put(player.getUUID(), pos);
                            playerMinigameVisibility.put(player.getUUID(), true);
                            AnvilMinigameEvents.setMinigameStarted(pos, true);
                            PacketDistributor.sendToServer(new MinigameSetStartedC2SPacket(pos));
                            PacketDistributor.sendToServer(new SetMinigameVisibleC2SPacket(true, pos));
                            AnvilMinigameEvents.setHitsRemaining(abstractSewingMachineBlockEntity.getRequiredProgress());
                        }
                    });
                }
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.PASS);
                return;
            }

            ClientAnvilMinigameData.setPendingMinigame(pos);
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }

    public static void hideMinigame(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new HideMinigameS2CPacket());
    }

    public static void handleAnvilOwnershipSync(CompoundTag syncData) {
        UUID owner = null;
        if (syncData.contains("anvilOwner")) {
            owner = syncData.getUUID("anvilOwner");
            if (owner.getMostSignificantBits() == 0 && owner.getLeastSignificantBits() == 0) {
                owner = null;
            }
        }
        BlockPos pos = BlockPos.of(syncData.getLong("anvilPos"));
        ClientAnvilMinigameData.putOccupiedAnvil(pos, owner);

        // ✅ Only start minigame if this client is the new owner and it was waiting
        if (Minecraft.getInstance().player != null
                && Minecraft.getInstance().player.getUUID().equals(owner)
                && pos.equals(ClientAnvilMinigameData.getPendingMinigamePos())) {

            BlockEntity be = Minecraft.getInstance().level.getBlockEntity(pos);
            if (be instanceof AbstractSewingMachineBlockEntity asmBE && asmBE.hasRecipe()) {
                Optional<SewingRecipe> recipeOpt = asmBE.getCurrentRecipe();
                recipeOpt.ifPresent(recipe -> {
                    ClientAnvilMinigameData.clearPendingMinigame(); // ✅ Done
                });
            }
        }
    }

    public static void releaseAnvil(ServerPlayer player, BlockPos pos) {
        UUID playerId = player.getUUID();
        if (playerMinigameVisibility.get(playerId) != null)
            playerMinigameVisibility.remove(playerId);
        if (playerAnvilPositions.get(playerId) != null
                && pos.equals(playerAnvilPositions.get(playerId))) {
            playerAnvilPositions.remove(playerId);

            // 1. Clear ownership from the block entity (server-side)
            BlockEntity be = player.level().getBlockEntity(pos);
            String quality = "perfect";
            if (be instanceof AbstractSewingMachineBlockEntity abstractSewingMachineBlockEntity) {
                abstractSewingMachineBlockEntity.clearOwner();
                quality = abstractSewingMachineBlockEntity.minigameQuality().getDisplayName();
            }
            // 3. Clear client-side state
            ClientAnvilMinigameData.putOccupiedAnvil(pos, null);
            AnvilMinigameEvents.reset(quality);
            // 4. Sync null ownership to all clients
            CompoundTag syncData = new CompoundTag();
            syncData.putLong("anvilPos", pos.asLong());
            syncData.putUUID("anvilOwner", new UUID(0, 0)); // special "no owner" UUID
            PacketDistributor.sendToAllPlayers(new MinigameSyncS2CPacket(syncData));
        }
    }
}
