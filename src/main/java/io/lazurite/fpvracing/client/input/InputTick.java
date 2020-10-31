package io.lazurite.fpvracing.client.input;

import io.lazurite.fpvracing.client.ClientInitializer;
import io.lazurite.fpvracing.network.tracker.Config;
import io.lazurite.fpvracing.network.tracker.generic.GenericType;
import io.lazurite.fpvracing.server.ServerInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Pair;

import static org.lwjgl.glfw.GLFW.glfwGetJoystickAxes;

@Environment(EnvType.CLIENT)
public class InputTick {
    public static final Pair<String, GenericType<Integer>> CONTROLLER_ID = new Pair<>("controllerID", ServerInitializer.INTEGER_TYPE);

    public static final Pair<String, GenericType<Integer>> THROTTLE = new Pair<>("throttle", ServerInitializer.INTEGER_TYPE);
    public static final Pair<String, GenericType<Integer>> PITCH = new Pair<>("pitch", ServerInitializer.INTEGER_TYPE);
    public static final Pair<String, GenericType<Integer>> YAW = new Pair<>("yaw", ServerInitializer.INTEGER_TYPE);
    public static final Pair<String, GenericType<Integer>> ROLL = new Pair<>("roll", ServerInitializer.INTEGER_TYPE);
    public static final Pair<String, GenericType<Float>> DEADZONE = new Pair<>("deadzone", ServerInitializer.FLOAT_TYPE);

    public static final Pair<String, GenericType<Boolean>> THROTTLE_CENTER_POSITION = new Pair<>("throttleCenterPosition", ServerInitializer.BOOLEAN_TYPE);
    public static final Pair<String, GenericType<Boolean>> INVERT_THROTTLE = new Pair<>("invertThrottle", ServerInitializer.BOOLEAN_TYPE);
    public static final Pair<String, GenericType<Boolean>> INVERT_PITCH = new Pair<>("invertPitch", ServerInitializer.BOOLEAN_TYPE);
    public static final Pair<String, GenericType<Boolean>> INVERT_YAW = new Pair<>("invertYaw", ServerInitializer.BOOLEAN_TYPE);
    public static final Pair<String, GenericType<Boolean>> INVERT_ROLL = new Pair<>("invertRoll", ServerInitializer.BOOLEAN_TYPE);

    public static final AxisValues axisValues = new AxisValues();

    public static void tick() {
        MinecraftClient client = MinecraftClient.getInstance();
        Config config = ClientInitializer.getConfig();

        if (!client.isPaused() && controllerExists()) {
            boolean throttleCenterPosition = THROTTLE_CENTER_POSITION.getRight().fromConfig(config, THROTTLE_CENTER_POSITION.getLeft());
            boolean invertPitch = INVERT_PITCH.getRight().fromConfig(config, INVERT_PITCH.getLeft());
            boolean invertYaw = INVERT_YAW.getRight().fromConfig(config, INVERT_YAW.getLeft());
            boolean invertRoll = INVERT_ROLL.getRight().fromConfig(config, INVERT_ROLL.getLeft());

            float deadzone = DEADZONE.getRight().fromConfig(config, DEADZONE.getLeft());

            int id = CONTROLLER_ID.getRight().fromConfig(config, CONTROLLER_ID.getLeft());
            int throttle = THROTTLE.getRight().fromConfig(config, THROTTLE.getLeft());
            int pitch = PITCH.getRight().fromConfig(config, PITCH.getLeft());
            int yaw = YAW.getRight().fromConfig(config, YAW.getLeft());
            int roll = ROLL.getRight().fromConfig(config, ROLL.getLeft());

            axisValues.currT = glfwGetJoystickAxes(id).get(throttle);
            axisValues.currX = glfwGetJoystickAxes(id).get(pitch);
            axisValues.currY = glfwGetJoystickAxes(id).get(yaw);
            axisValues.currZ = glfwGetJoystickAxes(id).get(roll);

            if (INVERT_THROTTLE.getRight().fromConfig(config, INVERT_THROTTLE.getLeft())) {
                axisValues.currT *= -1;
            }

            if (throttleCenterPosition) {
                if (axisValues.currT < 0) {
                    axisValues.currT = 0;
                }
            } else {
                axisValues.currT = (axisValues.currT + 1) / 2.0f;
            }

            if (invertPitch) {
                axisValues.currX *= -1;
            }

            if (invertYaw) {
                axisValues.currY *= -1;
            }

            if (invertRoll) {
                axisValues.currZ *= -1;
            }

            if (deadzone != 0.0F) {
                float halfDeadzone = deadzone / 2.0f;

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
            glfwGetJoystickAxes(CONTROLLER_ID.getRight().fromConfig(ClientInitializer.getConfig(), CONTROLLER_ID.getLeft())).get();
            return true;
        } catch (NullPointerException e) {
            return false;
        }
    }
}
