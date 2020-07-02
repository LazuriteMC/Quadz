package bluevista.fpvracingmod.client.input;

import bluevista.fpvracingmod.client.controller.Controller;
import bluevista.fpvracingmod.client.math.helper.QuaternionHelper;
import bluevista.fpvracingmod.server.entities.DroneEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public class InputTick {

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    private static boolean shouldTick;

    private static float prevX;
    private static float prevY;
    private static float prevZ;
    private static float prevT;

    public static void tick(DroneEntity drone, float delta) {
        if(shouldTick()) {
            float currX = -Controller.getAxis(Controller.PITCH_NUM);
            float currY = -Controller.getAxis(Controller.YAW_NUM);
            float currZ = -Controller.getAxis(Controller.ROLL_NUM);
            float currT = Controller.getAxis(Controller.THROTTLE_NUM) + 1;

            float deltaX = prevX + (currX - prevX) * delta;
            float deltaY = prevY + (currY - prevY) * delta;
            float deltaZ = prevZ + (currZ - prevZ) * delta;
            float deltaT = prevT + (currT - prevT) * delta;

            drone.setOrientation(QuaternionHelper.rotateX(drone.getOrientation(), deltaX));
            drone.setOrientation(QuaternionHelper.rotateY(drone.getOrientation(), deltaY));
            drone.setOrientation(QuaternionHelper.rotateZ(drone.getOrientation(), deltaZ));
            drone.setThrottle(deltaT);

            prevX = currX;
            prevY = currY;
            prevZ = currZ;
            prevT = currT;
        }
    }

    public static void setShouldTick(boolean should) {
        shouldTick = should;
    }

    public static boolean shouldTick() {
        return shouldTick && !mc.isPaused();
    }

}
