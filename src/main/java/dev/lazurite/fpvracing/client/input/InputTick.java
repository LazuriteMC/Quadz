package dev.lazurite.fpvracing.client.input;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import dev.lazurite.fpvracing.api.event.JoystickEvents;
import dev.lazurite.fpvracing.client.config.Config;
import dev.lazurite.fpvracing.common.util.BetaflightHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.nio.FloatBuffer;
import java.util.List;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;

/**
 * This class is responsible for polling the user's controller each
 * time the class is ticked. It dynamically values stored in {@link Config}.
 * @see Config
 */
@Environment(EnvType.CLIENT)
public final class InputTick {
    public static final InputTick INSTANCE = new InputTick();
    private final InputFrame frame = new InputFrame();
    private final Map<Integer, String> joysticks = Maps.newHashMap();
    private long next;

    private InputTick() {
    }

    public void tick() {
        if (System.currentTimeMillis() > next) {
            Map<Integer, String> lastJoysticks = Maps.newHashMap(joysticks);
            this.joysticks.clear();

            System.out.println(lastJoysticks);
            for (int i = 0; i < 16; i++) {
                if (glfwJoystickPresent(i)) {
                    joysticks.put(i, getJoystickName(i));

                    if (!lastJoysticks.containsKey(i)) {
                        JoystickEvents.JOYSTICK_CONNECT.invoker().onConnect(i, getJoystickName(i));
                    }
                } else if (lastJoysticks.containsKey(i)) {
                    JoystickEvents.JOYSTICK_DISCONNECT.invoker().onDisconnect(i, lastJoysticks.get(i));
                }
            }

            next = System.currentTimeMillis() + 500;
        }

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

    public List<String> getJoysticks() {
        return Lists.newArrayList(joysticks.values());
    }

    public InputFrame getInputFrame(float delta) {
        InputFrame out = new InputFrame(frame);
        out.setPitch((float) BetaflightHelper.calculateRates(out.getPitch(), Config.INSTANCE.rate, Config.INSTANCE.expo, Config.INSTANCE.superRate, delta));
        out.setYaw((float) BetaflightHelper.calculateRates(out.getYaw(), Config.INSTANCE.rate, Config.INSTANCE.expo, Config.INSTANCE.superRate, delta));
        out.setRoll((float) BetaflightHelper.calculateRates(out.getRoll(), Config.INSTANCE.rate, Config.INSTANCE.expo, Config.INSTANCE.superRate, delta));
        return out;
    }

    public static String getJoystickName(int id) {
        if (glfwJoystickPresent(id)) {
            return glfwGetJoystickName(id);
        }

        return null;
    }

    public static boolean controllerExists() {
        return glfwJoystickPresent(Config.INSTANCE.controllerId);
    }
}
