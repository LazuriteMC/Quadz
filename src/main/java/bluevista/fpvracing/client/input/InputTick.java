package bluevista.fpvracing.client.input;

import bluevista.fpvracing.client.ClientInitializer;
import bluevista.fpvracing.config.Config;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

import static org.lwjgl.glfw.GLFW.glfwGetJoystickAxes;

@Environment(EnvType.CLIENT)
public class InputTick {
    public static final String CONTROLLER_ID = "controllerID";
    public static final String THROTTLE = "throttle";
    public static final String PITCH = "pitch";
    public static final String YAW = "yaw";
    public static final String ROLL = "roll";
    public static final String DEADZONE = "deadzone";
    public static final String THROTTLE_CENTER_POSITION = "throttleCenterPosition";
    public static final String INVERT_THROTTLE = "invertThrottle";
    public static final String INVERT_PITCH = "invertPitch";
    public static final String INVERT_YAW = "invertYaw";
    public static final String INVERT_ROLL = "invertRoll";

    public static final AxisValues axisValues = new AxisValues();

    public static void tick() {
        MinecraftClient client = MinecraftClient.getInstance();
        Config config = ClientInitializer.getConfig();

        if (!client.isPaused() && controllerExists()) {
            int id = config.getInt(CONTROLLER_ID);
            axisValues.currT = glfwGetJoystickAxes(id).get(config.getInt(THROTTLE));
            axisValues.currT = glfwGetJoystickAxes(id).get(config.getInt(PITCH));
            axisValues.currT = glfwGetJoystickAxes(id).get(config.getInt(YAW));
            axisValues.currT = glfwGetJoystickAxes(id).get(config.getInt(ROLL));

            if (config.getBool(INVERT_THROTTLE)) {
                axisValues.currT *= -1;
            }

            if (config.getBool(THROTTLE_CENTER_POSITION)) {
                if (axisValues.currT < 0) {
                    axisValues.currT = 0;
                }
            } else if (config.getBool(THROTTLE_CENTER_POSITION)) {
                axisValues.currT = (axisValues.currT + 1) / 2.0f;
            }

            if (config.getBool(INVERT_PITCH)) {
                axisValues.currX *= -1;
            }

            if (config.getBool(INVERT_YAW)) {
                axisValues.currY *= -1;
            }

            if (config.getBool(INVERT_ROLL)) {
                axisValues.currZ *= -1;
            }

            if (config.getFloat(DEADZONE) != 0.0F) {
                float halfDeadzone = config.getFloat(DEADZONE) / 2.0f;

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
            glfwGetJoystickAxes(ClientInitializer.getConfig().getInt(CONTROLLER_ID)).get();
            return true;
        } catch (NullPointerException e) {
            return false;
        }
    }
}
