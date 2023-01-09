package dev.lazurite.quadz.client.hooks;

import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.common.util.JoystickOutput;
import dev.lazurite.quadz.client.Config;
import dev.lazurite.quadz.client.render.screen.ControllerConnectedToast;
import dev.lazurite.quadz.common.entity.Quadcopter;
import dev.lazurite.toolbox.api.network.ClientNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ClientEventHooks {

    public static void onPostLogin(Minecraft minecraft, ClientLevel level, LocalPlayer player) {
        Config.load();

        if (player != null) {
            player.setJoystickValue(new ResourceLocation(Quadz.MODID, "rate"), Config.rate);
            player.setJoystickValue(new ResourceLocation(Quadz.MODID, "super_rate"), Config.superRate);
            player.setJoystickValue(new ResourceLocation(Quadz.MODID, "expo"), Config.expo);
        }
    }

    public static void onClientTick(Minecraft minecraft) {
        if (!minecraft.isPaused() && minecraft.player != null && JoystickOutput.controllerExists()) {
            JoystickOutput.getAxisValue(minecraft.player, Config.pitch, new ResourceLocation(Quadz.MODID, "pitch"), Config.pitchInverted, false);
            JoystickOutput.getAxisValue(minecraft.player, Config.yaw, new ResourceLocation(Quadz.MODID, "yaw"), Config.yawInverted, false);
            JoystickOutput.getAxisValue(minecraft.player, Config.roll, new ResourceLocation(Quadz.MODID, "roll"), Config.rollInverted, false);
            JoystickOutput.getAxisValue(minecraft.player, Config.throttle, new ResourceLocation(Quadz.MODID, "throttle"), Config.throttleInverted, Config.throttleInCenter);
        }
    }

    public static void onJoystickConnect(int id, String name) {
        ControllerConnectedToast.add(Component.translatable("quadz.toast.connect"), name);
    }

    public static void onJoystickDisconnect(int id, String name) {
        ControllerConnectedToast.add(Component.translatable("quadz.toast.disconnect"), name);
    }

    public static void onLeftClick() {
        ClientNetworking.send(Quadz.Networking.REQUEST_REMOTE_CONTROLLABLE_VIEW, buf -> buf.writeInt(-1));
    }

    public static void onRightClick() {
        ClientNetworking.send(Quadz.Networking.REQUEST_REMOTE_CONTROLLABLE_VIEW, buf -> buf.writeInt(1));
    }

    public static void onClientLevelTick(ClientLevel level) {
        final var client = Minecraft.getInstance();

        if (!client.isPaused()) {
            client.player.syncJoystick();

            if (client.options.keyShift.isDown() && client.cameraEntity instanceof Quadcopter) {
                client.options.getCameraType().reset();
            }
        }
    }

}
