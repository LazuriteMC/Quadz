package bluevista.fpvracingmod.client;

import bluevista.fpvracingmod.client.input.keybinds.*;
import bluevista.fpvracingmod.network.DroneInfoS2C;
import bluevista.fpvracingmod.network.DroneQuaternionS2C;
import bluevista.fpvracingmod.network.ConfigS2C;
import bluevista.fpvracingmod.config.Config;
import bluevista.fpvracingmod.server.ServerInitializer;
import bluevista.fpvracingmod.client.renderers.DroneRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class ClientInitializer implements ClientModInitializer {

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
        DroneInfoS2C.register();
        DroneQuaternionS2C.register();
        ConfigS2C.register();
    }

    public static Config getConfig() {
        return config;
    }

}
