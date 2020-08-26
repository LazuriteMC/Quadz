package bluevista.fpvracingmod.client;

import bluevista.fpvracingmod.network.entity.DroneEntityC2S;
import bluevista.fpvracingmod.config.Config;
import bluevista.fpvracingmod.server.entities.DroneEntity;
import bluevista.fpvracingmod.server.items.TransmitterItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public class ClientTick {
    public static DroneEntity boundDrone;
    private static float configFOV;
    private static double prevFOV;

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


            if (client.cameraEntity instanceof DroneEntity) {
                configFOV = ClientInitializer.getConfig().getFloatOption(Config.FIELD_OF_VIEW);

                if (client.player.getMainHandStack().getItem() instanceof TransmitterItem) {
                    if (client.player.getMainHandStack().getSubTag("bind") != null) {
                        DroneEntity drone = TransmitterItem.droneFromTransmitter(client.player.getMainHandStack(), client.player);
                        if (drone != null) {
                            configFOV = drone.getFieldOfView();
                        }
                    }
                }

                if (client.options.fov != configFOV && configFOV != 0.0f) {
                    prevFOV = client.options.fov;
                    client.options.fov = configFOV;
                }
            } else if (client.options.fov == configFOV) {
                client.options.fov = prevFOV;
            }
        }
    }

    public static void register() {
        ClientTickCallback.EVENT.register(ClientTick::tick);
    }
}
