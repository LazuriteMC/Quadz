package bluevista.fpvracing.client.input.keybinds;

import bluevista.fpvracing.network.keybinds.GodModeC2S;
import bluevista.fpvracing.server.ServerInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class GodModeKeybind {
    private static KeyBinding key;

    public static void callback(MinecraftClient client) {
        if (key.wasPressed()) GodModeC2S.send();
    }

    public static void register() {
        key = new KeyBinding(
                "key." + ServerInitializer.MODID + ".godmode",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_M,
                "category." + ServerInitializer.MODID + ".keys"
        );

        KeyBindingHelper.registerKeyBinding(key);
        ClientTickCallback.EVENT.register(GodModeKeybind::callback);
    }
}
