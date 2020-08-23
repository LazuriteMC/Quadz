package bluevista.fpvracingmod.client;

import bluevista.fpvracingmod.client.input.keybinds.EMPKeybind;
import bluevista.fpvracingmod.client.input.keybinds.GodModeKeybind;
import bluevista.fpvracingmod.client.input.keybinds.GogglePowerKeybind;
import bluevista.fpvracingmod.client.input.keybinds.NoClipKeybind;
import bluevista.fpvracingmod.client.renderers.DroneRenderer;
import bluevista.fpvracingmod.config.Config;
import bluevista.fpvracingmod.network.config.ConfigS2C;
import bluevista.fpvracingmod.network.entity.DroneEntityS2C;
import bluevista.fpvracingmod.client.physics.PhysicsWorld;
import bluevista.fpvracingmod.server.ServerInitializer;
import bluevista.fpvracingmod.server.entities.DroneEntity;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class ClientInitializer implements ClientModInitializer {

    public static PhysicsWorld physicsWorld;
    private static Config config;

    @Override
    public void onInitializeClient() {
        GLFW.glfwInit(); // forcefully initializes GLFW

        ClientTick.register();
        registerKeybinds();
        registerRenderers();
        registerNetwork();
        registerConfig();
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
    }

    private void registerNetwork() {
        DroneEntityS2C.register();
        ConfigS2C.register();
    }

    public static Config getConfig() {
        return config;
    }

    public static boolean isInGoggles(MinecraftClient client) {
        return client.getCameraEntity() instanceof DroneEntity;
    }
}
