package dev.lazurite.quadz.client.input.keybind;

import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.client.Config;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.TranslatableText;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class FollowKeybind {
    public static void register() {
        KeyBinding key = new KeyBinding(
                "key." + Quadz.MODID + ".follow",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                "key." + Quadz.MODID + ".category"
        );

        KeyBindingHelper.registerKeyBinding(key);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (key.wasPressed()) {
                Config.getInstance().followLOS = !Config.getInstance().followLOS;

                if (Config.getInstance().followLOS) {
                    client.player.sendMessage(new TranslatableText("message.quadz.follow_on"), true);
                } else {
                    client.player.sendMessage(new TranslatableText("message.quadz.follow_off"), true);
                }
            }
        });
    }
}
