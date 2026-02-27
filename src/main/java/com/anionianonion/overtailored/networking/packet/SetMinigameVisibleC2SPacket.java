package com.anionianonion.overtailored.networking.packet;

import com.anionianonion.overtailored.OvertailoredMod;
import com.anionianonion.overtailored.block.entity.AbstractSewingMachineBlockEntity;
import com.anionianonion.overtailored.events.ModItemInteractEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SetMinigameVisibleC2SPacket(Boolean visible, BlockPos pos) implements CustomPacketPayload {
    public static final ResourceLocation ID = OvertailoredMod.loc("set_minigame_visible");
    public static final Type<SetMinigameVisibleC2SPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, SetMinigameVisibleC2SPacket> STREAM_CODEC = StreamCodec.of(
            (buffer, packet) -> {
                ByteBufCodecs.BOOL.encode(buffer, packet.visible);
                BlockPos.STREAM_CODEC.encode(buffer, packet.pos);
            },
            buffer -> new SetMinigameVisibleC2SPacket(
                    ByteBufCodecs.BOOL.decode(buffer),
                    BlockPos.STREAM_CODEC.decode(buffer)
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SetMinigameVisibleC2SPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            if (!(player.level().getBlockEntity(payload.pos) instanceof AbstractSewingMachineBlockEntity sewingMachine)) return;

            sewingMachine.setMinigameOn(payload.visible);
            ModItemInteractEvents.playerMinigameVisibility.put(player.getUUID(), payload.visible);
        });
    }
}
