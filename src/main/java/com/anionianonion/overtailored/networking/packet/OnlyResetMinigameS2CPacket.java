package com.anionianonion.overtailored.networking.packet;

import com.anionianonion.overtailored.OvertailoredMod;
import com.anionianonion.overtailored.events.AnvilMinigameEvents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OnlyResetMinigameS2CPacket() implements CustomPacketPayload {
    public static final ResourceLocation ID = OvertailoredMod.loc("only_reset_minigame");
    public static final Type<OnlyResetMinigameS2CPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, OnlyResetMinigameS2CPacket> STREAM_CODEC = StreamCodec.of(
            (buffer, packet) -> {},
            buffer -> new OnlyResetMinigameS2CPacket()
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OnlyResetMinigameS2CPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> AnvilMinigameEvents.reset());
    }
}