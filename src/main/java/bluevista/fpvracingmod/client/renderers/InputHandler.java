package bluevista.fpvracingmod.client.renderers;

import bluevista.fpvracingmod.client.controls.Controller;
import bluevista.fpvracingmod.client.math.helper.QuaternionHelper;
import bluevista.fpvracingmod.server.entities.DroneEntity;
import bluevista.fpvracingmod.server.items.TransmitterItem;
import net.minecraft.client.MinecraftClient;

public class InputHandler {

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    private static float prevX;
    private static float prevY;
    private static float prevZ;

    public static void tick(DroneEntity drone, float delta) {
        float currX = -Controller.getAxis(2); // yaw, pitch, or roll?
        float currY = -Controller.getAxis(3); // yaw, pitch, or roll?
        float currZ = -Controller.getAxis(1); // yaw, pitch, or roll?

        float deltaX = prevX + (currX - prevX) * delta;
        float deltaY = prevY + (currY - prevY) * delta;
        float deltaZ = prevZ + (currZ - prevZ) * delta;

        if(isPlayerControlling(drone)) {
            drone.setOrientation(QuaternionHelper.rotateX(drone.getOrientation(), deltaX));
            drone.setOrientation(QuaternionHelper.rotateY(drone.getOrientation(), deltaY));
            drone.setOrientation(QuaternionHelper.rotateZ(drone.getOrientation(), deltaZ));
            drone.setThrottle(Controller.getAxis(0) + 1);
        }

        prevX = currX;
        prevY = currY;
        prevZ = currZ;
    }

    public static boolean isPlayerControlling(DroneEntity drone) {
        return mc.player.inventory.getMainHandStack().getItem() instanceof TransmitterItem &&
                mc.player.getUuidAsString().equals(drone.getControllingPlayerUUID());
    }
}
