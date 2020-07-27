package bluevista.fpvracingmod.client.input.keybinds;

import bluevista.fpvracingmod.network.PowerGogglesC2S;
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
public class PowerOnGogglesKeybind {
    private static KeyBinding key;

    public static void callback(MinecraftClient client) {
        if(client.player != null)
            if (key.wasPressed())
                if(GogglesItem.isWearingGoggles(client.player))
//                    GogglesItem.setOn(client.player.inventory.armor.get(3), true, client.player);
                    PowerGogglesC2S.send(true);
    }

    public static void register() {
        key = new KeyBinding(
                "key." + ServerInitializer.MODID + ".powerongoggles",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                "category." + ServerInitializer.MODID + ".keys"
        );

        KeyBindingHelper.registerKeyBinding(key);
        ClientTickCallback.EVENT.register(PowerOnGogglesKeybind::callback);
    }
}


