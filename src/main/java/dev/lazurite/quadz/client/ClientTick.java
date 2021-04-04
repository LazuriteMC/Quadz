package dev.lazurite.quadz.client;

import dev.lazurite.quadz.common.state.Bindable;
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
    public static void tick(MinecraftClient client) {
        if (client.player != null && client.world != null && !client.isPaused()) {
            Bindable.get(client.player.getMainHandStack()).ifPresent(transmitter -> {
                for (Entity entity : client.world.getEntities()) {
                    if (entity instanceof QuadcopterEntity) {
                        QuadcopterEntity quadcopter = (QuadcopterEntity) entity;

                        if (transmitter.isBoundTo(quadcopter)) {
                            /* Get a new InputFrame from InputTick. */
                            quadcopter.getInputFrame().set(InputTick.getInstance().getInputFrame());

                            /* Send that InputFrame to the server. */
                            quadcopter.sendInputFrame();
                            break;
                        }
                    }
                }
            });
        }
    }
}
