package bluevista.fpvracingmod.client;

import bluevista.fpvracingmod.client.input.keybinds.EMPKeybind;
import bluevista.fpvracingmod.client.input.keybinds.NoClipKeybind;
import bluevista.fpvracingmod.client.input.keybinds.RemoveGogglesKeybind;
import bluevista.fpvracingmod.network.DroneInfoToClient;
import bluevista.fpvracingmod.network.GogglesInfoToClient;
import bluevista.fpvracingmod.network.ConfigS2C;
import bluevista.fpvracingmod.config.Config;
import bluevista.fpvracingmod.server.ServerInitializer;
import bluevista.fpvracingmod.client.renderers.DroneRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;

@Environment(EnvType.CLIENT)
public class ClientInitializer implements ClientModInitializer {

    private static Config config;

    @Override
    public void onInitializeClient() {
        ClientTick.register();

        registerKeybinds();
        registerRenderers();
        registerNetwork();
        registerConfig();
    }

    private void registerConfig() {
        config = new Config();
        config.loadConfig();
    }

    private void registerKeybinds() {
        RemoveGogglesKeybind.register();
        EMPKeybind.register();
        NoClipKeybind.register();
    }

    private void registerRenderers() {
        EntityRendererRegistry.INSTANCE.register(ServerInitializer.DRONE_ENTITY, (entityRenderDispatcher, context) -> new DroneRenderer(entityRenderDispatcher));
    }

    private void registerNetwork() {
        DroneInfoToClient.register();
        GogglesInfoToClient.register();
        ConfigS2C.register();
    }

    public static Config getConfig() {
        return config;
    }

}
