package bluevista.fpvracingmod.client.input;

import bluevista.fpvracingmod.client.network.EMPPacketHandler;
import bluevista.fpvracingmod.client.network.RemoveGogglesPacketHandler;
import bluevista.fpvracingmod.server.ServerInitializer;
import bluevista.fpvracingmod.server.items.GogglesItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class EMPKeybinding {
    private static KeyBinding key;

    public static void callback(MinecraftClient mc) {
        if (key.wasPressed()) EMPPacketHandler.send();
    }

    public static void register() {
        key = new KeyBinding(
                "key." + ServerInitializer.MODID + ".emp",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                "category." + ServerInitializer.MODID + ".keys"
        );

        KeyBindingHelper.registerKeyBinding(key);
        ClientTickCallback.EVENT.register(EMPKeybinding::callback);
    }
}
