package bluevista.fpvracingmod.client;

import bluevista.fpvracingmod.client.controller.Controller;
import bluevista.fpvracingmod.client.math.helper.QuaternionHelper;
import bluevista.fpvracingmod.server.entities.DroneEntity;
import bluevista.fpvracingmod.server.items.TransmitterItem;
import net.minecraft.client.MinecraftClient;

public class InputHandler {

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    private static float prevX;
    private static float prevY;
    private static float prevZ;
    private static float prevT;

    public static void tick(DroneEntity drone, float delta) {
        float currX = -Controller.getAxis(Controller.ROLL_NUM);
        float currY = -Controller.getAxis(Controller.PITCH_NUM);
        float currZ = -Controller.getAxis(Controller.YAW_NUM);
        float currT = Controller.getAxis(Controller.THROTTLE_NUM) + 1;

        float deltaX = prevX + (currX - prevX) * delta;
        float deltaY = prevY + (currY - prevY) * delta;
        float deltaZ = prevZ + (currZ - prevZ) * delta;
        float deltaT = prevT + (currT - prevT) * delta;

        if(isPlayerControlling(drone)) {
            drone.setOrientation(QuaternionHelper.rotateX(drone.getOrientation(), deltaX));
            drone.setOrientation(QuaternionHelper.rotateY(drone.getOrientation(), deltaY));
            drone.setOrientation(QuaternionHelper.rotateZ(drone.getOrientation(), deltaZ));
            drone.setThrottle(deltaT);
        }

        prevX = currX;
        prevY = currY;
        prevZ = currZ;
        prevT = currT;
    }

    public static boolean isPlayerControlling(DroneEntity drone) {
        return mc.player.inventory.getMainHandStack().getItem() instanceof TransmitterItem &&
                mc.player.getUuidAsString().equals(drone.getBoundPlayerUUID());
    }
}
