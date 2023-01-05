package dev.lazurite.quadz.client;

import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.client.render.entity.QuadcopterEntityRenderer;
import dev.lazurite.quadz.common.util.Bindable;
import dev.lazurite.quadz.common.util.Search;
import dev.lazurite.quadz.common.util.event.ClickEvents;
import dev.lazurite.quadz.common.util.event.JoystickEvents;
import dev.lazurite.quadz.client.hooks.ClientEventHooks;
import dev.lazurite.quadz.client.hooks.ClientNetworkEventHooks;
import dev.lazurite.quadz.client.resource.SplashResourceLoader;
import dev.lazurite.quadz.common.entity.Quadcopter;
import dev.lazurite.toolbox.api.event.ClientEvents;
import dev.lazurite.toolbox.api.network.PacketRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.PackType;

import java.util.Optional;

public class QuadzClient implements ClientModInitializer {

    public static boolean isInThirdPerson() {
        return !Minecraft.getInstance().options.getCameraType().isFirstPerson();
    }

    /**
     * Finds the player's quadcopter if it's within a 256 block radius.
     */
    public static Optional<Quadcopter> getQuadcopter() {
        var player = Minecraft.getInstance().player;

        if (player != null) {
            return Bindable.get(player.getMainHandItem()).flatMap(remote -> Search.byBindId(player.level, player.position(), remote.getBindId(), 256));
    }

        return Optional.empty();
    }

    @Override
    public void onInitializeClient() {
        // Renderer
        EntityRendererRegistry.register(Quadz.QUADCOPTER, QuadcopterEntityRenderer::new);

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

        // Network events
        PacketRegistry.registerClientbound(Quadz.Networking.JOYSTICK_INPUT, ClientNetworkEventHooks::onJoystickInput);
    }

}
