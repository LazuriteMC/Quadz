package dev.lazurite.quadz.common.util.network;

import dev.lazurite.quadz.common.data.model.Bindable;
import dev.lazurite.quadz.common.data.template.TemplateLoader;
import dev.lazurite.quadz.common.data.template.model.Template;
import dev.lazurite.quadz.common.entity.QuadcopterEntity;
import dev.lazurite.quadz.common.util.tools.RemoteControllableSearch;
import dev.lazurite.toolbox.api.network.PacketRegistry;
import dev.lazurite.toolbox.api.network.ServerNetworking;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;

public class CommonNetworkHandler {
    public static void onTemplateReceived(PacketRegistry.ServerboundContext context) {
        final var player = context.player();
        final var server = player.getServer();
        final var buf = context.byteBuf();

        final var template = Template.deserialize(buf);

        server.execute(() -> {
            PlayerLookup.all(server).forEach(p -> {
                if (!p.equals(player)) {
                    ServerNetworking.send(p, NetworkResources.TEMPLATE, template::serialize);
                }
            });

            TemplateLoader.load(template);
        });
    }

    public static void onInputFrame(PacketRegistry.ServerboundContext context) {
        final var player = context.player();
        final var server = player.getServer();
        final var buf = context.byteBuf();

        final var joystickAxisValues = new HashMap<ResourceLocation, Float>();
        final var joystickCount = buf.readInt();

        for (int i = 0; i < joystickCount; i++) {
            final var resourceLocation = buf.readResourceLocation();
            final var axisValue = buf.readFloat();
            joystickAxisValues.put(resourceLocation, axisValue);
        }

        server.execute(() -> {
            Bindable.get(player.getMainHandItem()).ifPresent(transmitter -> {
                RemoteControllableSearch.byBindId(player.level, player.position(), transmitter.getBindId(), 100).ifPresent(entity -> {
                    if (entity.isBoundTo(transmitter)) {
                        entity.setJoystickValues(joystickAxisValues);
                    }
                });
            });
        });
    }

    public static void onRemoteControllableViewRequestReceived(PacketRegistry.ServerboundContext context) {
        final var player = context.player();
        final var server = player.getServer();
        final var buf = context.byteBuf();
        final var spectateDirection = buf.readInt();

        server.execute(() -> {
            if (player.getCamera() instanceof QuadcopterEntity quadcopter) {
                final var allQuads = new ArrayList<>(RemoteControllableSearch.allBeingViewed(server));
                final var index = Math.max(allQuads.lastIndexOf(quadcopter) + spectateDirection, 0);
                player.setCamera(allQuads.get(index % allQuads.size()));
            } else {
                Bindable.get(player.getMainHandItem()).flatMap(
                        transmitter -> RemoteControllableSearch.byBindId(player.getLevel(), player.getCamera().position(), transmitter.getBindId(), server.getPlayerList().getViewDistance() * 16)
                ).ifPresentOrElse(remoteControllableEntity -> {
                    if (remoteControllableEntity instanceof QuadcopterEntity quadcopter) {
                        player.setCamera(quadcopter);
                        quadcopter.getRigidBody().prioritize(player);
                    }
                }, () -> RemoteControllableSearch.allBeingViewed(server).stream().findFirst().ifPresent(player::setCamera));
            }
        });
    }

    public static void onPlayerViewRequestReceived(PacketRegistry.ServerboundContext context) {
        final var player = context.player();
        final var server = player.getServer();
        server.execute(() -> player.setCamera(player));
    }
}
