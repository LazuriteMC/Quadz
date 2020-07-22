package bluevista.fpvracingmod.client;

import bluevista.fpvracingmod.network.DroneInfoToServer;
import bluevista.fpvracingmod.server.entities.DroneEntity;
import bluevista.fpvracingmod.server.items.GogglesItem;
import bluevista.fpvracingmod.server.items.TransmitterItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.minecraft.client.MinecraftClient;

import java.util.List;

@Environment(EnvType.CLIENT)
public class ClientTick {
    public static void tick(MinecraftClient mc) {
        if (mc.player != null) {

            if(GogglesItem.isWearingGoggles(mc.player) && !isInView(mc)) {
                List<DroneEntity> drones = DroneEntity.getNearbyDrones(mc.player, 100);
                for (DroneEntity drone : drones) {
                    if (GogglesItem.isOnRightChannel(drone, mc.player)) {
                        setView(mc, drone);
                    }
                }
            }

            if (TransmitterItem.isHoldingTransmitter(mc.player)) {
                DroneEntity drone = TransmitterItem.droneFromTransmitter(mc.player.getMainHandStack(), mc.player);
                if (drone != null) DroneInfoToServer.send(drone);
            }

            if (!GogglesItem.isWearingGoggles(mc.player) && isInView(mc) || mc.getCameraEntity().removed)
                resetView(mc);
        }
    }

    public static void setView(MinecraftClient mc, DroneEntity drone) {
        if(!(mc.getCameraEntity() instanceof DroneEntity))
            mc.setCameraEntity(drone);
    }

    public static void resetView(MinecraftClient mc) {
        mc.setCameraEntity(null);
    }

    public static boolean isInView(MinecraftClient mc) {
        return mc.getCameraEntity() instanceof DroneEntity;
    }

    public static void register() {
        ClientTickCallback.EVENT.register(ClientTick::tick);
    }
}
