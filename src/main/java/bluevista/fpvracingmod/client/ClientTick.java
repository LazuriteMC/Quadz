package bluevista.fpvracingmod.client;

import bluevista.fpvracingmod.client.input.InputTick;
import bluevista.fpvracingmod.server.entities.DroneEntity;
import bluevista.fpvracingmod.server.entities.ViewHandler;
import bluevista.fpvracingmod.server.items.GogglesItem;
import bluevista.fpvracingmod.server.items.TransmitterItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;

@Environment(EnvType.CLIENT)
public class ClientTick {

    private static ViewHandler view;
    public static DroneEntity currentDrone;

    public static void tick(MinecraftClient mc) {
        float delta = mc.getTickDelta();

        if(view != null)
            view.clientTick(delta);

        /*
         * Drone view and control logic
         */
        if (mc.player != null) {
            InputTick.setShouldTick(false);
            if(isWearingGoggles(mc.player) || isHoldingTransmitter(mc.player)) {
                if(mc.getCameraEntity() instanceof ViewHandler &&
                        ((ViewHandler) mc.getCameraEntity()).getTarget() instanceof DroneEntity)
                    currentDrone = (DroneEntity) ((ViewHandler) mc.getCameraEntity()).getTarget();
                else currentDrone = DroneEntity.getNearestTo(mc.player);

                if(currentDrone != null) {
                    if(isWearingGoggles(mc.player))
                        setView(mc, currentDrone);
                    if (isValidTransmitter(mc.player, currentDrone))
                        InputTick.setShouldTick(true);
                }
            }

            if(!isWearingGoggles(mc.player) && mc.getCameraEntity() instanceof ViewHandler)
                resetView(mc);
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
     * Well?? Should u????
     */
    public static boolean shouldRenderHand() {
        return view == null;
    }

    public static void setView(MinecraftClient mc, Entity entity) {
        if(!(mc.getCameraEntity() instanceof ViewHandler)) {
            view = new ViewHandler(mc.world, entity);
            mc.setCameraEntity(view);
        }
    }

    public static void resetView(MinecraftClient mc) {
        mc.setCameraEntity(null);
    }
}
