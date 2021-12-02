package dev.lazurite.quadz.client.input.keybind;

import com.mojang.blaze3d.platform.InputConstants;
import dev.lazurite.quadz.Quadz;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class ControlKeybinds {
    public static KeyMapping pitchForward = new KeyMapping(
            "key." + Quadz.MODID + ".pitch.forward",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UP,
            "key." + Quadz.MODID + ".category"
    );

    public static KeyMapping pitchBackward = new KeyMapping(
            "key." + Quadz.MODID + ".pitch.backward",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_DOWN,
            "key." + Quadz.MODID + ".category"
    );

    public static KeyMapping rollLeft = new KeyMapping(
            "key." + Quadz.MODID + ".roll.left",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT,
            "key." + Quadz.MODID + ".category"
    );

    public static KeyMapping rollRight = new KeyMapping(
            "key." + Quadz.MODID + ".roll.right",
            InputConstants.Type.KEYSYM,
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
