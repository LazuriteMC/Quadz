package bluevista.fpvracing.client;

import bluevista.fpvracing.config.Config;
import bluevista.fpvracing.server.entities.DroneEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public class ClientTick {
    public static boolean isServerModded = false;
    public static boolean shouldRenderPlayer = false;

    private static float droneFOV;
    private static double prevFOV;

    public static void tick(MinecraftClient client) {
        if (client.player != null && !client.isPaused()) {
            if (client.getCameraEntity() instanceof DroneEntity) {
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

    public static boolean isPlayerIDClient(int playerID) {
        if (ClientInitializer.client.player != null) {
            return playerID == ClientInitializer.client.player.getEntityId();
        } else {
            return false;
        }
    }

    public static void register() {
        ClientTickCallback.EVENT.register(ClientTick::tick);
    }
}
