package dev.lazurite.quadz.client;

import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.api.InputHandler;
import dev.lazurite.quadz.api.event.ClickEvents;
import dev.lazurite.quadz.api.event.JoystickEvents;
import dev.lazurite.quadz.client.keybind.CameraAngleKeybinds;
import dev.lazurite.quadz.client.keybind.FollowKeybind;
import dev.lazurite.quadz.client.keybind.NoClipKeybind;
import dev.lazurite.quadz.client.render.renderer.QuadcopterRenderer;
import dev.lazurite.quadz.client.render.model.RemoteControllableItemModel;
import dev.lazurite.quadz.client.render.renderer.GogglesItemRenderer;
import dev.lazurite.quadz.client.render.ui.toast.ControllerConnectedToast;
import dev.lazurite.quadz.client.resource.SplashResourceLoader;
import dev.lazurite.quadz.client.util.CameraTypeAccess;
import dev.lazurite.quadz.common.data.Config;
import dev.lazurite.quadz.common.data.template.TemplateLoader;
import dev.lazurite.quadz.common.data.template.util.TemplateResourceLoader;
import dev.lazurite.quadz.common.entity.RemoteControllableEntity;
import dev.lazurite.quadz.common.util.network.NetworkResources;
import dev.lazurite.toolbox.api.event.ClientEvents;
import dev.lazurite.toolbox.api.network.ClientNetworking;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import software.bernie.geckolib3.GeckoLib;
import software.bernie.geckolib3.renderers.geo.GeoArmorRenderer;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;

public class QuadzClient implements ClientModInitializer {
    public static final TemplateResourceLoader templateLoader = new TemplateResourceLoader();

    @Override
    public void onInitializeClient() {
        GeckoLib.initialize();
        GeoArmorRenderer.registerArmorRenderer(new GogglesItemRenderer(), Quadz.GOGGLES_ITEM);
        GeoItemRenderer.registerItemRenderer(Quadz.QUADCOPTER_ITEM, new GeoItemRenderer<>(new RemoteControllableItemModel()));

        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(templateLoader);
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SplashResourceLoader());
        EntityRendererRegistry.register(Quadz.QUADCOPTER_ENTITY, QuadcopterRenderer::new);

        /* Register Keybindings */
        CameraAngleKeybinds.register();
        NoClipKeybind.register();
        FollowKeybind.register();

        /* Register Packets */
        NetworkResources.registerClientbound();

        /* Register Toast Events */
        JoystickEvents.JOYSTICK_CONNECT.register(this::onJoystickConnect);
        JoystickEvents.JOYSTICK_DISCONNECT.register(this::onJoystickDisconnect);

        /* Register Client Tick Events */
        ClientEvents.Tick.START_LEVEL_TICK.register(this::onClientLevelTick);

        /* Set up events */
        ClickEvents.LEFT_CLICK_EVENT.register(this::onLeftClick);
        ClickEvents.RIGHT_CLICK_EVENT.register(this::onRightClick);
        ClientEvents.Lifecycle.DISCONNECT.register(this::onDisconnect);
        ClientEvents.Lifecycle.POST_LOGIN.register(this::onPostLogin);
    }

    public static boolean isInThirdPerson() {
        return !Minecraft.getInstance().options.getCameraType().isFirstPerson();
    }

    public void onClientLevelTick(ClientLevel level) {
        final var client = Minecraft.getInstance();

        if (!client.isPaused()) {
            if (client.options.keyShift.isDown() && client.cameraEntity instanceof RemoteControllableEntity) {
                ((CameraTypeAccess) (Object) client.options.getCameraType()).reset();
            }
        }

        InputHandler.sync();
    }

    protected void onJoystickConnect(int id, String name) {
        ControllerConnectedToast.add(Component.translatable("toast.quadz.controller.connect"), name);
    }

    protected void onJoystickDisconnect(int id, String name) {
        ControllerConnectedToast.add(Component.translatable("toast.quadz.controller.disconnect"), name);
    }

    protected void onLeftClick() {
        ClientNetworking.send(NetworkResources.REQUEST_REMOTE_CONTROLLABLE_VIEW_C2S, buf -> buf.writeInt(-1));
    }

    protected void onRightClick() {
        ClientNetworking.send(NetworkResources.REQUEST_REMOTE_CONTROLLABLE_VIEW_C2S, buf -> buf.writeInt(1));
    }

    protected void onDisconnect(Minecraft minecraft, ClientLevel level) {
        TemplateLoader.clearRemoteTemplates();
    }

    protected void onPostLogin(Minecraft minecraft, ClientLevel level, LocalPlayer player) {
        Config.load();

        TemplateLoader.getTemplates().forEach(template -> ClientNetworking.send(NetworkResources.TEMPLATE, template::serialize));
    }
}
