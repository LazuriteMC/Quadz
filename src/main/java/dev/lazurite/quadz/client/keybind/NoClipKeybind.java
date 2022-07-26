package dev.lazurite.quadz.client.keybind;

import com.mojang.blaze3d.platform.InputConstants;
import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.common.util.network.NetworkResources;
import dev.lazurite.toolbox.api.event.ClientEvents;
import dev.lazurite.toolbox.api.network.ClientNetworking;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class NoClipKeybind {
    public static void register() {
        final var key = new KeyMapping(
                "key." + Quadz.MODID + ".noclip",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_N,
                "key." + Quadz.MODID + ".category"
        );

        KeyBindingHelper.registerKeyBinding(key);

        ClientEvents.Tick.END_CLIENT_TICK.register(client -> {
            if (key.consumeClick()) {
                ClientNetworking.send(NetworkResources.NOCLIP_C2S, buf -> {});
            }
        });
    }
}
