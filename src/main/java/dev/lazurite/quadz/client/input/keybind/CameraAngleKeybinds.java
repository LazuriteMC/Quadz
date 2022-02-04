package dev.lazurite.quadz.client.input.keybind;

import com.mojang.blaze3d.platform.InputConstants;
import dev.lazurite.quadz.Quadz;
import dev.lazurite.toolbox.api.event.ClientEvents;
import dev.lazurite.toolbox.api.network.ClientNetworking;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class CameraAngleKeybinds {
    public static void register() {
        final var up = new KeyMapping(
                "key." + Quadz.MODID + ".camera.raise",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_PERIOD,
                "key." + Quadz.MODID + ".category"
        );

        final var down = new KeyMapping(
                "key." + Quadz.MODID + ".camera.lower",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_COMMA,
                "key." + Quadz.MODID + ".category"
        );

        KeyBindingHelper.registerKeyBinding(up);
        KeyBindingHelper.registerKeyBinding(down);

        ClientEvents.Tick.END_CLIENT_TICK.register(client -> {
            if (up.consumeClick()) {
                ClientNetworking.send(Quadz.CHANGE_CAMERA_ANGLE_C2S, buf -> buf.writeInt(5));
            } else if (down.consumeClick()) {
                ClientNetworking.send(Quadz.CHANGE_CAMERA_ANGLE_C2S, buf -> buf.writeInt(-5));
            }
        });
    }
}
