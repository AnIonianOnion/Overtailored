package com.anionianonion.overtailored.networking.packet;

import com.anionianonion.overtailored.OvertailoredMod;
import com.anionianonion.overtailored.block.entity.AbstractSewingMachineBlockEntity;
import com.anionianonion.overtailored.events.AnvilMinigameEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static com.anionianonion.overtailored.events.ModItemInteractEvents.playerAnvilPositions;
import static com.anionianonion.overtailored.events.ModItemInteractEvents.playerMinigameVisibility;

public record MinigameSetStartedC2SPacket(BlockPos pos) implements CustomPacketPayload {
    public static final ResourceLocation ID = OvertailoredMod.loc("minigame_set_started_c2s");
    public static final Type<MinigameSetStartedC2SPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, MinigameSetStartedC2SPacket> STREAM_CODEC = StreamCodec.of(
            (buffer, packet) -> buffer.writeBlockPos(packet.pos),
            buffer -> new MinigameSetStartedC2SPacket(buffer.readBlockPos())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MinigameSetStartedC2SPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            if (!(player.level().getBlockEntity(payload.pos) instanceof AbstractSewingMachineBlockEntity sewingMachineBlockEntity)) return;
            AnvilMinigameEvents.setMinigameStarted(payload.pos, true);
            PacketDistributor.sendToPlayer(player, new MinigameSetStartedS2CPacket(payload.pos));
            playerAnvilPositions.put(player.getUUID(), payload.pos);
            playerMinigameVisibility.put(player.getUUID(), true);
            sewingMachineBlockEntity.setPlayer(player);
            sewingMachineBlockEntity.setMinigameOn(true);
        });
    }
}
