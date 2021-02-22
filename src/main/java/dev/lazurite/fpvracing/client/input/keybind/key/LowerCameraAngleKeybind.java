package dev.lazurite.fpvracing.client.input.keybind.key;

import dev.lazurite.fpvracing.FPVRacing;
import dev.lazurite.fpvracing.client.input.keybind.net.ChangeCameraAngleC2S;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class LowerCameraAngleKeybind {
    private static KeyBinding key;

    public static void callback(MinecraftClient client) {
        if (key.wasPressed()) {
            ChangeCameraAngleC2S.send(-5);
        }
    }

    public static void register() {
        key = new KeyBinding(
                "key." + FPVRacing.MODID + ".lower_camera",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_DOWN,
                "key." + FPVRacing.MODID + ".category"
        );

        KeyBindingHelper.registerKeyBinding(key);
        ClientTickEvents.END_CLIENT_TICK.register(LowerCameraAngleKeybind::callback);
    }
}
