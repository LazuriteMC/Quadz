package bluevista.fpvracingmod.client.input;

import bluevista.fpvracingmod.client.controller.Controller;
import bluevista.fpvracingmod.client.math.QuaternionHelper;
import bluevista.fpvracingmod.server.entities.DroneEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public class InputTick {

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    private static boolean shouldTick;

    private static long prevTime;

    public static void tick(DroneEntity drone) {
        if(shouldTick()) {
            float d = (System.currentTimeMillis() - prevTime) / 1000f;

            float currX = -Controller.getBetaflightAxis(Controller.PITCH_NUM, Controller.RATE, Controller.EXPO, Controller.SUPER_RATE);
            float currY = -Controller.getBetaflightAxis(Controller.YAW_NUM, Controller.RATE, Controller.EXPO, Controller.SUPER_RATE);
            float currZ = -Controller.getBetaflightAxis(Controller.ROLL_NUM, Controller.RATE, Controller.EXPO, Controller.SUPER_RATE);
            float currT = Controller.getAxis(Controller.THROTTLE_NUM) + 1;

            // Note: There's probably a better way of doing this, but yeah... it ignores input within the deadzone range
            if (Controller.DEADZONE != 0) {
                float halfDeadzone = Controller.DEADZONE / 2.0f;

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

            float deltaX = currX * d;
            float deltaY = currY * d;
            float deltaZ = currZ * d;

            drone.setOrientation(QuaternionHelper.rotateX(drone.getOrientation(), deltaX));
            drone.setOrientation(QuaternionHelper.rotateY(drone.getOrientation(), deltaY));
            drone.setOrientation(QuaternionHelper.rotateZ(drone.getOrientation(), deltaZ));
            drone.setThrottle(currT);

            prevTime = System.currentTimeMillis();
        }
    }

    public static void setShouldTick(boolean should) {
        shouldTick = should;
    }

    public static boolean shouldTick() {
        return shouldTick && !mc.isPaused();
    }

}
