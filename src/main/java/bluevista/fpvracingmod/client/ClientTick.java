package bluevista.fpvracingmod.client;

import bluevista.fpvracingmod.config.Config;
import bluevista.fpvracingmod.network.entity.DroneEntityC2S;
import bluevista.fpvracingmod.server.entities.DroneEntity;
import bluevista.fpvracingmod.server.items.TransmitterItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class ClientTick {
    private static float configFOV;
    private static double prevFOV;

    public static void tick(MinecraftClient client) {
        DroneEntity.NEAR_TRACKING_RANGE = MathHelper.floor(DroneEntity.TRACKING_RANGE / 16.0D) < client.options.viewDistance ? DroneEntity.TRACKING_RANGE - 5 : client.options.viewDistance * 16;

        if (client.player != null && !client.isPaused()) {
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
                    if (client.player.getMainHandStack().getSubTag(Config.BIND) != null) {
                        DroneEntity drone = TransmitterItem.droneFromTransmitter(client.player.getMainHandStack(), client.player);
                        if (drone != null) {
                            configFOV = drone.getConfigValues(Config.FIELD_OF_VIEW).floatValue();
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
