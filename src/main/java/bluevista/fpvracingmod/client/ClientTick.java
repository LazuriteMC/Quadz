package bluevista.fpvracingmod.client;

import bluevista.fpvracingmod.network.ClientConfigC2S;
import bluevista.fpvracingmod.network.DroneInfoC2S;
import bluevista.fpvracingmod.server.entities.DroneEntity;
import bluevista.fpvracingmod.server.items.TransmitterItem;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.minecraft.client.MinecraftClient;

public class ClientTick {
    private static boolean haveSentPacket = false;

    public static void tick(MinecraftClient mc) {
        if (mc.player != null) {
            if (!haveSentPacket) {
                ClientConfigC2S.send();
                haveSentPacket = true;
            }

            if (TransmitterItem.isHoldingTransmitter(mc.player)) {
                DroneEntity drone = TransmitterItem.droneFromTransmitter(mc.player.getMainHandStack(), mc.player);
                if(drone != null)
                    DroneInfoC2S.send(drone);
            }
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
