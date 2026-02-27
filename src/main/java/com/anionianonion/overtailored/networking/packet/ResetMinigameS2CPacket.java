package com.anionianonion.overtailored.networking.packet;

import com.anionianonion.overtailored.OvertailoredMod;
import com.anionianonion.overtailored.block.entity.AbstractSewingMachineBlockEntity;
import com.anionianonion.overtailored.events.AnvilMinigameEvents;
import com.anionianonion.overtailored.events.ModItemInteractEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ResetMinigameS2CPacket(BlockPos anvilPos) implements CustomPacketPayload {
    public static final ResourceLocation ID = OvertailoredMod.loc("reset_minigame");
    public static final Type<ResetMinigameS2CPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, ResetMinigameS2CPacket> STREAM_CODEC = StreamCodec.of(
            (buffer, packet) -> BlockPos.STREAM_CODEC.encode(buffer, packet.anvilPos),
            buffer -> new ResetMinigameS2CPacket(BlockPos.STREAM_CODEC.decode(buffer))
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ResetMinigameS2CPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            BlockEntity be = player.level().getBlockEntity(payload.anvilPos);
            if (!(be instanceof AbstractSewingMachineBlockEntity anvil)) return;
            String quality = anvil.minigameQuality().getDisplayName();
            OvertailoredMod.LOGGER.debug(
                    "Resetting minigame for {} at anvil {} with quality {}",
                    player.getName().getString(), payload.anvilPos, quality
            );
            // Only reset if the player's tracked anvil matches
            if (ModItemInteractEvents.playerAnvilPositions
                    .getOrDefault(player.getUUID(), BlockPos.ZERO)
                    .equals(payload.anvilPos)) {
                ModItemInteractEvents.playerAnvilPositions.remove(player.getUUID());
                ModItemInteractEvents.playerMinigameVisibility.remove(player.getUUID());
                AnvilMinigameEvents.reset(quality);
            }
        });
    }
}
