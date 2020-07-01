package bluevista.fpvracingmod.client;

import bluevista.fpvracingmod.server.entities.DroneEntity;
import bluevista.fpvracingmod.server.entities.ViewHandler;
import bluevista.fpvracingmod.server.items.GogglesItem;
import bluevista.fpvracingmod.server.items.TransmitterItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Matrix4f;

@Environment(EnvType.CLIENT)
public class RenderHandler {

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    private static ViewHandler view;
    public static DroneEntity currentDrone;

    public static void renderTick(MatrixStack stack, float delta) {
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
         * Drone view and control logic
         */
        if (isWorldLoaded()) {
            if(isWearingGoggles(mc.player) || isHoldingTransmitter(mc.player)) {
                if(isDroneTargeted())
                    currentDrone = (DroneEntity) ((ViewHandler) mc.getCameraEntity()).getTarget();
                else currentDrone = DroneEntity.getNearestTo(mc.player);

                if(currentDrone != null) {
                    if(isWearingGoggles(mc.player)) setView(currentDrone);
                    if (isValidTransmitter(mc.player, currentDrone))
                        InputHandler.tick(currentDrone, delta);
                }
            } else if(mc.getCameraEntity() instanceof ViewHandler) {
                resetView();
            }
        }
    }

    /*
     * Returns whether or not the transmitter
     * the given player is holding is also
     * bound to the given drone entity.
     */
    public static boolean isValidTransmitter(ClientPlayerEntity player, DroneEntity drone) {
        ItemStack heldStack = player.inventory.getMainHandStack();

        try {
            return isHoldingTransmitter(player) &&
                    drone.getUuid().equals(heldStack.getSubTag("bind").getUuid("bind"));
        } catch (NullPointerException e) {
            return false;
        }
    }

    /*
     * Returns whether or not the given player is holding a TransmitterItem
     */
    public static boolean isHoldingTransmitter(ClientPlayerEntity player) {
        return player.inventory.getMainHandStack().getItem() instanceof TransmitterItem;
    }

    /*
     * Returns whether or not the player has a GogglesItem equipped
     * in their head armor slot.
     */
    public static boolean isWearingGoggles(ClientPlayerEntity player) {
        return player.inventory.armor.get(3).getItem() instanceof GogglesItem;
    }

    /*
     * Sorta indirect, but eh
     */
    public static boolean isWorldLoaded() {
        return mc.player != null;
    }

    /*
     * Returns whether or not the drone is currently
     * being targeted on-screen
     */
    public static boolean isDroneTargeted() {
        return mc.getCameraEntity() instanceof ViewHandler &&
                ((ViewHandler) mc.getCameraEntity()).getTarget() instanceof DroneEntity;
    }

    /*
     * Well?? Should u????
     */
    public static boolean shouldRenderHand() {
        return view == null;
    }

    public static void setView(Entity entity) {
        view = new ViewHandler(mc.world, entity);
        mc.setCameraEntity(view);
    }

    public static void resetView() {
        mc.setCameraEntity(null);
    }
}
