package bluevista.fpvracingmod.client;

import bluevista.fpvracingmod.client.config.Config;
import bluevista.fpvracingmod.client.controller.Controller;
import bluevista.fpvracingmod.client.input.keybinds.EMPKeybind;
import bluevista.fpvracingmod.client.input.keybinds.NoClipKeybind;
import bluevista.fpvracingmod.client.input.keybinds.PowerOffGogglesKeybind;
import bluevista.fpvracingmod.client.input.keybinds.PowerOnGogglesKeybind;
import bluevista.fpvracingmod.network.DroneInfoS2C;
import bluevista.fpvracingmod.network.DroneQuaternionS2C;
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
        } catch (Exception e) {
            System.err.println("Error loading config");
            e.printStackTrace();
        }
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
    }

    public static Config getConfig() {
        return config;
    }
}
