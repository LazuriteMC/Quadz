package dev.lazurite.quadz.common.network;

import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.client.input.Mode;
import dev.lazurite.quadz.common.data.template.TemplateLoader;
import dev.lazurite.quadz.common.data.template.model.Template;
import dev.lazurite.quadz.common.bindable.Bindable;
import dev.lazurite.quadz.common.quadcopter.Quadcopter;
import dev.lazurite.quadz.common.quadcopter.entity.QuadcopterEntity;
import dev.lazurite.quadz.common.util.PlayerData;
import dev.lazurite.quadz.common.util.input.InputFrame;
import dev.lazurite.toolbox.api.network.PacketRegistry;
import dev.lazurite.toolbox.api.network.ServerNetworking;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;

public class CommonNetworkHandler {
    public static void onQuadcopterSettingsReceived(PacketRegistry.ServerboundContext context) {
        final var player = context.player();
        final var server = player.getServer();
        final var buf = context.byteBuf();

        final var entityId = buf.readInt();
        final var cameraAngle = buf.readInt();

        server.execute(() -> {
            final var entity = player.getLevel().getEntity(entityId);

            if (entity instanceof QuadcopterEntity) {
                ((QuadcopterEntity) entity).setCameraAngle(cameraAngle);
            }
        });
    }

    public static void onPlayerDataReceived(PacketRegistry.ServerboundContext context) {
        final var player = context.player();
        final var server = player.getServer();
        final var buf = context.byteBuf();

        final var callSign = buf.readUtf(32767);
        server.execute(() -> ((PlayerData) player).setCallSign(callSign));
    }

    public static void onTemplateReceived(PacketRegistry.ServerboundContext context) {
        final var player = context.player();
        final var server = player.getServer();
        final var buf = context.byteBuf();

        final var template = Template.deserialize(buf);

        server.execute(() -> {
            PlayerLookup.all(server).forEach(p -> {
                if (!p.equals(player)) {
                    ServerNetworking.send(p, Quadz.TEMPLATE, template::serialize);
                }
            });

            TemplateLoader.load(template);
        });
    }

    public static void onInputFrame(PacketRegistry.ServerboundContext context) {
        final var player = context.player();
        final var server = player.getServer();
        final var buf = context.byteBuf();

        final var entityId = buf.readInt();
        final var frame = new InputFrame(
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readEnum(Mode.class));

        server.execute(() -> {
            Bindable.get(player.getMainHandItem()).ifPresent(transmitter -> {
                final var entity = player.getLevel().getEntity(entityId);

                if (entity instanceof QuadcopterEntity quadcopterEntity) {
                    if (quadcopterEntity.isBoundTo(transmitter)) {
                        quadcopterEntity.getInputFrame().set(frame);
                    }
                }
            });
        });
    }

    public static void onQuadcopterViewRequestReceived(PacketRegistry.ServerboundContext context) {
        final var player = context.player();
        final var server = player.getServer();
        final var buf = context.byteBuf();
        final var spectateDirection = buf.readInt();

        server.execute(() -> {
            if (player.getCamera() instanceof QuadcopterEntity quadcopter) {
                final var allQuads = Quadcopter.getAllViewing(server);
                final var index = Math.max(allQuads.lastIndexOf(quadcopter) + spectateDirection, 0);
                player.setCamera(allQuads.get(index % allQuads.size()));
            } else {
                Bindable.get(player.getMainHandItem()).flatMap(
                        transmitter -> Quadcopter.getQuadcopterByBindId(player.getLevel(), player.getCamera().position(), transmitter.getBindId(), server.getPlayerList().getViewDistance() * 16)
                ).ifPresentOrElse(quadcopter -> {
                    player.setCamera(quadcopter);
                    quadcopter.getRigidBody().prioritize(player);
                }, () -> Quadcopter.getAllViewing(server).stream().findFirst().ifPresent(player::setCamera));
            }
        });
    }

    public static void onPlayerViewRequestReceived(PacketRegistry.ServerboundContext context) {
        final var player = context.player();
        final var server = player.getServer();
        server.execute(() -> player.setCamera(player));
    }
}
