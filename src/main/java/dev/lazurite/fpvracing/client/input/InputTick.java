package dev.lazurite.fpvracing.client.input;

import dev.lazurite.fpvracing.client.ClientInitializer;
import dev.lazurite.fpvracing.network.tracker.Config;
import dev.lazurite.fpvracing.server.ServerInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

import static org.lwjgl.glfw.GLFW.glfwGetJoystickAxes;

@Environment(EnvType.CLIENT)
public class InputTick {
    public static final Config.Key<Integer> CONTROLLER_ID = new Config.Key<>("controllerID", ServerInitializer.INTEGER_TYPE);

    public static final Config.Key<Integer> THROTTLE = new Config.Key<>("throttle", ServerInitializer.INTEGER_TYPE);
    public static final Config.Key<Integer> PITCH = new Config.Key<>("pitch", ServerInitializer.INTEGER_TYPE);
    public static final Config.Key<Integer> YAW = new Config.Key<>("yaw", ServerInitializer.INTEGER_TYPE);
    public static final Config.Key<Integer> ROLL = new Config.Key<>("roll", ServerInitializer.INTEGER_TYPE);
    public static final Config.Key<Float> DEADZONE = new Config.Key<>("deadzone", ServerInitializer.FLOAT_TYPE);

    public static final Config.Key<Boolean> THROTTLE_CENTER_POSITION = new Config.Key<>("throttleCenterPosition", ServerInitializer.BOOLEAN_TYPE);
    public static final Config.Key<Boolean> INVERT_THROTTLE = new Config.Key<>("invertThrottle", ServerInitializer.BOOLEAN_TYPE);
    public static final Config.Key<Boolean> INVERT_PITCH = new Config.Key<>("invertPitch", ServerInitializer.BOOLEAN_TYPE);
    public static final Config.Key<Boolean> INVERT_YAW = new Config.Key<>("invertYaw", ServerInitializer.BOOLEAN_TYPE);
    public static final Config.Key<Boolean> INVERT_ROLL = new Config.Key<>("invertRoll", ServerInitializer.BOOLEAN_TYPE);

    public static final AxisValues axisValues = new AxisValues();

    public static void tick() {
        MinecraftClient client = MinecraftClient.getInstance();
        Config config = ClientInitializer.getConfig();

        if (!client.isPaused() && controllerExists()) {
            axisValues.currT = glfwGetJoystickAxes(config.getValue(CONTROLLER_ID)).get(config.getValue(THROTTLE));
            axisValues.currX = glfwGetJoystickAxes(config.getValue(CONTROLLER_ID)).get(config.getValue(PITCH));
            axisValues.currY = glfwGetJoystickAxes(config.getValue(CONTROLLER_ID)).get(config.getValue(YAW));
            axisValues.currZ = glfwGetJoystickAxes(config.getValue(CONTROLLER_ID)).get(config.getValue(ROLL));

            if (config.getValue(INVERT_THROTTLE)) {
                axisValues.currT *= -1;
            }

            if (config.getValue(THROTTLE_CENTER_POSITION)) {
                if (axisValues.currT < 0) {
                    axisValues.currT = 0;
                }
            } else {
                axisValues.currT = (axisValues.currT + 1) / 2.0f;
            }

            if (config.getValue(INVERT_PITCH)) {
                axisValues.currX *= -1;
            }

            if (config.getValue(INVERT_YAW)) {
                axisValues.currY *= -1;
            }

            if (config.getValue(INVERT_ROLL)) {
                axisValues.currZ *= -1;
            }

            if (config.getValue(DEADZONE) != 0.0F) {
                float halfDeadzone = config.getValue(DEADZONE) / 2.0f;

                if (axisValues.currX < halfDeadzone && axisValues.currX > -halfDeadzone) {
                    axisValues.currX = 0.0f;
                }

                if (axisValues.currY < halfDeadzone && axisValues.currY > -halfDeadzone) {
                    axisValues.currY = 0.0f;
                }

                if (axisValues.currZ < halfDeadzone && axisValues.currZ > -halfDeadzone) {
                    axisValues.currZ = 0.0f;
                }
            }
        }
    }

    public static boolean controllerExists() {
        try {
            glfwGetJoystickAxes(ClientInitializer.getConfig().getValue(CONTROLLER_ID)).get();
            return true;
        } catch (NullPointerException e) {
            return false;
        }
    }
}
