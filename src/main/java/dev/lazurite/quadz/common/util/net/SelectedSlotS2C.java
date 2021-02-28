package dev.lazurite.quadz.common.util.net;

import dev.lazurite.quadz.Quadz;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class SelectedSlotS2C {
    public static final Identifier PACKET_ID = new Identifier(Quadz.MODID, "selected_slot_s2c");

    public static void accept(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        int slot = buf.readInt();

        client.execute(() -> {
            if (client.player != null) {
                client.player.inventory.selectedSlot = slot;
            }
        });
    }

    public static void send(ServerPlayerEntity player, int slot) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(slot);
        ServerPlayNetworking.send(player, PACKET_ID, buf);
    }
}
