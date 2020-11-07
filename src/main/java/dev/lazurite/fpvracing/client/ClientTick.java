package dev.lazurite.fpvracing.client;

import dev.lazurite.fpvracing.server.entity.FlyableEntity;
import dev.lazurite.fpvracing.server.entity.flyable.QuadcopterEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public class ClientTick {
    public static boolean isServerModded = false;
    public static boolean shouldRenderPlayer = false;
    private static double prevFOV;

    public static void tick(MinecraftClient client) {
        if (client.player != null && !client.isPaused()) {
            if (client.getCameraEntity() instanceof FlyableEntity) {
                FlyableEntity flyable = (FlyableEntity) client.getCameraEntity();
                float droneFOV = flyable.getValue(FlyableEntity.FIELD_OF_VIEW);

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

    /**
     * Finds out whether or not the player is in the goggles
     * by checking what the client camera entity is.
     * @param client the minecraft client to use
     * @return whether or not the player is in goggles
     */
    public static boolean isInGoggles(MinecraftClient client) {
        return client.getCameraEntity() instanceof QuadcopterEntity;
    }

    public static void register() {
        ClientTickCallback.EVENT.register(ClientTick::tick);
    }
}
