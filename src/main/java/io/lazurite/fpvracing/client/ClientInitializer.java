package io.lazurite.fpvracing.client;

import io.lazurite.fpvracing.client.input.keybinds.EMPKeybind;
import io.lazurite.fpvracing.client.input.keybinds.GodModeKeybind;
import io.lazurite.fpvracing.client.input.keybinds.GogglePowerKeybind;
import io.lazurite.fpvracing.client.input.keybinds.NoClipKeybind;
import io.lazurite.fpvracing.client.renderer.FixedWingRenderer;
import io.lazurite.fpvracing.client.renderer.QuadcopterRenderer;
import io.lazurite.fpvracing.client.renderer.QuadcopterItemRenderer;
import io.lazurite.fpvracing.network.tracker.Config;
import io.lazurite.fpvracing.network.packet.*;
import io.lazurite.fpvracing.network.tracker.ConfigFile;
import io.lazurite.fpvracing.physics.PhysicsWorld;
import io.lazurite.fpvracing.server.ServerInitializer;
import io.lazurite.fpvracing.server.entity.flyable.QuadcopterEntity;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

/**
 * This class is responsible for loading all of the client-side
 * registries and configurations.
 */
@Environment(EnvType.CLIENT)
public class ClientInitializer implements ClientModInitializer {

    /** The running instance of the minecraft client. */
    public static final MinecraftClient client = MinecraftClient.getInstance();

    /** The physics world used for {@link QuadcopterEntity} objects. */
    public static PhysicsWorld physicsWorld;

    /** The client player's config which is later sent to the server. */
    public static final String CONFIG_NAME = ServerInitializer.MODID + ".properties";
    public static Config config;

    /**
     * Initializes all of the registries and loads the player config.
     */
    @Override
    public void onInitializeClient() {
        GLFW.glfwInit(); // forcefully initializes GLFW

        config = ConfigFile.readConfig(CONFIG_NAME);
        ClientTick.register();

        GogglePowerKeybind.register();
        GodModeKeybind.register();
        NoClipKeybind.register();
        EMPKeybind.register();

        PhysicsHandlerS2C.register();
        SelectedSlotS2C.register();
        ShouldRenderPlayerS2C.register();
        ModdedServerS2C.register();
        ConfigCommandS2C.register();
        ConfigValueS2C.register();

        QuadcopterItemRenderer.register();
        QuadcopterRenderer.register();
        FixedWingRenderer.register();
    }

    /**
     * Get the config object.
     * @return the {@link Config} object being stored
     */
    public static Config getConfig() {
        return config;
    }
}
