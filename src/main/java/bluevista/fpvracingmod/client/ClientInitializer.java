package bluevista.fpvracingmod.client;

import bluevista.fpvracingmod.client.config.Config;
import bluevista.fpvracingmod.server.ServerInitializer;
import bluevista.fpvracingmod.client.network.SpawnNetworkHandler;
import bluevista.fpvracingmod.client.renderers.DroneRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.util.Identifier;

public class ClientInitializer implements ClientModInitializer {

    private static Config config;

    @Override
    public void onInitializeClient() {
        initRenderers();
        initNetwork();

        config = new Config("config/fpvracing.cfg");
        System.out.println("Hello statement: " + config.getConfigValue("hello"));
    }

    public void initRenderers() {
        EntityRendererRegistry.INSTANCE.register(ServerInitializer.DRONE_ENTITY, (entityRenderDispatcher, context) -> new DroneRenderer(entityRenderDispatcher));
    }

    public void initNetwork() {
        ClientSidePacketRegistry.INSTANCE.register(new Identifier("fpvracing", "spawn_drone"), SpawnNetworkHandler::accept);
    }

    public static Config getConfig() {
        return config;
    }
}
