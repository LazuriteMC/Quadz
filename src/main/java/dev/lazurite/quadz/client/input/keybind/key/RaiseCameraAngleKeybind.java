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
public class RaiseCameraAngleKeybind {
    private static KeyBinding key;

    public static void callback(MinecraftClient client) {
        if (key.wasPressed()) {
            ChangeCameraAngleC2S.send(5);
        }
    }

    public static void register() {
        key = new KeyBinding(
                "key." + Quadz.MODID + ".raise_camera",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UP,
                "key." + Quadz.MODID + ".category"
        );

        KeyBindingHelper.registerKeyBinding(key);
        ClientTickEvents.END_CLIENT_TICK.register(RaiseCameraAngleKeybind::callback);
    }
}
