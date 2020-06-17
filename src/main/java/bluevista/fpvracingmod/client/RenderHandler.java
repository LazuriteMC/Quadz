package bluevista.fpvracingmod.client;

import bluevista.fpvracingmod.client.renderers.InputHandler;
import bluevista.fpvracingmod.server.entities.DroneEntity;
import bluevista.fpvracingmod.server.entities.ViewHandler;
import bluevista.fpvracingmod.server.items.GogglesItem;
import bluevista.fpvracingmod.server.items.TransmitterItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.Matrix4f;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;

import javax.sound.midi.Transmitter;

public class RenderHandler {

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    private static ViewHandler view;
    public static DroneEntity currentDrone;
//    public static Vec3d playerPos;

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
            if(isPlayerHoldingTransmitter() || isPlayerWearingGoggles()) {
                currentDrone = DroneEntity.getNearestTo(mc.player);
            }

            if(isPlayerHoldingTransmitter())
                if(currentDrone != null)
                    InputHandler.tick(currentDrone, delta);

            if(isPlayerWearingGoggles()) {
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

    public static boolean shouldRenderHand() {
        return view == null;
    }

    public static boolean isPlayerHoldingTransmitter() {
        return mc.player.inventory.getMainHandStack().getItem() instanceof TransmitterItem;
    }

    public static boolean isPlayerWearingGoggles() {
        return mc.player.inventory.armor.get(3).getItem() instanceof GogglesItem;
    }
}
