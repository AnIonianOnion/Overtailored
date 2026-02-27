package com.anionianonion.overtailored.networking.packet;

import com.anionianonion.overtailored.OvertailoredMod;
import com.anionianonion.overtailored.SewingQuality;
import com.anionianonion.overtailored.block.custom.AbstractSewingMachineBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketSendCounterC2SPacket(String quality, BlockPos pos) implements CustomPacketPayload {
    public static final ResourceLocation ID = OvertailoredMod.loc("packet_send_counter");
    public static final Type<PacketSendCounterC2SPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, PacketSendCounterC2SPacket> STREAM_CODEC = StreamCodec.of(
            (buffer, packet) -> {
                ByteBufCodecs.STRING_UTF8.encode(buffer, packet.quality);
                BlockPos.STREAM_CODEC.encode(buffer, packet.pos);
            },
            buffer -> new PacketSendCounterC2SPacket(
                    ByteBufCodecs.STRING_UTF8.decode(buffer),
                    BlockPos.STREAM_CODEC.decode(buffer)
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketSendCounterC2SPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            if (!(player.level().getBlockState(payload.pos).getBlock() instanceof AbstractSewingMachineBlock)) return;
            AbstractSewingMachineBlock.setQuality(SewingQuality.fromString(payload.quality));
        });
    }
}
