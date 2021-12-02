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
import net.minecraft.network.FriendlyByteBuf;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class CameraAngleKeybinds {
    public static void register() {
        KeyMapping up = new KeyMapping(
                "key." + Quadz.MODID + ".camera.raise",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_PERIOD,
                "key." + Quadz.MODID + ".category"
        );

        KeyMapping down = new KeyMapping(
                "key." + Quadz.MODID + ".camera.lower",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_COMMA,
                "key." + Quadz.MODID + ".category"
        );

        KeyBindingHelper.registerKeyBinding(up);
        KeyBindingHelper.registerKeyBinding(down);

        Consumer<Integer> sendPacket = amount -> {
            FriendlyByteBuf buf = PacketByteBufs.create();
            buf.writeInt(amount);
            ClientPlayNetworking.send(Quadz.CHANGE_CAMERA_ANGLE_C2S, buf);
        };

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (up.consumeClick()) {
                sendPacket.accept(5);
            } else if (down.consumeClick()) {
                sendPacket.accept(-5);
            }
        });
    }
}
