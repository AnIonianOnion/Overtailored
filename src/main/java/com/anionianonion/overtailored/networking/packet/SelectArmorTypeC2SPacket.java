package com.anionianonion.overtailored.networking.packet;

import com.anionianonion.overtailored.OvertailoredMod;
import com.anionianonion.overtailored.item.ArmorType;
import com.anionianonion.overtailored.item.ArmorTypeRegistry;
import com.anionianonion.overtailored.screens.PatternWorkbenchMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Optional;

public record SelectArmorTypeC2SPacket(String armorTypeId, int containerId) implements CustomPacketPayload {
    public static final ResourceLocation ID = OvertailoredMod.loc("select_tool_type");
    public static final Type<SelectArmorTypeC2SPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, SelectArmorTypeC2SPacket> STREAM_CODEC = StreamCodec.of(
            (buffer, packet) -> {
                ByteBufCodecs.STRING_UTF8.encode(buffer, packet.armorTypeId);
                ByteBufCodecs.INT.encode(buffer, packet.containerId);
            },
            buffer -> new SelectArmorTypeC2SPacket(
                    ByteBufCodecs.STRING_UTF8.decode(buffer),
                    ByteBufCodecs.INT.decode(buffer)
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SelectArmorTypeC2SPacket payload,  IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            Optional<ArmorType> optional = ArmorTypeRegistry.byId(payload.armorTypeId);
            if (optional.isPresent()) {
                OvertailoredMod.LOGGER.debug("ArmorType '{}' found. Proceeding to create blueprint.", payload.armorTypeId);
                if (player.containerMenu instanceof PatternWorkbenchMenu menu) {
                    menu.createBlueprint(optional.get());
                    menu.broadcastChanges(); // ensure client sync
                } else {
                    OvertailoredMod.LOGGER.warn("Player '{}' is not in PatternWorkbenchMenu, but in {}",
                            player.getGameProfile().getName(),
                            player.containerMenu.getClass().getSimpleName());
                }
            } else {
                OvertailoredMod.LOGGER.error("ArmorTypeRegistry.byId('{}') returned empty; cannot create blueprint.", payload.armorTypeId);
            }
        });
    }
}