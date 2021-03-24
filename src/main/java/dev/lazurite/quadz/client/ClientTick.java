package dev.lazurite.quadz.client;

import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.client.input.frame.InputFrame;
import dev.lazurite.quadz.client.input.frame.InputFrameC2S;
import dev.lazurite.quadz.client.input.InputTick;
import dev.lazurite.quadz.common.entity.QuadcopterEntity;
import dev.lazurite.quadz.common.item.TransmitterItem;
import dev.lazurite.quadz.common.item.container.TransmitterContainer;
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
 * @see InputFrameC2S
 */
@Environment(EnvType.CLIENT)
public class ClientTick {
    public static void tick(MinecraftClient client) {
        if (client.player != null && client.world != null && !client.isPaused()) {
            if (client.player.getMainHandStack().getItem() instanceof TransmitterItem) {
                TransmitterContainer transmitter = Quadz.TRANSMITTER_CONTAINER.get(client.player.getMainHandStack());

                if (client.getCameraEntity() instanceof QuadcopterEntity) {
                    QuadcopterEntity quadcopter = (QuadcopterEntity) client.getCameraEntity();

                    if (quadcopter.getBindId() == transmitter.getBindId()) {
                        /* Get a new InputFrame from InputTick. */
                        quadcopter.setInputFrame(InputTick.getInstance().getInputFrame());

                        /* Send that InputFrame to the server. */
                        InputFrameC2S.send(client.getCameraEntity(), quadcopter.getInputFrame());
                    }
                } else {
                    for (Entity entity : client.world.getEntities()) {
                        if (entity instanceof QuadcopterEntity) {
                            QuadcopterEntity quadcopter = (QuadcopterEntity) entity;

                            if (quadcopter.getBindId() == transmitter.getBindId()) {
                                /* Get a new InputFrame from InputTick. */
                                quadcopter.setInputFrame(InputTick.getInstance().getInputFrame());

                                /* Send that InputFrame to the server. */
                                InputFrameC2S.send(client.getCameraEntity(), quadcopter.getInputFrame());
                                break;
                            }
                        }
                    }
                }
            }
        }
    }
}
