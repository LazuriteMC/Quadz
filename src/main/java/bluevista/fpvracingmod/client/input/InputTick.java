package bluevista.fpvracingmod.client.input;

import bluevista.fpvracingmod.client.ClientInitializer;
import bluevista.fpvracingmod.client.math.BetaflightHelper;
import bluevista.fpvracingmod.client.math.QuaternionHelper;
import bluevista.fpvracingmod.config.Config;
import bluevista.fpvracingmod.server.entities.DroneEntity;
import bluevista.fpvracingmod.server.items.TransmitterItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Quaternion;

import static org.lwjgl.glfw.GLFW.glfwGetJoystickAxes;

@Environment(EnvType.CLIENT)
public class InputTick {

    private static final MinecraftClient client = MinecraftClient.getInstance();

    private static long prevTime;
    private static final int throttleScalar = 15; // find a scientific value for this, no more guessing ;)

    private static final Config clientConfig = ClientInitializer.getConfig(); // 3 long 5 me 3 type

    public static void tick(DroneEntity drone) {
        if(drone != null && TransmitterItem.isHoldingTransmitter(client.player) && !client.isPaused() && controllerExists()) {
            float d = (System.currentTimeMillis() - prevTime) / 1000f;

            float currT = glfwGetJoystickAxes(clientConfig.getIntOption(Config.CONTROLLER_ID)).get(clientConfig.getIntOption(Config.THROTTLE_NUM)) + 1;
            float currX = -glfwGetJoystickAxes(clientConfig.getIntOption(Config.CONTROLLER_ID)).get(clientConfig.getIntOption(Config.PITCH_NUM));
            float currY = -glfwGetJoystickAxes(clientConfig.getIntOption(Config.CONTROLLER_ID)).get(clientConfig.getIntOption(Config.YAW_NUM));
            float currZ = -glfwGetJoystickAxes(clientConfig.getIntOption(Config.CONTROLLER_ID)).get(clientConfig.getIntOption(Config.ROLL_NUM));

            if (clientConfig.getIntOption(Config.INVERT_THROTTLE) == 1) {
                currT = Math.abs(2 - currT);
            }

            if (clientConfig.getIntOption(Config.THROTTLE_CENTER_POSITION) == 1) {
                --currT;
                if (currT < 0) {
                    currT = 0;
                }
                currT *= 2;
            }

            if (clientConfig.getIntOption(Config.INVERT_PITCH) == 1) {
                currX *= -1;
            }

            if (clientConfig.getIntOption(Config.INVERT_YAW) == 1) {
                currY *= -1;
            }

            if (clientConfig.getIntOption(Config.INVERT_ROLL) == 1) {
                currZ *= -1;
            }

            // Note: There's probably a better way of doing this, but yeah... it ignores input within the deadzone range
            if (clientConfig.getFloatOption(Config.DEADZONE) != 0.0F) {
                float halfDeadzone = clientConfig.getFloatOption(Config.DEADZONE) / 2.0f;

                if (currX < halfDeadzone && currX > -halfDeadzone) {
                    currX = 0.0f;
                }

                if (currY < halfDeadzone && currY > -halfDeadzone) {
                    currY = 0.0f;
                }

                if (currZ < halfDeadzone && currZ > -halfDeadzone) {
                    currZ = 0.0f;
                }
            }

            float deltaX = (float) BetaflightHelper.calculateRates(currX, clientConfig.getFloatOption(Config.RATE), clientConfig.getFloatOption(Config.EXPO), clientConfig.getFloatOption(Config.SUPER_RATE)) * d;
            float deltaY = (float) BetaflightHelper.calculateRates(currY, clientConfig.getFloatOption(Config.RATE), clientConfig.getFloatOption(Config.EXPO), clientConfig.getFloatOption(Config.SUPER_RATE)) * d;
            float deltaZ = (float) BetaflightHelper.calculateRates(currZ, clientConfig.getFloatOption(Config.RATE), clientConfig.getFloatOption(Config.EXPO), clientConfig.getFloatOption(Config.SUPER_RATE)) * d;

            Quaternion q = drone.getOrientation();
            QuaternionHelper.rotateX(q, deltaX);
            QuaternionHelper.rotateY(q, deltaY);
            QuaternionHelper.rotateZ(q, deltaZ);

            currT /= throttleScalar;
            drone.setThrottle(currT);

            prevTime = System.currentTimeMillis();
        }
    }

    public static boolean controllerExists() {
        try {
            glfwGetJoystickAxes(clientConfig.getIntOption(Config.CONTROLLER_ID)).get();
            return true;
        } catch (NullPointerException e) {
            return false;
        }
    }
}
