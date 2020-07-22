package bluevista.fpvracingmod.client.input;

import bluevista.fpvracingmod.client.controller.Controller;
import bluevista.fpvracingmod.client.math.BetaflightHelper;
import bluevista.fpvracingmod.client.math.QuaternionHelper;
import bluevista.fpvracingmod.server.entities.DroneEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Quaternion;

@Environment(EnvType.CLIENT)
public class InputTick {

    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static long prevTime;
    private static final int throttleScalar = 15;

    public static void tick(DroneEntity drone) {
        float d = (System.currentTimeMillis() - prevTime) / 1000f;

        float currX = -Controller.getAxis(Controller.PITCH_NUM);
        float currY = -Controller.getAxis(Controller.YAW_NUM);
        float currZ = -Controller.getAxis(Controller.ROLL_NUM);
        float currT = (Controller.getAxis(Controller.THROTTLE_NUM) + 1);

        if (Controller.INVERT_THROTTLE == 1) {
            currT = Math.abs(2 - currT);
        }

        if (Controller.THROTTLE_CENTER_POSITION == 1) {
            --currT;
            if (currT < 0) {
                currT = 0;
            }
            currT *= 2;
        }

        if (Controller.INVERT_PITCH == 1) {
            currX *= -1;
        }

        if (Controller.INVERT_YAW == 1) {
            currY *= -1;
        }

        if (Controller.INVERT_ROLL == 1) {
            currZ *= -1;
        }

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

        float deltaX = (float) BetaflightHelper.calculateRates(currX, Controller.RATE, Controller.EXPO, Controller.SUPER_RATE) * d;
        float deltaY = (float) BetaflightHelper.calculateRates(currY, Controller.RATE, Controller.EXPO, Controller.SUPER_RATE) * d;
        float deltaZ = (float) BetaflightHelper.calculateRates(currZ, Controller.RATE, Controller.EXPO, Controller.SUPER_RATE) * d;

        Quaternion q = drone.getOrientation();
        QuaternionHelper.rotateX(q, deltaX);
        QuaternionHelper.rotateY(q, deltaY);
        QuaternionHelper.rotateZ(q, deltaZ);

        currT /= throttleScalar;
        drone.setThrottle(currT);

        prevTime = System.currentTimeMillis();
    }
}
