package dev.lazurite.quadz.client;

import dev.lazurite.form.impl.client.render.model.TemplatedModel;
import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.client.keybind.NoClipKeybind;
import dev.lazurite.quadz.client.render.renderer.QuadcopterRenderer;
import dev.lazurite.quadz.client.render.renderer.GogglesItemRenderer;
import dev.lazurite.quadz.client.resource.SplashResourceLoader;
import dev.lazurite.remote.api.input.JoystickOutput;
import dev.lazurite.remote.impl.common.util.PlayerStorage;
import dev.lazurite.toolbox.api.event.ClientEvents;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import software.bernie.geckolib3.renderers.geo.GeoArmorRenderer;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;

public class QuadzClient implements ClientModInitializer {
    public static final ResourceLocation NOCLIP_C2S = new ResourceLocation(Quadz.MODID, "noclip_c2s");

    @Override
    public void onInitializeClient() {
        Config.load();

        /* Register Renderers */
        GeoArmorRenderer.registerArmorRenderer(new GogglesItemRenderer(), Quadz.GOGGLES_ITEM);
        GeoItemRenderer.registerItemRenderer(Quadz.QUADCOPTER_ITEM, new GeoItemRenderer<>(new TemplatedModel<>()));
        EntityRendererRegistry.register(Quadz.QUADCOPTER_ENTITY, QuadcopterRenderer::new);

        /* Register Splash Screen Text Manager */
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SplashResourceLoader());

        /* Register Keybinding */
        NoClipKeybind.register();

        ClientEvents.Tick.START_CLIENT_TICK.register(this::onClientTick);
        ClientEvents.Lifecycle.POST_LOGIN.register(this::onPostLogin);
    }

    protected void onPostLogin(Minecraft minecraft, ClientLevel level, LocalPlayer player) {
        if (player != null) {
            ((PlayerStorage) player).setJoystickValue(new ResourceLocation(Quadz.MODID, "rate"), Config.rate);
            ((PlayerStorage) player).setJoystickValue(new ResourceLocation(Quadz.MODID, "super_rate"), Config.superRate);
            ((PlayerStorage) player).setJoystickValue(new ResourceLocation(Quadz.MODID, "expo"), Config.expo);
        }
    }

    protected void onClientTick(Minecraft minecraft) {
        if (!minecraft.isPaused() && minecraft.player != null && JoystickOutput.controllerExists()) {
            JoystickOutput.getAxisValue(minecraft.player, Config.pitch, new ResourceLocation(Quadz.MODID, "pitch"), Config.pitchInverted, false);
            JoystickOutput.getAxisValue(minecraft.player, Config.yaw, new ResourceLocation(Quadz.MODID, "yaw"), Config.yawInverted, false);
            JoystickOutput.getAxisValue(minecraft.player, Config.roll, new ResourceLocation(Quadz.MODID, "roll"), Config.rollInverted, false);
            JoystickOutput.getAxisValue(minecraft.player, Config.throttle, new ResourceLocation(Quadz.MODID, "throttle"), Config.throttleInverted, Config.throttleInCenter);
        }
    }
}
