package dev.lazurite.quadz.common.hooks;

import dev.lazurite.quadz.common.util.Bindable;
import dev.lazurite.quadz.common.util.Search;
import dev.lazurite.quadz.common.util.event.CameraEvents;
import dev.lazurite.quadz.common.entity.Quadcopter;
import dev.lazurite.toolbox.api.network.PacketRegistry;

import java.util.ArrayList;
import java.util.Optional;

public class ServerNetworkEventHooks {

    public static void onJoystickInput(PacketRegistry.ServerboundContext context) {
        var player = context.player();
        var buf = context.byteBuf();
        var axisCount = buf.readInt();

        for (int i = 0; i < axisCount; i++) {
            var axis = buf.readResourceLocation();
            var value = buf.readFloat();
            player.setJoystickValue(axis, value);
        }
    }

    public static void onQuadcopterViewRequested(PacketRegistry.ServerboundContext context) {
        var player = context.player();
        var buf = context.byteBuf();
        var spectateDirection = buf.readInt();

        Optional.ofNullable(player.getServer()).ifPresent(server -> {
            server.execute(() -> {
                if (player.getCamera() instanceof Quadcopter quadcopter) {
                    var allQuadcopters = new ArrayList<>(Search.forAllViewed(server));
                    var index = Math.max(allQuadcopters.lastIndexOf(quadcopter) + spectateDirection, 0);
                    var entity = allQuadcopters.get(index % allQuadcopters.size());
                    player.setCamera(entity);
                    CameraEvents.SWITCH_CAMERA_EVENT.invoke(player.getCamera(), entity);
                } else {
                    Bindable.get(player.getMainHandItem()).ifPresent(bindable -> {
                        Search.forQuadWithBindId(
                                        player.getLevel(),
                                        player.getCamera().position(),
                                        bindable.getBindId(),
                                        server.getPlayerList().getViewDistance() * 16)
                                .ifPresentOrElse(entity -> {
                                    player.setCamera(entity);
                                    CameraEvents.SWITCH_CAMERA_EVENT.invoke(player.getCamera(), entity);
                                }, () -> Search.forAllViewed(server).stream().findFirst().ifPresent(entity -> {
                                    player.setCamera(entity);
                                    CameraEvents.SWITCH_CAMERA_EVENT.invoke(player.getCamera(), entity);
                                }));
                    });

                }
            });
        });
    }

    public static void onPlayerViewRequestReceived(PacketRegistry.ServerboundContext context) {
        var player = context.player();
        Optional.ofNullable(player.getServer()).ifPresent(server -> server.execute(() -> player.setCamera(player)));
    }

}
