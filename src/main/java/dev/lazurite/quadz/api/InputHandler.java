package dev.lazurite.quadz.api;

import com.google.common.collect.Maps;
import dev.lazurite.quadz.common.data.Config;
import dev.lazurite.quadz.common.data.model.Bindable;
import dev.lazurite.quadz.common.data.model.JoystickAxis;
import dev.lazurite.quadz.common.util.tools.RemoteControllableSearch;
import dev.lazurite.quadz.common.util.network.NetworkResources;
import dev.lazurite.toolbox.api.network.ClientNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;

public interface InputHandler {
    Map<Integer, String> JOYSTICKS = Maps.newConcurrentMap();

    static boolean controllerExists() {
        return Config.controllerId != -1 && glfwJoystickPresent(Config.controllerId);
    }

    static float getAxisValue(JoystickAxis joystickAxis) {
        final var deadzone = Config.deadzone;
        final var axisValue = glfwGetJoystickAxes(Config.controllerId).get(joystickAxis.getAxis()) * (joystickAxis.isInverted() ? -1.0f : 1.0f);

        /* Account for deadzone and return */
        return deadzone != 0 && axisValue < deadzone * 0.5f && axisValue > -deadzone * 0.5f ? 0.0f : axisValue;
    }

    static String getJoystickName(int id) {
        if (glfwJoystickPresent(id)) {
            return glfwGetJoystickName(id);
        }

        return null;
    }

    static void sync() {
        final var player = Minecraft.getInstance().player;

        Bindable.get(player.getMainHandItem()).ifPresent(transmitter -> {
            final var level = Minecraft.getInstance().level;
            final var joystickValues = new HashMap<ResourceLocation, Float>();

            RemoteControllableSearch.byBindId(level, player.position(), transmitter.getBindId(), 100).ifPresent(entity -> {
                JoystickRegistry.getInstance().getJoystickAxes().forEach(joystickAxis -> {
                    joystickValues.put(joystickAxis.getResourceLocation(), getAxisValue(joystickAxis));
                });

                entity.setJoystickValues(joystickValues);
            });
        });

        ClientNetworking.send(NetworkResources.INPUT_FRAME, buf -> {
            buf.writeInt(JoystickRegistry.getInstance().getJoystickAxes().size());

            JoystickRegistry.getInstance().getJoystickAxes().forEach(joystickAxis -> {
                buf.writeResourceLocation(joystickAxis.getResourceLocation());
                buf.writeFloat(getAxisValue(joystickAxis));
            });
        });
    }
}
