package bluevista.fpvracingmod.client;

import bluevista.fpvracingmod.client.config.Config;
import bluevista.fpvracingmod.client.controller.Controller;
import bluevista.fpvracingmod.client.input.InputTick;
import bluevista.fpvracingmod.client.input.RemoveGogglesKeybinding;
import bluevista.fpvracingmod.server.ServerInitializer;
import bluevista.fpvracingmod.client.network.SpawnNetworkHandler;
import bluevista.fpvracingmod.client.renderers.DroneRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class ClientInitializer implements ClientModInitializer {

    private static Config config;

    @Override
    public void onInitializeClient() {
        config = new Config("fpvracing", new String[] {"controllerID",  "throttle", "pitch", "yaw", "roll", "deadzone", "rate", "superRate", "expo"});

        EntityRendererRegistry.INSTANCE.register(ServerInitializer.DRONE_ENTITY, (entityRenderDispatcher, context) -> new DroneRenderer(entityRenderDispatcher));
        ClientSidePacketRegistry.INSTANCE.register(new Identifier("fpvracing", "spawn_drone"), SpawnNetworkHandler::accept);
        ClientTickCallback.EVENT.register(ClientTick::tick);
        ClientTickCallback.EVENT.register(RemoveGogglesKeybinding::callback);

        initControllerSettings();
    }

    private void initControllerSettings() {
        try {
            Controller.setControllerId(Integer.parseInt(config.getValue("controllerID")));
            Controller.setThrottle(Integer.parseInt(config.getValue("throttle")));
            Controller.setPitch(Integer.parseInt(config.getValue("pitch")));
            Controller.setYaw(Integer.parseInt(config.getValue("yaw")));
            Controller.setRoll(Integer.parseInt(config.getValue("roll")));
            Controller.setDeadzone(Float.parseFloat(config.getValue("deadzone")));
            Controller.setRate(Float.parseFloat(config.getValue("rate")));
            Controller.setSuperRate(Float.parseFloat(config.getValue("superRate")));
            Controller.setExpo(Float.parseFloat(config.getValue("expo")));
        } catch (Exception e) {
            System.err.println("Error loading config");
            e.printStackTrace();
        }
    }

    public static Config getConfig() {
        return config;
    }
}
