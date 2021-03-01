package dev.lazurite.quadz.client.input.keybind.key;

import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.client.input.keybind.net.ChangeCameraAngleC2S;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class CameraAngleKeybinds {
    private static KeyBinding up;
    private static KeyBinding down;

    public static void callback(MinecraftClient client) {
        if (up.wasPressed()) {
            ChangeCameraAngleC2S.send(5);
        } else if (down.wasPressed()) {
            ChangeCameraAngleC2S.send(-5);
        }
    }

    public static void register() {
        up = new KeyBinding(
                "key." + Quadz.MODID + ".camera.raise",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_PERIOD,
                "key." + Quadz.MODID + ".category"
        );

        down = new KeyBinding(
                "key." + Quadz.MODID + ".camera.lower",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_COMMA,
                "key." + Quadz.MODID + ".category"
        );

        KeyBindingHelper.registerKeyBinding(up);
        KeyBindingHelper.registerKeyBinding(down);
        ClientTickEvents.END_CLIENT_TICK.register(CameraAngleKeybinds::callback);
    }
}
