package bluevista.fpvracingmod.client;

import bluevista.fpvracingmod.network.ConfigC2S;
import bluevista.fpvracingmod.network.DroneInfoC2S;
import bluevista.fpvracingmod.server.entities.DroneEntity;
import bluevista.fpvracingmod.server.items.TransmitterItem;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.minecraft.client.MinecraftClient;

public class ClientTick {
    private static boolean haveSentPacket = false;
    public static DroneEntity boundDrone;

    public static void tick(MinecraftClient mc) {
        if (mc.player != null) {
            DroneEntity d = TransmitterItem.droneFromTransmitter(mc.player.getMainHandStack(), mc.player);
            boundDrone = d == null ? boundDrone : d;

            if (!haveSentPacket) {
                ConfigC2S.send(ClientInitializer.getConfig());
                haveSentPacket = true;
            }

            if (TransmitterItem.isHoldingTransmitter(mc.player))
                if(boundDrone != null) DroneInfoC2S.send(boundDrone);
        } else if (haveSentPacket) {
            haveSentPacket = false;
        }
    }

    public static boolean isInGoggles(MinecraftClient client) {
        return client.getCameraEntity() instanceof DroneEntity;
    }

    public static void register() {
        ClientTickCallback.EVENT.register(ClientTick::tick);
    }
}
