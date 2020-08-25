package bluevista.fpvracingmod.client;

import bluevista.fpvracingmod.network.entity.DroneEntityC2S;
import bluevista.fpvracingmod.server.entities.DroneEntity;
import bluevista.fpvracingmod.server.items.TransmitterItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public class ClientTick {
    public static DroneEntity boundDrone;

    public static void tick(MinecraftClient client) {
        if (client.player != null && !client.isPaused()) {
            DroneEntity d = TransmitterItem.droneFromTransmitter(client.player.getMainHandStack(), client.player);
            if(d != null) boundDrone = d;

            client.world.getEntities().forEach((entity) -> {
                if(entity instanceof DroneEntity) {
                    DroneEntity drone = (DroneEntity) entity;
                    if(drone.playerID != null) {
                        if (drone.playerID.equals(client.player.getUuid())) {
                            DroneEntityC2S.send(drone);
                        }
                    }
                }
            });

        }
    }

    public static void register() {
        ClientTickCallback.EVENT.register(ClientTick::tick);
    }
}
