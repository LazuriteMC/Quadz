package dev.lazurite.fpvracing.client.input.keybinds;

import dev.lazurite.fpvracing.FPVRacing;
import dev.lazurite.fpvracing.common.packet.ElectromagneticPulseS2C;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/**
 * The EMP keybinding class sets up the keybinding (default key: o)
 * and also handles triggering of the keybinding. When the keybinding
 * is triggered, it sends a packet to the server using {@link ElectromagneticPulseS2C}.
 */
@Environment(EnvType.CLIENT)
public class EMPKeybind {
    private static KeyBinding key;

    public static void callback(MinecraftClient client) {
        if (key.wasPressed()) ElectromagneticPulseS2C.send(client.player.getPos());
    }

    public static void register() {
        key = new KeyBinding(
                "key." + FPVRacing.MODID + ".emp",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                "category." + FPVRacing.MODID + ".keys"
        );

        KeyBindingHelper.registerKeyBinding(key);
        ClientTickEvents.END_CLIENT_TICK.register(EMPKeybind::callback);
    }
}
