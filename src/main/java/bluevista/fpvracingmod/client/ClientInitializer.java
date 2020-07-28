package bluevista.fpvracingmod.client;

import bluevista.fpvracingmod.config.ConfigReader;
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

    private static ConfigReader configReader;
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
        configReader = new ConfigReader();

        // this should be reworked so that if one value doesn't exist
        // it is the only one skipped and not every one past it
        try {
            config.getOptions().put(Config.CONTROLLER_ID, Integer.parseInt(configReader.getValue(Config.CONTROLLER_ID)));
            config.getOptions().put(Config.THROTTLE_NUM, Integer.parseInt(configReader.getValue(Config.THROTTLE_NUM)));
            config.getOptions().put(Config.PITCH_NUM, Integer.parseInt(configReader.getValue(Config.PITCH_NUM)));
            config.getOptions().put(Config.YAW_NUM, Integer.parseInt(configReader.getValue(Config.YAW_NUM)));
            config.getOptions().put(Config.ROLL_NUM, Integer.parseInt(configReader.getValue(Config.ROLL_NUM)));
            config.getOptions().put(Config.DEADZONE, Float.parseFloat(configReader.getValue(Config.DEADZONE)));
            config.getOptions().put(Config.THROTTLE_CENTER_POSITION, Integer.parseInt(configReader.getValue(Config.THROTTLE_CENTER_POSITION)));
            config.getOptions().put(Config.RATE, Float.parseFloat(configReader.getValue(Config.RATE)));
            config.getOptions().put(Config.SUPER_RATE, Float.parseFloat(configReader.getValue(Config.SUPER_RATE)));
            config.getOptions().put(Config.EXPO, Float.parseFloat(configReader.getValue(Config.EXPO)));
            config.getOptions().put(Config.INVERT_THROTTLE, Integer.parseInt(configReader.getValue(Config.INVERT_THROTTLE)));
            config.getOptions().put(Config.INVERT_PITCH, Integer.parseInt(configReader.getValue(Config.INVERT_PITCH)));
            config.getOptions().put(Config.INVERT_YAW, Integer.parseInt(configReader.getValue(Config.INVERT_YAW)));
            config.getOptions().put(Config.INVERT_ROLL, Integer.parseInt(configReader.getValue(Config.INVERT_ROLL)));
            config.getOptions().put(Config.CAMERA_ANGLE, Integer.parseInt(configReader.getValue(Config.CAMERA_ANGLE)));
            config.getOptions().put(Config.BAND, Integer.parseInt(configReader.getValue(Config.BAND)));
            config.getOptions().put(Config.CHANNEL, Integer.parseInt(configReader.getValue(Config.CHANNEL)));
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
        ConfigS2C.register();
    }

    public static ConfigReader getConfigReader() {
        return configReader;
    }

    public static Config getConfig() {
        return config;
    }

}
