package bluevista.fpvracing.client.input.keybinds;

import bluevista.fpvracing.network.keybinds.EMPC2S;
import bluevista.fpvracing.server.ServerInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/**
 * The EMP keybinding class sets up the keybinding (default key: o)
 * and also handles triggering of the keybinding. When the keybinding
 * is triggered, it sends a packet to the server using {@link EMPC2S}.
 */
@Environment(EnvType.CLIENT)
public class EMPKeybind {
    private static KeyBinding key;

    /**
     * The callback for the keybinding. It triggers
     * when the player presses the EMP keybinding.
     * @param client the client
     */
    public static void callback(MinecraftClient client) {
        if (key.wasPressed()) EMPC2S.send();
    }

    /**
     * Registers the keybinding in {@link bluevista.fpvracing.client.ClientInitializer}.
     * Also sets up the language identifier which is translated in the en_us.json file
     * within assets/.
     */
    public static void register() {
        key = new KeyBinding(
                "key." + ServerInitializer.MODID + ".emp",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                "category." + ServerInitializer.MODID + ".keys"
        );

        KeyBindingHelper.registerKeyBinding(key);
        ClientTickCallback.EVENT.register(EMPKeybind::callback);
    }
}
