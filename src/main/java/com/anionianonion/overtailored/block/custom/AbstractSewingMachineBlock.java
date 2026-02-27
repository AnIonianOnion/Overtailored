package com.anionianonion.overtailored.block.custom;

import com.anionianonion.overtailored.SewingMachineTier;
import com.anionianonion.overtailored.SewingQuality;
import com.anionianonion.overtailored.block.entity.AbstractSewingMachineBlockEntity;
import com.anionianonion.overtailored.config.ServerConfig;
import com.anionianonion.overtailored.events.AnvilMinigameEvents;
import com.anionianonion.overtailored.events.ModEvents;
import com.anionianonion.overtailored.events.ModItemInteractEvents;
import com.anionianonion.overtailored.networking.packet.PacketSendCounterC2SPacket;
import com.anionianonion.overtailored.sound.ModSounds;
import com.anionianonion.overtailored.utils.ModTags;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import org.joml.Random;
import org.joml.Vector3f;

import java.util.UUID;

//https://www.youtube.com/watch?v=sQzHmHba92o - ty Kaupenjoe
//based on https://github.com/phuccom000/Overgeared/blob/master/src/main/java/net/stirdrem/overgeared/block/custom/AbstractSmithingAnvilNew.java
public abstract class AbstractSewingMachineBlock extends BaseEntityBlock {
    //makes clothes from fabric

    protected static SewingQuality quality = null;
    protected static SewingMachineTier tier;

    public AbstractSewingMachineBlock(SewingMachineTier sewingMachineTier, Properties properties) {
        super(properties);
        tier = sewingMachineTier;
    }

    public static SewingQuality getQuality() {
        return quality != null ? quality : SewingQuality.NONE;
    }

    public static void setQuality(SewingQuality sewingQuality) {
        AbstractSewingMachineBlock.quality = sewingQuality;
    }

    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public abstract BlockEntity newBlockEntity(BlockPos pos, BlockState state);

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if(state.getBlock() != newState.getBlock()) {
            if(level.getBlockEntity(pos) instanceof
                    AbstractSewingMachineBlockEntity abstractSewingMachineBlockEntity) {
                Containers.dropContents(level, pos, abstractSewingMachineBlockEntity);

                // Remove block entity BEFORE super.onRemove
                level.removeBlockEntity(pos);

                level.updateNeighbourForOutputSignal(pos, this);

                if (!level.isClientSide()) {
                    ModEvents.resetMinigameForAnvil(level, pos);
                }
            }
        }

        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        BlockEntity be = level.getBlockEntity(pos);
        if(!(be instanceof AbstractSewingMachineBlockEntity abstractSewingMachineBlockEntity)) return InteractionResult.PASS;
        if(level.isClientSide()) return InteractionResult.SUCCESS;

        // Check ownership
        if (abstractSewingMachineBlockEntity.hasRecipe()) {
            UUID currentOwner = abstractSewingMachineBlockEntity.getOwnerUUID();
            if (currentOwner != null && !currentOwner.equals(player.getUUID()) && player instanceof ServerPlayer serverPlayer) {
                Player ownerPlayer = level.getPlayerByUUID(currentOwner);
                String ownerName = ownerPlayer != null ? ownerPlayer.getDisplayName().getString() : "Another player";

                serverPlayer.sendSystemMessage(
                        Component.translatable("message.overtailored.sewing_station_in_use_by_another", ownerName)
                                .withStyle(ChatFormatting.RED),
                        true
                );
                return InteractionResult.FAIL;
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected void spawnDestroyParticles(Level level, Player player, BlockPos pos, BlockState state) {
        level.addParticle(
                ParticleTypes.ASH,
                pos.getX() + 0.5,
                pos.getY() + 0.5,
                pos.getZ() + 0.5,
                0,
                0.02,
                0
        );
    }

    @Override
    public ItemInteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos,
                                           Player player, InteractionHand hand, BlockHitResult hit) {
        boolean isSewingNeedle = held.is(ModTags.Items.SEWING_UTENSILS);
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof AbstractSewingMachineBlockEntity sewingMachine)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        // CLIENT-SIDE: Handle minigame hit processing
        if (level.isClientSide()) {
            if (player.isCrouching()) {
                return ItemInteractionResult.SUCCESS; // Let server handle opening menu
            }

            if (sewingMachine.hasRecipe() && isSewingNeedle) {
                // Check if this is our anvil
                BlockPos ourAnvilPos = AnvilMinigameEvents.getAnvilPos(player.getUUID());
                if (ourAnvilPos != null && !pos.equals(ourAnvilPos)) {
                    // Not our anvil - don't process hit
                    return ItemInteractionResult.SUCCESS;
                }

                // Check if minigame is visible (quality recipe with minigame enabled)
                if (AnvilMinigameEvents.isVisible()) {
                    // Process the hit client-side to update minigame state
                    AnvilMinigameEvents.resetPopUps();
                    String hitQuality = AnvilMinigameEvents.handleHit();
                    // Send the quality result to server
                    PacketDistributor.sendToServer(new PacketSendCounterC2SPacket(hitQuality, pos));
                    return ItemInteractionResult.SUCCESS;
                } else if (!sewingMachine.hasQuality() && !sewingMachine.needsMinigame()) {
                    // Non-quality recipe without minigame - allow direct hammering
                    return ItemInteractionResult.SUCCESS;
                } else if (!ServerConfig.ENABLE_MINIGAME.get()) {
                    // Minigame disabled - allow direct hammering
                    return ItemInteractionResult.SUCCESS;
                }
            }
            return ItemInteractionResult.SUCCESS;
        }

        // SERVER-SIDE handling
        // Check ownership
        if (sewingMachine.hasRecipe()) {
            UUID currentOwner = sewingMachine.getOwnerUUID();
            if (currentOwner != null && !currentOwner.equals(player.getUUID()) && player instanceof ServerPlayer serverPlayer) {
                Player ownerPlayer = level.getPlayerByUUID(currentOwner);
                String ownerName = ownerPlayer != null ? ownerPlayer.getDisplayName().getString() : "Another player";

                serverPlayer.sendSystemMessage(
                        Component.translatable("message.overtailored.sewing_station_in_use_by_another", ownerName)
                                .withStyle(ChatFormatting.RED),
                        true
                );
                return ItemInteractionResult.FAIL;
            }

            // Allow hammering for non-quality recipes OR when minigame is disabled
            if (isSewingNeedle &&
                    (sewingMachine.isMinigameOn() ||
                            (!sewingMachine.hasQuality() && !sewingMachine.needsMinigame())
                    || !ServerConfig.ENABLE_MINIGAME.get())) {
                // Check if player is at the correct anvil
                BlockPos playerAnvilPos = ModItemInteractEvents.playerAnvilPositions.get(player.getUUID());
                if (playerAnvilPos != null && !pos.equals(playerAnvilPos)) {
                    ServerPlayer serverPlayer = (ServerPlayer) player;
                    serverPlayer.sendSystemMessage(
                            Component.translatable("message.overtailored.another_sewing_station_in_use")
                                    .withStyle(ChatFormatting.RED),
                            true
                    );
                    return ItemInteractionResult.FAIL;
                }

                // Check minigame visibility from server tracking
                Boolean visible = ModItemInteractEvents.playerMinigameVisibility.get(player.getUUID());
                if (visible == null && sewingMachine.isMinigameOn()) {
                    // Player hasn't started minigame yet, open menu instead
                    ModItemInteractEvents.hideMinigame((ServerPlayer) player);
                    player.openMenu(sewingMachine, pos);
                    return ItemInteractionResult.sidedSuccess(level.isClientSide());
                }

                // Process the hammer hit
                held.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
                sewingMachine.increaseForgingProgress(level, pos, state);
                spawnAnvilParticles(level, pos);

                // Play appropriate sound based on hits remaining
                if (sewingMachine.getHitsRemaining() == 1) {
                    if (sewingMachine.isFailedResult()) {
                        level.playSound(null, pos, ModSounds.FORGING_FAILED.get(), SoundSource.BLOCKS, 1f, 1f);
                    } else {
                        level.playSound(null, pos, ModSounds.FORGING_COMPLETE.get(), SoundSource.BLOCKS, 1f, 1f);
                    }
                } else {
                    level.playSound(null, pos, ModSounds.ANVIL_HIT.get(), SoundSource.BLOCKS, 1f, 1f);
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide());
            }

            // Has recipe but can't hammer - open menu
            ModItemInteractEvents.hideMinigame((ServerPlayer) player);
        } else {
            // No recipe - release anvil ownership
            ModItemInteractEvents.releaseAnvil((ServerPlayer) player, pos);
        }

        player.openMenu(sewingMachine, pos);
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }

    protected void spawnAnvilParticles(Level level, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel) {

            Random random = new Random();
            for (int i = 0; i < 6; i++) {
                double offsetX = 0.5 + (random.nextFloat() - 0.5);
                double offsetY = 1.0 + random.nextFloat() * 0.5;
                double offsetZ = 0.5 + (random.nextFloat() - 0.5);
                double velocityX = (random.nextFloat() - 0.5) * 0.1;
                double velocityY = random.nextFloat() * 0.1;
                double velocityZ = (random.nextFloat() - 0.5) * 0.1;

                serverLevel.sendParticles(new DustParticleOptions(new Vector3f(1.0f, 0.5f, 0.0f), 1.0f),
                        pos.getX() + offsetX, pos.getY() + offsetY, pos.getZ() + offsetZ, 1,
                        velocityX, velocityY, velocityZ, 1);
                serverLevel.sendParticles(ParticleTypes.CRIT,
                        pos.getX() + offsetX, pos.getY() + offsetY, pos.getZ() + offsetZ, 1,
                        velocityX, velocityY, velocityZ, 1);
            }
        }
    }

    @Override
    public abstract VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context);

}
