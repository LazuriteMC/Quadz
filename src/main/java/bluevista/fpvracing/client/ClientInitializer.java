package bluevista.fpvracing.client;

import bluevista.fpvracing.client.input.keybinds.EMPKeybind;
import bluevista.fpvracing.client.input.keybinds.GodModeKeybind;
import bluevista.fpvracing.client.input.keybinds.GogglePowerKeybind;
import bluevista.fpvracing.client.input.keybinds.NoClipKeybind;
import bluevista.fpvracing.client.renderers.DroneRenderer;
import bluevista.fpvracing.client.renderers.DroneSpawnerItemRenderer;
import bluevista.fpvracing.config.Config;
import bluevista.fpvracing.config.ConfigReader;
import bluevista.fpvracing.network.ModdedServerS2C;
import bluevista.fpvracing.network.SelectedSlotS2C;
import bluevista.fpvracing.network.config.ConfigS2C;
import bluevista.fpvracing.network.entity.DroneEntityS2C;
import bluevista.fpvracing.client.physics.PhysicsWorld;
import bluevista.fpvracing.network.ShouldRenderPlayerS2C;
import bluevista.fpvracing.server.ServerInitializer;
import bluevista.fpvracing.server.entities.DroneEntity;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
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

    /** The physics world used for {@link DroneEntity} objects. */
    public static PhysicsWorld physicsWorld;

    /** The client player's config which is later sent to the server. */
    private static Config config;

    /**
     * Initializes all of the registries and loads the player config.
     */
    @Override
    public void onInitializeClient() {
        GLFW.glfwInit(); // forcefully initializes GLFW

        ClientTick.register();
        registerConfig();
        registerKeybinds();
        registerRenderers();
        registerNetwork();
    }

    /**
     * Loads the player's config from disk.
     */
    public static void registerConfig() {
        config = new Config(new ConfigReader());
        config.loadConfig();
    }

    /**
     * Registers keybindings such as
     * god mode, noclip, EMP, etc.
     */
    private void registerKeybinds() {
        GogglePowerKeybind.register();
        GodModeKeybind.register();
        NoClipKeybind.register();
        EMPKeybind.register();
    }

    /**
     * Registers the renderers which use {@link bluevista.fpvracing.client.models.DroneModel}.
     */
    private void registerRenderers() {
        EntityRendererRegistry.INSTANCE.register(ServerInitializer.DRONE_ENTITY, (entityRenderDispatcher, context) -> new DroneRenderer(entityRenderDispatcher));
        DroneSpawnerItemRenderer.register();
    }

    /**
     * Registers the network packets which are sent
     * from the server and received by the client.
     */
    private void registerNetwork() {
        ConfigS2C.register();
        DroneEntityS2C.register();
        SelectedSlotS2C.register();
        ShouldRenderPlayerS2C.register();
        ModdedServerS2C.register();
    }

    /**
     * Get the config object created in {@link ClientInitializer#registerConfig()}.
     * @return the {@link Config} object being stored
     */
    public static Config getConfig() {
        return config;
    }

    /**
     * Finds out whether or not the player is in the goggles
     * by checking what the client camera entity is.
     * @param client the minecraft client to use
     * @return whether or not the player is in goggles
     */
    public static boolean isInGoggles(MinecraftClient client) {
        return client.getCameraEntity() instanceof DroneEntity;
    }
}
