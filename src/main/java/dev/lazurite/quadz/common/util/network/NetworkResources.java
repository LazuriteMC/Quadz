package dev.lazurite.quadz.common.util.network;

import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.client.util.network.ClientNetworkHandler;
import dev.lazurite.toolbox.api.network.PacketRegistry;
import net.minecraft.resources.ResourceLocation;

public final class NetworkResources {
    public static final ResourceLocation INPUT_FRAME = new ResourceLocation(Quadz.MODID, "input_frame");
    public static final ResourceLocation TEMPLATE = new ResourceLocation(Quadz.MODID, "template_s2c");
    public static final ResourceLocation REQUEST_REMOTE_CONTROLLABLE_VIEW_C2S = new ResourceLocation(Quadz.MODID, "request_remote_controllable_view_c2s");
    public static final ResourceLocation REQUEST_PLAYER_VIEW_C2S = new ResourceLocation(Quadz.MODID, "request_player_view_c2s");
    public static final ResourceLocation NOCLIP_C2S = new ResourceLocation(Quadz.MODID, "noclip_c2s");
    public static final ResourceLocation CHANGE_CAMERA_ANGLE_C2S = new ResourceLocation(Quadz.MODID, "change_camera_angle_c2s");

    public static void registerServerbound() {
        PacketRegistry.registerServerbound(NetworkResources.TEMPLATE, CommonNetworkHandler::onTemplateReceived);
        PacketRegistry.registerServerbound(NetworkResources.INPUT_FRAME, CommonNetworkHandler::onInputFrame);
        PacketRegistry.registerServerbound(NetworkResources.REQUEST_REMOTE_CONTROLLABLE_VIEW_C2S, CommonNetworkHandler::onRemoteControllableViewRequestReceived);
        PacketRegistry.registerServerbound(NetworkResources.REQUEST_PLAYER_VIEW_C2S, CommonNetworkHandler::onPlayerViewRequestReceived);

        PacketRegistry.registerServerbound(NetworkResources.NOCLIP_C2S, KeybindNetworkHandler::onNoClipKey);
        PacketRegistry.registerServerbound(NetworkResources.CHANGE_CAMERA_ANGLE_C2S, KeybindNetworkHandler::onChangeCameraAngleKey);
    }

    public static void registerClientbound() {
        PacketRegistry.registerClientbound(NetworkResources.TEMPLATE, ClientNetworkHandler::onTemplateReceived);
        PacketRegistry.registerClientbound(NetworkResources.INPUT_FRAME, ClientNetworkHandler::onInputFrameReceived);
    }
}
