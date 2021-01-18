package dev.lazurite.fpvracing.client.input;

import dev.lazurite.fpvracing.client.config.Config;
import dev.lazurite.fpvracing.common.util.BetaflightHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.nio.FloatBuffer;

import static org.lwjgl.glfw.GLFW.glfwGetJoystickAxes;

/**
 * This class is responsible for polling the user's controller each
 * time the class is ticked. It dynamically values stored in {@lin Config}.
 * @see Config
 */
@Environment(EnvType.CLIENT)
public final class InputTick {
    public static final InputTick INSTANCE = new InputTick();
    private final InputFrame frame = new InputFrame();

    private InputTick() {
    }

    public void tick() {
        if (controllerExists()) {
            FloatBuffer buffer = glfwGetJoystickAxes(Config.INSTANCE.controllerId);
            frame.set(buffer.get(Config.INSTANCE.throttle), buffer.get(Config.INSTANCE.pitch), buffer.get(Config.INSTANCE.roll), buffer.get(Config.INSTANCE.yaw));

            if (Config.INSTANCE.invertThrottle) {
                frame.setThrottle(-frame.getThrottle());
            }

            if (Config.INSTANCE.invertPitch) {
                frame.setPitch(-frame.getPitch());
            }

            if (Config.INSTANCE.invertRoll) {
                frame.setRoll(-frame.getRoll());
            }

            if (Config.INSTANCE.invertYaw) {
                frame.setYaw(-frame.getYaw());
            }

            if (Config.INSTANCE.throttleInCenter) {
                if (frame.getThrottle() < 0) {
                    frame.setThrottle(0);
                }
            } else {
                frame.setThrottle((frame.getThrottle() + 1) / 2.0f);
            }

            if (Config.INSTANCE.deadzone != 0.0f) {
                float halfDeadzone = Config.INSTANCE.deadzone / 2.0f;

                if (frame.getPitch() < halfDeadzone && frame.getPitch() > -halfDeadzone) {
                    frame.setPitch(0);
                }

                if (frame.getYaw() < halfDeadzone && frame.getYaw() > -halfDeadzone) {
                    frame.setYaw(0);
                }

                if (frame.getRoll() < halfDeadzone && frame.getRoll() > -halfDeadzone) {
                    frame.setRoll(0);
                }
            }
        }
    }

    public InputFrame getInputFrame(float delta) {
        InputFrame out = new InputFrame(frame);
        out.setPitch((float) BetaflightHelper.calculateRates(out.getPitch(), Config.INSTANCE.rate, Config.INSTANCE.expo, Config.INSTANCE.superRate, delta));
        out.setYaw((float) BetaflightHelper.calculateRates(out.getYaw(), Config.INSTANCE.rate, Config.INSTANCE.expo, Config.INSTANCE.superRate, delta));
        out.setRoll((float) BetaflightHelper.calculateRates(out.getRoll(), Config.INSTANCE.rate, Config.INSTANCE.expo, Config.INSTANCE.superRate, delta));
        return out;
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
