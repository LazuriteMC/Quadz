package dev.lazurite.quadz.client.input.keybind;

import com.mojang.blaze3d.platform.InputConstants;
import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.client.Config;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.TranslatableComponent;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class FollowKeybind {
    public static void register() {
        KeyMapping key = new KeyMapping(
                "key." + Quadz.MODID + ".follow",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                "key." + Quadz.MODID + ".category"
        );

        KeyBindingHelper.registerKeyBinding(key);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (key.consumeClick()) {
                Config.getInstance().followLOS = !Config.getInstance().followLOS;

                if (Config.getInstance().followLOS) {
                    client.player.displayClientMessage(new TranslatableComponent("message.quadz.follow_on"), true);
                } else {
                    client.player.displayClientMessage(new TranslatableComponent("message.quadz.follow_off"), true);
                }
            }
        });
    }
}
