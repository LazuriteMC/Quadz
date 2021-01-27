package dev.lazurite.fpvracing.client.input.keybind;

import dev.lazurite.fpvracing.FPVRacing;
import dev.lazurite.fpvracing.client.packet.keybind.ElectromagneticPulseC2S;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class EMPKeybind {
    private static KeyBinding key;

    public static void callback(MinecraftClient client) {
        if (key.wasPressed()) ElectromagneticPulseC2S.send(80);
    }

    public static void register() {
        key = new KeyBinding(
                "key." + FPVRacing.MODID + ".emp",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                "key." + FPVRacing.MODID + ".category"
        );

        KeyBindingHelper.registerKeyBinding(key);
        ClientTickEvents.END_CLIENT_TICK.register(EMPKeybind::callback);
    }
}
