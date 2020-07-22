package bluevista.fpvracingmod.client;

import bluevista.fpvracingmod.client.config.Config;
import bluevista.fpvracingmod.client.controller.Controller;
import bluevista.fpvracingmod.client.input.keybinds.EMPKeybind;
import bluevista.fpvracingmod.client.input.keybinds.NoClipKeybind;
import bluevista.fpvracingmod.client.input.keybinds.RemoveGogglesKeybind;
import bluevista.fpvracingmod.network.DroneInfoToClient;
import bluevista.fpvracingmod.network.GogglesInfoToClient;
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

//            setConfigBand(Integer.parseInt(config.getValue("band")));
//            setConfigChannel(Integer.parseInt(config.getValue("channel")));
//            setConfigCameraAngle(Integer.parseInt(config.getValue("cameraAngle")));
        } catch (Exception e) {
            System.err.println("Error loading config");
            e.printStackTrace();
        }
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
    }

    public static Config getConfig() {
        return config;
    }
}
