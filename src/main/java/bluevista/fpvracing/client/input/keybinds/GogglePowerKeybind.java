package bluevista.fpvracing.client.input.keybinds;

import bluevista.fpvracing.network.keybinds.PowerGogglesC2S;
import bluevista.fpvracing.server.ServerInitializer;
import bluevista.fpvracing.server.items.GogglesItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class GogglePowerKeybind {
    public static String[] keyNames;
    public static KeyBinding key;

    public static void callback(MinecraftClient client) {

        keyNames = new String[] {
            KeyBindingHelper.getBoundKeyOf(client.options.keySneak).getLocalizedText().getString().toUpperCase(),
            key.getBoundKeyLocalizedText().getString().toUpperCase()
        };

        if (client.player != null) {
            if (GogglesItem.isWearingGoggles(client.player)) {
                if (key.wasPressed()) {
                    PowerGogglesC2S.send(!GogglesItem.isOn(client.player), keyNames);
                }

                if (client.options.keySneak.wasPressed()) {
                    PowerGogglesC2S.send(false, keyNames);
                }
            }
        }
    }

    public static void register() {
        key = new KeyBinding(
                "key." + ServerInitializer.MODID + ".powergoggles",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                "category." + ServerInitializer.MODID + ".keys"
        );

        KeyBindingHelper.registerKeyBinding(key);
        ClientTickCallback.EVENT.register(GogglePowerKeybind::callback);
    }
}


