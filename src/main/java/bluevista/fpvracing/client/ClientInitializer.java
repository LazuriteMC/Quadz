package bluevista.fpvracing.client;

import bluevista.fpvracing.client.input.keybinds.EMPKeybind;
import bluevista.fpvracing.client.input.keybinds.GodModeKeybind;
import bluevista.fpvracing.client.input.keybinds.GogglePowerKeybind;
import bluevista.fpvracing.client.input.keybinds.NoClipKeybind;
import bluevista.fpvracing.client.renderers.DroneRenderer;
import bluevista.fpvracing.client.renderers.DroneSpawnerItemRenderer;
import bluevista.fpvracing.config.Config;
import bluevista.fpvracing.network.config.ConfigS2C;
import bluevista.fpvracing.network.entity.DroneEntityS2C;
import bluevista.fpvracing.client.physics.PhysicsWorld;
import bluevista.fpvracing.network.entity.ShouldRenderPlayerS2C;
import bluevista.fpvracing.server.ServerInitializer;
import bluevista.fpvracing.server.entities.DroneEntity;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

import java.util.UUID;

@Environment(EnvType.CLIENT)
public class ClientInitializer implements ClientModInitializer {
    public static final MinecraftClient client = MinecraftClient.getInstance();
    public static PhysicsWorld physicsWorld;
    private static Config config;

    @Override
    public void onInitializeClient() {
        GLFW.glfwInit(); // forcefully initializes GLFW

        ClientTick.register();
        registerConfig();
        registerKeybinds();
        registerRenderers();
        registerNetwork();
    }

    public static void registerConfig() {
        config = new Config();
        config.loadConfig();
    }

    private void registerKeybinds() {
        GogglePowerKeybind.register();
        GodModeKeybind.register();
        NoClipKeybind.register();
        EMPKeybind.register();
    }

    private void registerRenderers() {
        EntityRendererRegistry.INSTANCE.register(ServerInitializer.DRONE_ENTITY, (entityRenderDispatcher, context) -> new DroneRenderer(entityRenderDispatcher));
        DroneSpawnerItemRenderer.register();
    }

    private void registerNetwork() {
        DroneEntityS2C.register();
        ShouldRenderPlayerS2C.register();
        ConfigS2C.register();
    }

    public static Config getConfig() {
        return config;
    }

    public static boolean isInGoggles(MinecraftClient client) {
        return client.getCameraEntity() instanceof DroneEntity;
    }

    public static boolean isPlayerIDClient(UUID playerID) {
        if (client.player != null) {
            return playerID.equals(client.player.getUuid());
        } else {
            return false;
        }
    }
}
