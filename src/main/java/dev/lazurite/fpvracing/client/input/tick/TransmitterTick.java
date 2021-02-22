package dev.lazurite.fpvracing.client.input.tick;

import dev.lazurite.fpvracing.FPVRacing;
import dev.lazurite.fpvracing.client.input.frame.InputFrame;
import dev.lazurite.fpvracing.client.input.frame.InputFrameC2S;
import dev.lazurite.fpvracing.common.item.TransmitterItem;
import dev.lazurite.fpvracing.common.item.container.TransmitterContainer;
import dev.lazurite.fpvracing.common.util.type.Controllable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
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
public class TransmitterTick {
    public static void tick(ClientWorld world) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player != null && !client.isPaused()) {
            if (client.player.getMainHandStack().getItem() instanceof TransmitterItem) {
                TransmitterContainer transmitter = FPVRacing.TRANSMITTER_CONTAINER.get(client.player.getMainHandStack());

                if (client.getCameraEntity() instanceof Controllable) {
                    Controllable controllable = (Controllable) client.getCameraEntity();

                    if (controllable.getBindId() == transmitter.getBindId()) {
                        InputFrameC2S.send(client.getCameraEntity(), controllable.getInputFrame()); // send controller input to server
                    }
                }

                for (Entity entity : world.getEntities()) {
                    if (entity instanceof Controllable) {
                        Controllable controllable = (Controllable) entity;

                        if (controllable.getBindId() == transmitter.getBindId()) {
                            InputFrameC2S.send(entity, controllable.getInputFrame()); // send controller input to the server
                            break;
                        }
                    }
                }
            }
        }
    }
}
