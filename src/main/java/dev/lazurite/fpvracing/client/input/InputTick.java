package dev.lazurite.fpvracing.client.input;

import dev.lazurite.fpvracing.client.config.Config;
import dev.lazurite.fpvracing.common.util.BetaflightHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.nio.FloatBuffer;

import static org.lwjgl.glfw.GLFW.glfwGetJoystickAxes;

/**
 * This class is responsible for polling the user's controller each
 * time the class is ticked. It dynamically values stored in {@link Config}.
 * @see Config
 */
@Environment(EnvType.CLIENT)
public final class InputTick {
    public static final InputTick INSTANCE = new InputTick();
    private final InputFrame frame = new InputFrame();

    private InputTick() {
    }

    public void tick(float delta) {
        if (controllerExists()) {
            FloatBuffer buffer = glfwGetJoystickAxes(Config.INSTANCE.controllerId);
            frame.set(buffer.get(Config.INSTANCE.throttle), buffer.get(Config.INSTANCE.pitch), buffer.get(Config.INSTANCE.roll), buffer.get(Config.INSTANCE.yaw));

            if (Config.INSTANCE.invertThrottle) {
                frame.setT(-frame.getT());
            }

            if (Config.INSTANCE.invertPitch) {
                frame.setX(-frame.getX());
            }

            if (Config.INSTANCE.invertRoll) {
                frame.setZ(-frame.getZ());
            }

            if (Config.INSTANCE.invertYaw) {
                frame.setY(-frame.getY());
            }

            if (Config.INSTANCE.throttleInCenter) {
                if (frame.getT() < 0) {
                    frame.setT(0);
                }
            } else {
                frame.setT((frame.getT() + 1) / 2.0f);
            }

            if (Config.INSTANCE.deadzone != 0.0f) {
                float halfDeadzone = Config.INSTANCE.deadzone / 2.0f;

                if (frame.getX() < halfDeadzone && frame.getX() > -halfDeadzone) {
                    frame.setX(0);
                }

                if (frame.getY() < halfDeadzone && frame.getY() > -halfDeadzone) {
                    frame.setY(0);
                }

                if (frame.getZ() < halfDeadzone && frame.getZ() > -halfDeadzone) {
                    frame.setZ(0);
                }
            }

            frame.setX((float) BetaflightHelper.calculateRates(getInputFrame().getX(), Config.INSTANCE.rate, Config.INSTANCE.expo, Config.INSTANCE.superRate, delta));
            frame.setY((float) BetaflightHelper.calculateRates(getInputFrame().getY(), Config.INSTANCE.rate, Config.INSTANCE.expo, Config.INSTANCE.superRate, delta));
            frame.setZ((float) BetaflightHelper.calculateRates(getInputFrame().getZ(), Config.INSTANCE.rate, Config.INSTANCE.expo, Config.INSTANCE.superRate, delta));
        }
    }

    public InputFrame getInputFrame() {
        return this.frame;
    }

    public static boolean controllerExists() {
        try {
            glfwGetJoystickAxes(Config.INSTANCE.controllerId).get();
            return true;
        } catch (NullPointerException e) {
            return false;
        }
    }
}
