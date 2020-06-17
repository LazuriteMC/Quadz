package bluevista.fpvracingmod.client;

import bluevista.fpvracingmod.client.controls.Controller;
import bluevista.fpvracingmod.client.math.helper.QuaternionHelper;
import bluevista.fpvracingmod.server.entities.DroneEntity;
import bluevista.fpvracingmod.server.entities.ViewHandler;
import bluevista.fpvracingmod.server.items.GogglesItem;
import bluevista.fpvracingmod.server.items.TransmitterItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.Matrix4f;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public class RenderHandler {

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    private static ViewHandler view;
    public static DroneEntity currentDrone;
//    public static Vec3d playerPos;

    private static float prevX;
    private static float prevY;
    private static float prevZ;

    public static void renderTick(MatrixStack stack, float delta) {
        /*
         * Update the ViewHandler object if it exists
         */
        if(view != null) view.clientTick(delta);

        /*
         * Rotate the screen using the drone's orientation quaternion
         */
        Entity currentViewEntity = mc.getCameraEntity();
        if(currentViewEntity instanceof ViewHandler && !mc.gameRenderer.getCamera().isThirdPerson()) {
            if (((ViewHandler) currentViewEntity).getTarget() instanceof DroneEntity) {
                DroneEntity drone = (DroneEntity) ((ViewHandler) currentViewEntity).getTarget();
                Matrix4f newMat = new Matrix4f(drone.getOrientation());
                Matrix4f screenMat = stack.peek().getModel();
                newMat.transpose();
                screenMat.multiply(newMat);
            }
        }

        /*
         * If a world is loaded (if the player exists) ->
         * If the player is wearing the goggles item and holding a transmitter ->
         * Perform ViewHandler creation/deletion logic
         */
        if (mc.player != null) {
            if(mc.player.inventory.getMainHandStack().getItem() instanceof TransmitterItem) {
                currentDrone = DroneEntity.getNearestTo(mc.player);

                if(currentDrone != null) {
                    inputTick(currentDrone, delta);
                }
            }

            if(mc.player.inventory.armor.get(3).getItem() instanceof GogglesItem) {

//                mc.player.move(MoverType.PLAYER, new Vec3d(
//                        playerPos.x - (mc.player.getPositionVec()).x,
//                        playerPos.y - (mc.player.getPositionVec()).y,
//                        playerPos.z - (mc.player.getPositionVec()).z));

                if (!(mc.getCameraEntity() instanceof ViewHandler)) {
                    currentDrone = DroneEntity.getNearestTo(mc.player);

                    if(currentDrone != null) {
//                        playerPos = mc.player.getPos();
                        view = new ViewHandler(mc.world, currentDrone);
                        mc.setCameraEntity(view);
                    }

                } else if (((ViewHandler) mc.getCameraEntity()).getTarget() instanceof DroneEntity) {
                    currentDrone = (DroneEntity) ((ViewHandler) mc.getCameraEntity()).getTarget();
                }

            } else if(mc.getCameraEntity() instanceof ViewHandler) {
                view = null;
                mc.setCameraEntity(mc.player);
//                mc.player.setPos(playerPos.x, playerPos.y, playerPos.z);
            }
        }
    }

    public static void inputTick(DroneEntity drone, float delta) {
        float currX = -Controller.getAxis(2); // yaw, pitch, or roll?
        float currY = -Controller.getAxis(3); // yaw, pitch, or roll?
        float currZ = -Controller.getAxis(1); // yaw, pitch, or roll?

        float deltaX = prevX + (currX - prevX) * delta;
        float deltaY = prevY + (currY - prevY) * delta;
        float deltaZ = prevZ + (currZ - prevZ) * delta;

        if(isPlayerControlling()) {
            drone.setOrientation(QuaternionHelper.rotateX(drone.getOrientation(), deltaX));
            drone.setOrientation(QuaternionHelper.rotateY(drone.getOrientation(), deltaY));
            drone.setOrientation(QuaternionHelper.rotateZ(drone.getOrientation(), deltaZ));
            drone.setThrottle(Controller.getAxis(0) + 1);
        }

        prevX = currX;
        prevY = currY;
        prevZ = currZ;
    }

    public static boolean shouldRenderHand() {
        return view == null;
    }

    public static boolean isPlayerControlling() {
        return mc.player.inventory.getMainHandStack().getItem() instanceof TransmitterItem &&
                mc.player.getUuidAsString().equals(currentDrone.getControllingPlayerUUID());
    }

}
