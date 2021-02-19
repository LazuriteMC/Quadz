package dev.lazurite.fpvracing.client.input.tick;

import dev.lazurite.fpvracing.FPVRacing;
import dev.lazurite.fpvracing.client.packet.InputFrameC2S;
import dev.lazurite.fpvracing.common.item.TransmitterItem;
import dev.lazurite.fpvracing.common.item.container.TransmitterContainer;
import dev.lazurite.fpvracing.common.util.type.Controllable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;

@Environment(EnvType.CLIENT)
public class TransmitterTick {
    public static void tick(MinecraftClient client) {
        if (client.world != null && client.player != null && !client.isPaused()) {
            if (client.player.getMainHandStack().getItem() instanceof TransmitterItem) {
                TransmitterContainer transmitter = FPVRacing.TRANSMITTER_CONTAINER.get(client.player.getMainHandStack());

                if (client.getCameraEntity() instanceof Controllable) {
                    Controllable controllable = (Controllable) client.getCameraEntity();

                    if (controllable.getBindId() == transmitter.getBindId()) {
                        InputFrameC2S.send(client.getCameraEntity(), controllable.getInputFrame()); // send controller input to server
                    }
                } else {
                    for (Entity entity : client.world.getEntities()) {
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
}
