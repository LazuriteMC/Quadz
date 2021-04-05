package dev.lazurite.quadz.client;

import dev.lazurite.quadz.client.render.QuadzRendering;
import dev.lazurite.quadz.common.data.DataDriver;
import dev.lazurite.quadz.common.data.model.Template;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;

import java.util.concurrent.CompletableFuture;

public class ClientNetworkHandler {
    public static void onTemplateReceived(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        Template template = Template.deserialize(buf);

        client.execute(() -> {
            DataDriver.load(template);
            CompletableFuture.runAsync(() -> QuadzRendering.templateLoader.load(template));
        });
    }

    public static void onSelectSlot(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        int slot = buf.readInt();

        client.execute(() -> {
            if (client.player != null) {
                client.player.inventory.selectedSlot = slot;
            }
        });
    }
}
