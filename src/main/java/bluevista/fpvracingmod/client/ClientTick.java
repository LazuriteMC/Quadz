package bluevista.fpvracingmod.client;

import bluevista.fpvracingmod.config.Config;
import bluevista.fpvracingmod.server.entities.DroneEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;

import java.util.UUID;

@Environment(EnvType.CLIENT)
public class ClientTick {
    private static float droneFOV;
    private static double prevFOV;

    public static void tick(MinecraftClient client) {
        if (client.player != null && !client.isPaused()) {
            DroneEntity.NEAR_TRACKING_RANGE = MathHelper.floor(DroneEntity.TRACKING_RANGE / 16.0D) < client.options.viewDistance ? DroneEntity.TRACKING_RANGE - 5 : client.options.viewDistance * 16;

            if (client.cameraEntity instanceof DroneEntity) {
                droneFOV = ((DroneEntity)client.cameraEntity).getConfigValues(Config.FIELD_OF_VIEW).floatValue();

                if (droneFOV != 0.0f && client.options.fov != droneFOV) {
                    prevFOV = client.options.fov;
                    client.options.fov = droneFOV;
                }

                if (droneFOV == 0.0f && prevFOV != 0.0f) {
                    client.options.fov = prevFOV;
                    prevFOV = 0.0f;
                }
            } else if (prevFOV != 0.0f) {
                client.options.fov = prevFOV;
                prevFOV = 0.0f;
            }
        }
    }

    public static boolean isInGoggles() {
        return ClientInitializer.client.getCameraEntity() instanceof DroneEntity;
    }

    public static boolean isPlayerIDClient(UUID playerID) {
        if(ClientInitializer.client.player != null && playerID != null) {
            return playerID.equals(ClientInitializer.client.player.getUuid());
        } else {
            return false;
        }
    }

    public static void register() {
        ClientTickCallback.EVENT.register(ClientTick::tick);
    }
}
