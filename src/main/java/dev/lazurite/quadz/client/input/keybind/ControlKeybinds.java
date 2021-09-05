package dev.lazurite.quadz.client.input.keybind;

import dev.lazurite.quadz.Quadz;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class ControlKeybinds {
    public static KeyBinding pitchForward = new KeyBinding(
            "key." + Quadz.MODID + ".pitch.forward",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_UP,
            "key." + Quadz.MODID + ".category"
    );

    public static KeyBinding pitchBackward = new KeyBinding(
            "key." + Quadz.MODID + ".pitch.backward",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_DOWN,
            "key." + Quadz.MODID + ".category"
    );

    public static KeyBinding rollLeft = new KeyBinding(
            "key." + Quadz.MODID + ".roll.left",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT,
            "key." + Quadz.MODID + ".category"
    );

    public static KeyBinding rollRight = new KeyBinding(
            "key." + Quadz.MODID + ".roll.right",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT,
            "key." + Quadz.MODID + ".category"
    );

    public static void register() {
        KeyBindingHelper.registerKeyBinding(pitchForward);
        KeyBindingHelper.registerKeyBinding(pitchBackward);
        KeyBindingHelper.registerKeyBinding(rollLeft);
        KeyBindingHelper.registerKeyBinding(rollRight);
    }
}
