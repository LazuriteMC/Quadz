package bluevista.fpvracingmod.client;

import bluevista.fpvracingmod.server.entities.DroneEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

import java.util.UUID;

@Environment(EnvType.CLIENT)
public class ClientHelper {
    public static boolean isInGoggles(MinecraftClient client) {
        return client.getCameraEntity() instanceof DroneEntity;
    }

    public static boolean isPlayerIDClient(UUID playerID) {
        if(ClientInitializer.client.player != null) {
            return playerID.equals(ClientInitializer.client.player.getUuid());
        } else {
            return false;
        }
    }
}
