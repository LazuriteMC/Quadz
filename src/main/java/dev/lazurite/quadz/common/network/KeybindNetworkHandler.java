package dev.lazurite.quadz.common.network;

import dev.lazurite.quadz.common.bindable.Bindable;
import dev.lazurite.quadz.common.quadcopter.Quadcopter;
import dev.lazurite.quadz.common.quadcopter.entity.QuadcopterEntity;
import dev.lazurite.toolbox.api.network.PacketRegistry;
import net.minecraft.network.chat.Component;

public class KeybindNetworkHandler {
    public static void onNoClipKey(PacketRegistry.ServerboundContext context) {
        final var player = context.player();
        final var server = player.getServer();

        server.execute(() ->
            Bindable.get(player.getMainHandItem()).flatMap(transmitter -> Quadcopter.getQuadcopterByBindId(player.getLevel(), player.getCamera().position(), transmitter.getBindId(), server.getPlayerList().getViewDistance())).ifPresent(quadcopter -> {
                boolean lastNoClip = quadcopter.getRigidBody().terrainLoadingEnabled();
                quadcopter.getRigidBody().setTerrainLoadingEnabled(!lastNoClip);

                if (lastNoClip) {
                    player.displayClientMessage(Component.translatable("message.quadz.noclip_on"), true);
                } else {
                    player.displayClientMessage(Component.translatable("message.quadz.noclip_off"), true);
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
                if (player.getCamera() instanceof QuadcopterEntity quadcopter) {
                    if (quadcopter.isBoundTo(transmitter)) {
                        quadcopter.setCameraAngle(quadcopter.getCameraAngle() + amount);
                    }
                } else {
                    Quadcopter.getQuadcopterByBindId(player.getLevel(), player.getCamera().position(), transmitter.getBindId(), server.getPlayerList().getViewDistance())
                            .ifPresent(quadcopter -> quadcopter.setCameraAngle(quadcopter.getCameraAngle() + amount));
                }
            })
        );
    }
}
