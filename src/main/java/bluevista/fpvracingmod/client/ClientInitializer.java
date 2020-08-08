package bluevista.fpvracingmod.client;

import bluevista.fpvracingmod.client.input.keybinds.EMPKeybind;
import bluevista.fpvracingmod.client.input.keybinds.NoClipKeybind;
import bluevista.fpvracingmod.client.input.keybinds.PowerOffGogglesKeybind;
import bluevista.fpvracingmod.client.input.keybinds.PowerOnGogglesKeybind;
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
        PowerOffGogglesKeybind.register();
        PowerOnGogglesKeybind.register();
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
