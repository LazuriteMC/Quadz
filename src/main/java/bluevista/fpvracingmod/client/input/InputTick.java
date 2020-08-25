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

import javax.vecmath.Quat4f;

import static org.lwjgl.glfw.GLFW.glfwGetJoystickAxes;

@Environment(EnvType.CLIENT)
public class InputTick {
    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static long prevTime;

    public static void tick(DroneEntity drone) {
        if(drone != null && TransmitterItem.isHoldingTransmitter(client.player) && !client.isPaused() && controllerExists()) {
            float d = (System.currentTimeMillis() - prevTime) / 1000f;

            float currT = glfwGetJoystickAxes(ClientInitializer.getConfig().getIntOption(Config.CONTROLLER_ID)).get(ClientInitializer.getConfig().getIntOption(Config.THROTTLE));
            float currX = -glfwGetJoystickAxes(ClientInitializer.getConfig().getIntOption(Config.CONTROLLER_ID)).get(ClientInitializer.getConfig().getIntOption(Config.PITCH));
            float currY = -glfwGetJoystickAxes(ClientInitializer.getConfig().getIntOption(Config.CONTROLLER_ID)).get(ClientInitializer.getConfig().getIntOption(Config.YAW));
            float currZ = -glfwGetJoystickAxes(ClientInitializer.getConfig().getIntOption(Config.CONTROLLER_ID)).get(ClientInitializer.getConfig().getIntOption(Config.ROLL));

            if (ClientInitializer.getConfig().getIntOption(Config.INVERT_THROTTLE) == 1) {
                currT *= -1;
            }

            if (ClientInitializer.getConfig().getIntOption(Config.THROTTLE_CENTER_POSITION) == 1) {
                if (currT < 0) {
                    currT = 0;
                }
            } else if (ClientInitializer.getConfig().getIntOption(Config.THROTTLE_CENTER_POSITION) == 0) {
                currT = (currT + 1) / 2.0f;
            }

            if (ClientInitializer.getConfig().getIntOption(Config.INVERT_PITCH) == 1) {
                currX *= -1;
            }

            if (ClientInitializer.getConfig().getIntOption(Config.INVERT_YAW) == 1) {
                currY *= -1;
            }

            if (ClientInitializer.getConfig().getIntOption(Config.INVERT_ROLL) == 1) {
                currZ *= -1;
            }

            if (ClientInitializer.getConfig().getFloatOption(Config.DEADZONE) != 0.0F) {
                float halfDeadzone = ClientInitializer.getConfig().getFloatOption(Config.DEADZONE) / 2.0f;

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

            double deltaX = BetaflightHelper.calculateRates(currX, ClientInitializer.getConfig().getFloatOption(Config.RATE), ClientInitializer.getConfig().getFloatOption(Config.EXPO), ClientInitializer.getConfig().getFloatOption(Config.SUPER_RATE)) * d;
            double deltaY = BetaflightHelper.calculateRates(currY, ClientInitializer.getConfig().getFloatOption(Config.RATE), ClientInitializer.getConfig().getFloatOption(Config.EXPO), ClientInitializer.getConfig().getFloatOption(Config.SUPER_RATE)) * d;
            double deltaZ = BetaflightHelper.calculateRates(currZ, ClientInitializer.getConfig().getFloatOption(Config.RATE), ClientInitializer.getConfig().getFloatOption(Config.EXPO), ClientInitializer.getConfig().getFloatOption(Config.SUPER_RATE)) * d;

            if(prevTime > 0) {
                Quat4f q = drone.getOrientation();
                QuaternionHelper.rotateX(q, deltaX);
                QuaternionHelper.rotateY(q, deltaY);
                QuaternionHelper.rotateZ(q, deltaZ);
                drone.setOrientation(q);
            }

            drone.setThrottle(currT);
            prevTime = System.currentTimeMillis();
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
