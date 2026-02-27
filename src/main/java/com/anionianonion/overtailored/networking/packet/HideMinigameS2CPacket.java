package com.anionianonion.overtailored.networking.packet;

import com.anionianonion.overtailored.OvertailoredMod;
import com.anionianonion.overtailored.events.AnvilMinigameEvents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class HideMinigameS2CPacket implements CustomPacketPayload {
    public static final ResourceLocation ID = OvertailoredMod.loc("hide_minigame");
    public static final Type<HideMinigameS2CPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, HideMinigameS2CPacket> STREAM_CODEC = StreamCodec.of(
            (buffer, packet) -> {},
            buffer -> new HideMinigameS2CPacket()
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(HideMinigameS2CPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            AnvilMinigameEvents.hideMinigame(player.getUUID());
        });
    }
}
