package dev.lazurite.quadz.client;

import dev.lazurite.corduroy.api.ViewStack;
import dev.lazurite.form.api.Templated;
import dev.lazurite.form.api.render.FormRegistry;
import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.client.render.camera.QuadcopterView;
import dev.lazurite.quadz.client.render.RenderHooks;
import dev.lazurite.quadz.client.render.entity.QuadcopterEntityRenderer;
import dev.lazurite.quadz.common.util.Bindable;
import dev.lazurite.quadz.common.util.Search;
import dev.lazurite.quadz.common.util.event.ClickEvents;
import dev.lazurite.quadz.common.util.event.JoystickEvents;
import dev.lazurite.quadz.client.event.ClientEventHooks;
import dev.lazurite.quadz.client.event.ClientNetworkEventHooks;
import dev.lazurite.quadz.client.resource.SplashResourceLoader;
import dev.lazurite.quadz.common.entity.Quadcopter;
import dev.lazurite.toolbox.api.event.ClientEvents;
import dev.lazurite.toolbox.api.network.PacketRegistry;
import ladysnake.satin.api.event.ShaderEffectRenderCallback;
import ladysnake.satin.api.managed.ManagedShaderEffect;
import ladysnake.satin.api.managed.ShaderEffectManager;
import ladysnake.satin.api.managed.uniform.Uniform1f;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;

import java.util.Optional;

public class QuadzClient implements ClientModInitializer {

    // Shaders
    public static final ManagedShaderEffect STATIC_SHADER = ShaderEffectManager.getInstance()
            .manage(new ResourceLocation(Quadz.MODID, "shaders/post/static.json"));
    public static final ManagedShaderEffect FISHEYE_SHADER = ShaderEffectManager.getInstance()
            .manage(new ResourceLocation(Quadz.MODID, "shaders/post/fisheye.json"));
    public static final Uniform1f STATIC_TIMER = STATIC_SHADER.findUniform1f("Time");
    public static final Uniform1f STATIC_AMOUNT = STATIC_SHADER.findUniform1f("Amount");
    public static final Uniform1f FISHEYE_AMOUNT = FISHEYE_SHADER.findUniform1f("Amount");

    /**
     * Finds the player's quadcopter based on its camera view.
     */
    public static Optional<Quadcopter> getQuadcopterFromCamera() {
        return ViewStack.getInstance()
                .peek()
                .filter(view -> view instanceof QuadcopterView)
                .map(view -> ((QuadcopterView) view).getQuadcopter());
    }

    /**
     * Finds the player's quadcopter based on its held remote.
     * <p>
     * Only works if it's within a 256 block radius.
     */
    public static Optional<Quadcopter> getQuadcopterFromRemote() {
        var player = Minecraft.getInstance().player;

        if (player != null) {
            return Bindable.get(player.getMainHandItem()).flatMap(remote -> Search.forQuadWithBindId(player.level, player.position(), remote.getBindId(), 256));
        }

        return Optional.empty();
    }

    @Override
    public void onInitializeClient() {
        // Renderer
        EntityRendererRegistry.register(Quadz.QUADCOPTER, QuadcopterEntityRenderer::new);
        FormRegistry.register((Templated.Item) Quadz.QUADCOPTER_ITEM);

        // Splash screen text injection
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SplashResourceLoader());

        // Events
        ClientEvents.Lifecycle.POST_LOGIN.register(ClientEventHooks::onPostLogin);
        ClientEvents.Tick.START_CLIENT_TICK.register(ClientEventHooks::onClientTick);
        ClientEvents.Tick.START_LEVEL_TICK.register(ClientEventHooks::onClientLevelTick);
        JoystickEvents.JOYSTICK_CONNECT.register(ClientEventHooks::onJoystickConnect);
        JoystickEvents.JOYSTICK_DISCONNECT.register(ClientEventHooks::onJoystickDisconnect);
        ClickEvents.LEFT_CLICK_EVENT.register(ClientEventHooks::onLeftClick);
        ClickEvents.RIGHT_CLICK_EVENT.register(ClientEventHooks::onRightClick);
        ShaderEffectRenderCallback.EVENT.register(RenderHooks::onRenderShaders);

        // Network events
        PacketRegistry.registerClientbound(Quadz.Networking.JOYSTICK_INPUT, ClientNetworkEventHooks::onJoystickInput);
    }

}
