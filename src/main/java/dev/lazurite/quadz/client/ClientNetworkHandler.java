package dev.lazurite.quadz.client;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;

public class ClientNetworkHandler {
    public static void onSelectSlot(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        int slot = buf.readInt();

        client.execute(() -> {
            if (client.player != null) {
                client.player.inventory.selectedSlot = slot;
            }
        });
    }
}
