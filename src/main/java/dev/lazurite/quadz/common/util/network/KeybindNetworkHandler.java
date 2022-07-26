package dev.lazurite.quadz.common.util.network;

import dev.lazurite.quadz.common.data.model.Bindable;
import dev.lazurite.quadz.common.entity.QuadcopterEntity;
import dev.lazurite.quadz.common.entity.RemoteControllableEntity;
import dev.lazurite.quadz.common.util.tools.RemoteControllableSearch;
import dev.lazurite.toolbox.api.network.PacketRegistry;
import net.minecraft.network.chat.Component;

public class KeybindNetworkHandler {
    public static void onNoClipKey(PacketRegistry.ServerboundContext context) {
        final var player = context.player();
        final var server = player.getServer();

        server.execute(() ->
            Bindable.get(player.getMainHandItem()).flatMap(transmitter -> RemoteControllableSearch.byBindId(player.getLevel(), player.getCamera().position(), transmitter.getBindId(), server.getPlayerList().getViewDistance())).ifPresent(remoteControllableEntity -> {
                // Quadz specific
                if (remoteControllableEntity instanceof QuadcopterEntity quadcopter) {
                    boolean lastNoClip = quadcopter.getRigidBody().terrainLoadingEnabled();
                    quadcopter.getRigidBody().setTerrainLoadingEnabled(!lastNoClip);

                    if (lastNoClip) {
                        player.displayClientMessage(Component.translatable("message.quadz.noclip_on"), true);
                    } else {
                        player.displayClientMessage(Component.translatable("message.quadz.noclip_off"), true);
                    }
                }
            })
        );
    }

    public static void onChangeCameraAngleKey(PacketRegistry.ServerboundContext context) {
        final var player = context.player();
        final var server = player.getServer();
        final var buf = context.byteBuf();

        final var amount = buf.readInt();

        server.execute(() ->
            Bindable.get(player.getMainHandItem()).ifPresent(transmitter -> {
                if (player.getCamera() instanceof RemoteControllableEntity remoteControllableEntity) {
                    if (remoteControllableEntity.isBoundTo(transmitter)) {
                        remoteControllableEntity.setCameraAngle(remoteControllableEntity.getCameraAngle() + amount);
                    }
                } else {
                    RemoteControllableSearch.byBindId(player.getLevel(), player.getCamera().position(), transmitter.getBindId(), server.getPlayerList().getViewDistance())
                            .ifPresent(quadcopter -> quadcopter.setCameraAngle(quadcopter.getCameraAngle() + amount));
                }
            })
        );
    }
}
