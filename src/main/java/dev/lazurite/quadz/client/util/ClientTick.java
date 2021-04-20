package dev.lazurite.quadz.client.util;

import dev.lazurite.quadz.common.state.Bindable;
import dev.lazurite.quadz.common.state.QuadcopterState;
import dev.lazurite.quadz.common.util.input.InputFrame;
import dev.lazurite.quadz.client.input.InputTick;
import dev.lazurite.quadz.common.state.entity.QuadcopterEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;

/**
 * This class is responsible for transmitting the player's controller input
 * information to the server each tick. It will send input data from either
 * the player's camera entity or whatever entity they happen to be controlling
 * within range.
 * @see InputTick
 * @see InputFrame
 */
@Environment(EnvType.CLIENT)
public class ClientTick {
    public static int desiredCameraEntity = -1;

    public static void tick(MinecraftClient client) {
        if (client.player != null && client.world != null && !client.isPaused()) {
            if (desiredCameraEntity != -1) {
                Entity entity = client.world.getEntityById(desiredCameraEntity);

                if (entity != null) {
                    client.setCameraEntity(entity);
                    desiredCameraEntity = -1;
                }
            }

            Bindable.get(client.player.getMainHandStack()).ifPresent(transmitter -> {
                QuadcopterEntity quadcopter = QuadcopterState.getQuadcopterByBindId(client.world, client.player.getPos(), transmitter.getBindId(), (int) client.gameRenderer.getViewDistance());

                if (quadcopter != null) {
                    quadcopter.getInputFrame().set(InputTick.getInstance().getInputFrame());
                    quadcopter.sendInputFrame();
                }
            });
        }
    }
}
