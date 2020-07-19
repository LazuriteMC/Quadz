package bluevista.fpvracingmod.client;

import bluevista.fpvracingmod.client.config.Config;
import bluevista.fpvracingmod.client.controller.Controller;
import bluevista.fpvracingmod.client.input.EMPKeybinding;
import bluevista.fpvracingmod.client.input.RemoveGogglesKeybinding;
import bluevista.fpvracingmod.server.ServerInitializer;
import bluevista.fpvracingmod.client.renderers.DroneRenderer;
import bluevista.fpvracingmod.server.entities.DroneEntity;
import bluevista.fpvracingmod.network.QuaternionPacketHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;

@Environment(EnvType.CLIENT)
public class ClientInitializer implements ClientModInitializer {

    private static Config config;

    @Override
    public void onInitializeClient() {
        RemoveGogglesKeybinding.register();
        EMPKeybinding.register();
        ClientTick.register();
        QuaternionPacketHandler.register();

        registerRenderers();
        initControllerSettings();
    }

    private void initControllerSettings() {
        config = new Config(ServerInitializer.MODID);

        try {
            Controller.setControllerId(Integer.parseInt(config.getValue("controllerID")));
            Controller.setThrottleNum(Integer.parseInt(config.getValue("throttle")));
            Controller.setPitchNum(Integer.parseInt(config.getValue("pitch")));
            Controller.setYawNum(Integer.parseInt(config.getValue("yaw")));
            Controller.setRollNum(Integer.parseInt(config.getValue("roll")));
            Controller.setDeadzone(Float.parseFloat(config.getValue("deadzone")));
            Controller.setThrottleCenterPosition(Integer.parseInt(config.getValue("throttleCenterPosition")));
            Controller.setRate(Float.parseFloat(config.getValue("rate")));
            Controller.setSuperRate(Float.parseFloat(config.getValue("superRate")));
            Controller.setExpo(Float.parseFloat(config.getValue("expo")));
            Controller.setInvertThrottle(Integer.parseInt(config.getValue("invertThrottle")));
            Controller.setInvertPitch(Integer.parseInt(config.getValue("invertPitch")));
            Controller.setInvertYaw(Integer.parseInt(config.getValue("invertYaw")));
            Controller.setInvertRoll(Integer.parseInt(config.getValue("invertRoll")));

            DroneEntity.setCameraAngle(Float.parseFloat(config.getValue("cameraAngle")));
        } catch (Exception e) {
            System.err.println("Error loading config");
            e.printStackTrace();
        }
    }

    private void registerRenderers() {
        EntityRendererRegistry.INSTANCE.register(ServerInitializer.DRONE_ENTITY, (entityRenderDispatcher, context) -> new DroneRenderer(entityRenderDispatcher));
    }

    public static Config getConfig() {
        return config;
    }
}
