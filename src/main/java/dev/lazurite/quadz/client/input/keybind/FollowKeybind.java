package dev.lazurite.quadz.client.input.keybind;

import com.mojang.blaze3d.platform.InputConstants;
import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.client.Config;
import dev.lazurite.toolbox.api.event.ClientEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class FollowKeybind {
    public static void register() {
        final var key = new KeyMapping(
                "key." + Quadz.MODID + ".follow",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                "key." + Quadz.MODID + ".category"
        );

        KeyBindingHelper.registerKeyBinding(key);

        ClientEvents.Tick.END_CLIENT_TICK.register(client -> {
            if (key.consumeClick()) {
                Config.getInstance().followLOS = !Config.getInstance().followLOS;

                if (Config.getInstance().followLOS) {
                    client.player.displayClientMessage(Component.translatable("message.quadz.follow_on"), true);
                } else {
                    client.player.displayClientMessage(Component.translatable("message.quadz.follow_off"), true);
                }
            }
        });
    }
}
