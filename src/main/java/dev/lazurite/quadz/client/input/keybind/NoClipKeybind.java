package dev.lazurite.quadz.client.input.keybind;

import com.mojang.blaze3d.platform.InputConstants;
import dev.lazurite.quadz.Quadz;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class NoClipKeybind {
    public static void register() {
        KeyMapping key = new KeyMapping(
                "key." + Quadz.MODID + ".noclip",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_N,
                "key." + Quadz.MODID + ".category"
        );

        KeyBindingHelper.registerKeyBinding(key);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (key.consumeClick()) {
                ClientPlayNetworking.send(Quadz.NOCLIP_C2S, PacketByteBufs.create());
            }
        });
    }
}
