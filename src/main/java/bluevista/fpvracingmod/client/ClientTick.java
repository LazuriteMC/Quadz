package bluevista.fpvracingmod.client;

import bluevista.fpvracingmod.network.entity.DroneEntityC2S;
import bluevista.fpvracingmod.server.entities.DroneEntity;
import bluevista.fpvracingmod.server.items.TransmitterItem;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.minecraft.client.MinecraftClient;

public class ClientTick {
    public static DroneEntity boundDrone;

    public static void tick(MinecraftClient client) {
        if (client.player != null && !client.isPaused()) {
            DroneEntity d = TransmitterItem.droneFromTransmitter(client.player.getMainHandStack(), client.player);
            boundDrone = d == null ? boundDrone : d;

            if(ClientInitializer.physicsWorld != null)
                ClientInitializer.physicsWorld.stepWorld();

            if (TransmitterItem.isHoldingTransmitter(client.player)) {
                if (boundDrone != null) {
                    DroneEntityC2S.send(boundDrone);
                }
            }

        }
    }

    public static void register() {
        ClientTickCallback.EVENT.register(ClientTick::tick);
    }
}
