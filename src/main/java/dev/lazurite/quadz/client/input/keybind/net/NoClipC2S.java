package dev.lazurite.quadz.client.input.keybind.net;

import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.common.entity.QuadcopterEntity;
import dev.lazurite.quadz.common.item.TransmitterItem;
import dev.lazurite.quadz.common.item.container.TransmitterContainer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;

import java.util.List;

public class NoClipC2S {
    public static final Identifier PACKET_ID = new Identifier(Quadz.MODID, "noclip_c2s");

    public static void accept(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        ItemStack hand = player.getMainHandStack();

        server.execute(() -> {
            if (hand.getItem() instanceof TransmitterItem) {
                TransmitterContainer transmitter = Quadz.TRANSMITTER_CONTAINER.get(hand);
                List<QuadcopterEntity> quadcopters = player.getEntityWorld().getEntitiesByClass(QuadcopterEntity.class, new Box(player.getBlockPos()).expand(80), null);

                for (QuadcopterEntity quadcopter : quadcopters) {
                    if (quadcopter.getBindId() == transmitter.getBindId()) {
//                        quadcopter.getRigidBody().setNoClip(!body.isNoClipEnabled());

//                        if (body.isNoClipEnabled()) {
//                            player.sendMessage(new TranslatableText("message.fpvracing.noclip_on"), false);
//                        } else {
//                            player.sendMessage(new TranslatableText("message.fpvracing.noclip_off"), false);
//                        }

                        break;
                    }
                }
            }
        });
    }

    public static void send() {
        ClientPlayNetworking.send(PACKET_ID, PacketByteBufs.create());
    }
}
