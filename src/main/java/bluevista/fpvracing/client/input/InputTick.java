package bluevista.fpvracing.client.input;

import bluevista.fpvracing.client.ClientInitializer;
import bluevista.fpvracing.config.Config;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

import static org.lwjgl.glfw.GLFW.glfwGetJoystickAxes;

@Environment(EnvType.CLIENT)
public class InputTick {
    public static final AxisValues axisValues = new AxisValues();

    public static void tick() {
        MinecraftClient client = MinecraftClient.getInstance();

        if (!client.isPaused() && controllerExists()) {
            axisValues.currT = glfwGetJoystickAxes(ClientInitializer.getConfig().getIntOption(Config.CONTROLLER_ID)).get(ClientInitializer.getConfig().getIntOption(Config.THROTTLE));
            axisValues.currX = glfwGetJoystickAxes(ClientInitializer.getConfig().getIntOption(Config.CONTROLLER_ID)).get(ClientInitializer.getConfig().getIntOption(Config.PITCH));
            axisValues.currY = glfwGetJoystickAxes(ClientInitializer.getConfig().getIntOption(Config.CONTROLLER_ID)).get(ClientInitializer.getConfig().getIntOption(Config.YAW));
            axisValues.currZ = glfwGetJoystickAxes(ClientInitializer.getConfig().getIntOption(Config.CONTROLLER_ID)).get(ClientInitializer.getConfig().getIntOption(Config.ROLL));

            if (ClientInitializer.getConfig().getIntOption(Config.INVERT_THROTTLE) == 1) {
                axisValues.currT *= -1;
            }

            if (ClientInitializer.getConfig().getIntOption(Config.THROTTLE_CENTER_POSITION) == 1) {
                if (axisValues.currT < 0) {
                    axisValues.currT = 0;
                }
            } else if (ClientInitializer.getConfig().getIntOption(Config.THROTTLE_CENTER_POSITION) == 0) {
                axisValues.currT = (axisValues.currT + 1) / 2.0f;
            }

            if (ClientInitializer.getConfig().getIntOption(Config.INVERT_PITCH) == 1) {
                axisValues.currX *= -1;
            }

            if (ClientInitializer.getConfig().getIntOption(Config.INVERT_YAW) == 1) {
                axisValues.currY *= -1;
            }

            if (ClientInitializer.getConfig().getIntOption(Config.INVERT_ROLL) == 1) {
                axisValues.currZ *= -1;
            }

            if (ClientInitializer.getConfig().getFloatOption(Config.DEADZONE) != 0.0F) {
                float halfDeadzone = ClientInitializer.getConfig().getFloatOption(Config.DEADZONE) / 2.0f;

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
            glfwGetJoystickAxes(ClientInitializer.getConfig().getIntOption(Config.CONTROLLER_ID)).get();
            return true;
        } catch (NullPointerException e) {
            return false;
        }
    }
}
