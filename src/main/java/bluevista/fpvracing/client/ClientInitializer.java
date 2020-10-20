package bluevista.fpvracing.client;

import bluevista.fpvracing.client.input.keybinds.EMPKeybind;
import bluevista.fpvracing.client.input.keybinds.GodModeKeybind;
import bluevista.fpvracing.client.input.keybinds.GogglePowerKeybind;
import bluevista.fpvracing.client.input.keybinds.NoClipKeybind;
import bluevista.fpvracing.client.renderers.FixedWingRenderer;
import bluevista.fpvracing.client.renderers.QuadcopterRenderer;
import bluevista.fpvracing.client.renderers.QuadcopterItemRenderer;
import bluevista.fpvracing.config.Config;
import bluevista.fpvracing.config.ConfigReader;
import bluevista.fpvracing.network.ModdedServerS2C;
import bluevista.fpvracing.network.SelectedSlotS2C;
import bluevista.fpvracing.network.config.ConfigS2C;
import bluevista.fpvracing.network.entity.EntityPhysicsS2C;
import bluevista.fpvracing.physics.PhysicsWorld;
import bluevista.fpvracing.network.ShouldRenderPlayerS2C;
import bluevista.fpvracing.server.ServerInitializer;
import bluevista.fpvracing.server.entities.QuadcopterEntity;
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

    /** The physics world used for {@link QuadcopterEntity} objects. */
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

        GogglePowerKeybind.register();
        GodModeKeybind.register();
        NoClipKeybind.register();
        EMPKeybind.register();

        EntityPhysicsS2C.register();
        SelectedSlotS2C.register();
        ShouldRenderPlayerS2C.register();
        ModdedServerS2C.register();
        ConfigS2C.register();

        EntityRendererRegistry.INSTANCE.register(ServerInitializer.QUADCOPTER_ENTITY, (entityRenderDispatcher, context) -> new QuadcopterRenderer(entityRenderDispatcher));
        EntityRendererRegistry.INSTANCE.register(ServerInitializer.FIXED_WING_ENTITY, (entityRenderDispatcher, context) -> new FixedWingRenderer(entityRenderDispatcher));
        QuadcopterItemRenderer.register();
    }

    /**
     * Loads the player's config from disk.
     */
    public static void registerConfig() {
        config = new Config(new ConfigReader());
        config.loadConfig();
    }

    /**
     * Get the config object created in {@link ClientInitializer#registerConfig()}.
     * @return the {@link Config} object being stored
     */
    public static Config getConfig() {
        return config;
    }
}
