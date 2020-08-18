package bluevista.fpvracingmod.client.input;

import bluevista.fpvracingmod.client.ClientInitializer;
import bluevista.fpvracingmod.client.math.BetaflightHelper;
import bluevista.fpvracingmod.config.Config;
import bluevista.fpvracingmod.server.entities.DroneEntity;
import bluevista.fpvracingmod.server.items.TransmitterItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

import static org.lwjgl.glfw.GLFW.glfwGetJoystickAxes;

@Environment(EnvType.CLIENT)
public class InputTick {
    private static final MinecraftClient client = MinecraftClient.getInstance();

    private static long prevTime;
    private static final int throttleScalar = 15; // find a scientific value for this, no more guessing ;)

    public static void tick(DroneEntity drone) {
        if(drone != null && TransmitterItem.isHoldingTransmitter(client.player) && !client.isPaused() && controllerExists()) {
            float d = (System.currentTimeMillis() - prevTime) / 1000f;

            float currT = glfwGetJoystickAxes(ClientInitializer.getConfig().getIntOption(Config.CONTROLLER_ID)).get(ClientInitializer.getConfig().getIntOption(Config.THROTTLE)) + 1;
            float currX = -glfwGetJoystickAxes(ClientInitializer.getConfig().getIntOption(Config.CONTROLLER_ID)).get(ClientInitializer.getConfig().getIntOption(Config.PITCH));
            float currY = -glfwGetJoystickAxes(ClientInitializer.getConfig().getIntOption(Config.CONTROLLER_ID)).get(ClientInitializer.getConfig().getIntOption(Config.YAW));
            float currZ = -glfwGetJoystickAxes(ClientInitializer.getConfig().getIntOption(Config.CONTROLLER_ID)).get(ClientInitializer.getConfig().getIntOption(Config.ROLL));

            if (ClientInitializer.getConfig().getIntOption(Config.INVERT_THROTTLE) == 1) {
                currT = Math.abs(2 - currT);
            }

            if (ClientInitializer.getConfig().getIntOption(Config.THROTTLE_CENTER_POSITION) == 1) {
                --currT;
                if (currT < 0) {
                    currT = 0;
                }
                currT *= 2;
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

            // Note: There's probably a better way of doing this, but yeah... it ignores input within the deadzone range
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

            float deltaX = (float) BetaflightHelper.calculateRates(currX, ClientInitializer.getConfig().getFloatOption(Config.RATE), ClientInitializer.getConfig().getFloatOption(Config.EXPO), ClientInitializer.getConfig().getFloatOption(Config.SUPER_RATE)) * d;
            float deltaY = (float) BetaflightHelper.calculateRates(currY, ClientInitializer.getConfig().getFloatOption(Config.RATE), ClientInitializer.getConfig().getFloatOption(Config.EXPO), ClientInitializer.getConfig().getFloatOption(Config.SUPER_RATE)) * d;
            float deltaZ = (float) BetaflightHelper.calculateRates(currZ, ClientInitializer.getConfig().getFloatOption(Config.RATE), ClientInitializer.getConfig().getFloatOption(Config.EXPO), ClientInitializer.getConfig().getFloatOption(Config.SUPER_RATE)) * d;

            if(prevTime != 0) {
                drone.rotateX(deltaX);
                drone.rotateY(deltaY);
                drone.rotateZ(deltaZ);
            }

            currT /= throttleScalar;
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
