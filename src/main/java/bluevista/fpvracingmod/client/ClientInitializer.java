package bluevista.fpvracingmod.client;

import bluevista.fpvracingmod.FPVRacingMod;
import bluevista.fpvracingmod.client.network.DroneSpawnNetworkHandler;
import bluevista.fpvracingmod.client.renderers.DroneRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.util.Identifier;

public class ClientInitializer implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.INSTANCE.register(FPVRacingMod.DRONE_ENTITY, (entityRenderDispatcher, context) -> new DroneRenderer(entityRenderDispatcher));
        ClientSidePacketRegistry.INSTANCE.register(new Identifier("fpvracing", "spawn_drone"), DroneSpawnNetworkHandler::accept);
    }
}
