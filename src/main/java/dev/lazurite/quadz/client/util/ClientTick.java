package dev.lazurite.quadz.client.util;

import dev.lazurite.quadz.client.Config;
import dev.lazurite.quadz.common.bindable.Bindable;
import dev.lazurite.quadz.common.quadcopter.Quadcopter;
import dev.lazurite.quadz.common.quadcopter.entity.QuadcopterEntity;
import dev.lazurite.quadz.common.util.input.InputFrame;
import dev.lazurite.quadz.client.input.InputTick;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

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
    public static boolean isUsingKeyboard = false;

    public static void tickInput(ClientLevel level) {
        final var client = Minecraft.getInstance();

        if (!client.isPaused()) {
            isUsingKeyboard = false;

            if (client.options.keyShift.isDown() && client.cameraEntity instanceof QuadcopterEntity) {
                ((CameraTypeAccess) (Object) client.options.getCameraType()).reset();
            }

            Bindable.get(client.player.getMainHandItem()).ifPresent(transmitter -> {
                if (client.getCameraEntity() instanceof QuadcopterEntity quadcopter && quadcopter.isBoundTo(transmitter)) {
                    InputTick.getInstance().tickKeyboard(client);
                    quadcopter.getInputFrame().set(InputTick.getInstance().getInputFrame());
                    quadcopter.sendInputFrame();
                } else {
                    Quadcopter.getQuadcopterByBindId(
                            level,
                            client.cameraEntity.position(),
                            transmitter.getBindId(),
                            (int) client.gameRenderer.getRenderDistance())
                    .ifPresent(quadcopter -> {
                        if (Config.getInstance().followLOS) {
                            InputTick.getInstance().tickKeyboard(client);
                        }

                        quadcopter.getInputFrame().set(
                                Config.getInstance().controllerId == -1 ?
                                        new InputFrame() :
                                        InputTick.getInstance().getInputFrame());
                        quadcopter.sendInputFrame();
                    });
                }
            });
        }
    }
}
