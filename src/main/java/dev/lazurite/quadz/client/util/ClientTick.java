package dev.lazurite.quadz.client.util;

import dev.lazurite.quadz.client.Config;
import dev.lazurite.quadz.common.item.QuadcopterItem;
import dev.lazurite.quadz.common.state.Bindable;
import dev.lazurite.quadz.common.state.QuadcopterState;
import dev.lazurite.quadz.common.state.entity.QuadcopterEntity;
import dev.lazurite.quadz.common.util.input.InputFrame;
import dev.lazurite.quadz.client.input.InputTick;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;

import java.util.Optional;

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
    public static boolean isUsingKeyboard = false;

    public static void tick(MinecraftClient client) {
        if (client.player != null && client.world != null && !client.isPaused()) {
            if (desiredCameraEntity != -1) {
                Entity entity = client.world.getEntityById(desiredCameraEntity);

                if (entity != null) {
                    client.setCameraEntity(entity);
                    desiredCameraEntity = -1;
                }
            }

            isUsingKeyboard = false;

            Bindable.get(client.player.getMainHandStack()).ifPresent(transmitter -> {
                Optional<QuadcopterEntity> optionalQuad = QuadcopterState.getQuadcopterByBindId(client.world, client.player.getPos(), transmitter.getBindId(), (int) client.gameRenderer.getViewDistance());

                if (client.getCameraEntity() instanceof QuadcopterEntity && ((QuadcopterEntity) client.getCameraEntity()).isBoundTo(transmitter)) {
                    InputTick.getInstance().tickKeyboard(client);
                    QuadcopterEntity entity = (QuadcopterEntity) client.getCameraEntity();
                    entity.getInputFrame().set(InputTick.getInstance().getInputFrame());
                    entity.sendInputFrame();
                } else if (optionalQuad.isPresent()) {
                    if (Config.getInstance().followLOS) {
                        InputTick.getInstance().tickKeyboard(client);
                    } else if (Config.getInstance().controllerId == -1) {
                        optionalQuad.get().getInputFrame().set(new InputFrame());
                    } else {
                        optionalQuad.get().getInputFrame().set(InputTick.getInstance().getInputFrame());
                    }

                    optionalQuad.get().sendInputFrame();
                }
            });
        }
    }
}
