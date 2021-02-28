package dev.lazurite.quadz.client.input.keybind.key;

import dev.lazurite.quadz.client.input.keybind.net.GodModeC2S;
import dev.lazurite.quadz.Quadz;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class GodModeKeybind {
    private static KeyBinding key;

    public static void callback(MinecraftClient client) {
        if (key.wasPressed()) {
            GodModeC2S.send();
        }
    }

    public static void register() {
        key = new KeyBinding(
                "key." + Quadz.MODID + ".godmode",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_M,
                "key." + Quadz.MODID + ".category"
        );

        KeyBindingHelper.registerKeyBinding(key);
        ClientTickEvents.END_CLIENT_TICK.register(GodModeKeybind::callback);
    }
}
