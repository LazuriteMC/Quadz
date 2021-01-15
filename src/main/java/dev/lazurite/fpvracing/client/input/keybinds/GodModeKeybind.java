package dev.lazurite.fpvracing.client.input.keybinds;

import dev.lazurite.fpvracing.client.packet.GodModeC2S;
import dev.lazurite.fpvracing.FPVRacing;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class GodModeKeybind {
    private static KeyBinding key;

    public static void callback(MinecraftClient client) {
        if (key.wasPressed()) GodModeC2S.send();
    }

    public static void register() {
        key = new KeyBinding(
                "key." + FPVRacing.MODID + ".godmode",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_M,
                "category." + FPVRacing.MODID + ".keys"
        );

        KeyBindingHelper.registerKeyBinding(key);
        ClientTickCallback.EVENT.register(GodModeKeybind::callback);
    }
}
