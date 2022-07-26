package dev.lazurite.quadz.client.util.network;

import dev.lazurite.quadz.client.QuadzClient;
import dev.lazurite.quadz.common.data.model.Bindable;
import dev.lazurite.quadz.common.util.tools.RemoteControllableSearch;
import dev.lazurite.quadz.common.data.template.TemplateLoader;
import dev.lazurite.quadz.common.data.template.model.Template;
import dev.lazurite.toolbox.api.network.PacketRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class ClientNetworkHandler {
    public static void onInputFrameReceived(PacketRegistry.ClientboundContext context) {
        final var client = Minecraft.getInstance();
        final var buf = context.byteBuf();

        final var joystickAxisValues = new HashMap<ResourceLocation, Float>();
        final var joystickCount = buf.readInt();

        for (int i = 0; i < joystickCount; i++) {
            final var resourceLocation = buf.readResourceLocation();
            final var axisValue = buf.readFloat();
            joystickAxisValues.put(resourceLocation, axisValue);
        }

        client.execute(() -> {
            Bindable.get(client.player.getMainHandItem()).ifPresent(transmitter -> {
                RemoteControllableSearch.byBindId(client.level, client.player.position(), transmitter.getBindId(), 100).ifPresent(entity -> {
                    if (entity.isBoundTo(transmitter)) {
                        entity.setJoystickValues(joystickAxisValues);
                    }
                });
            });
        });
    }

    public static void onTemplateReceived(PacketRegistry.ClientboundContext context) {
        final var client = Minecraft.getInstance();
        final var buf = context.byteBuf();
        final var template = Template.deserialize(buf);

        client.execute(() -> {
            TemplateLoader.load(template);
            CompletableFuture.runAsync(() -> QuadzClient.templateLoader.load(template));
        });
    }
}