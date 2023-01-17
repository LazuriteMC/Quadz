package dev.lazurite.quadz.common.util;

import com.google.common.collect.Maps;
import dev.lazurite.quadz.client.Config;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.nio.FloatBuffer;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;

public interface JoystickOutput {

    Map<Integer, String> JOYSTICKS = Maps.newConcurrentMap();

    static boolean controllerExists() {
        return Config.controllerId != -1 && glfwJoystickPresent(Config.controllerId);
    }

    static FloatBuffer getAllAxisValues() {
        if (glfwJoystickPresent(Config.controllerId)) {
            return glfwGetJoystickAxes(Config.controllerId).duplicate();
        }

        return null;
    }

    static float getAxisValue(@Nullable Player player, int axis, ResourceLocation resourceLocation, boolean inverted, boolean halved) {
        var deadzone = Config.deadzone;
        var value = 0.0f;

        if (glfwJoystickPresent(Config.controllerId)) {
            value = glfwGetJoystickAxes(Config.controllerId).duplicate().get(axis);

            value = inverted ? -value : value;

            value = halved ? value >= 0.0f ? value * 2.0f - 1.0f : -1.0f : value;

            value = deadzone != 0 && value < deadzone * 0.5f && value > -deadzone * 0.5f ? 0.0f : value;
        }

        if (player != null && player.level.isClientSide()) {
            /* Queue for transmission to the server */
            player.quadz$setJoystickValue(resourceLocation, value);
        } else if (player != null && !player.level.isClientSide()) {
            /* Server-side retrieval */
            return player.quadz$getJoystickValue(resourceLocation);
        }

        return value;
    }

    static String getJoystickName(int id) {
        if (glfwJoystickPresent(id)) {
            return glfwGetJoystickName(id);
        }

        return null;
    }

}
