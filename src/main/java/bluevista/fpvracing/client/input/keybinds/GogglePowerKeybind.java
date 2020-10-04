package bluevista.fpvracing.client.input.keybinds;

import bluevista.fpvracing.client.ClientInitializer;
import bluevista.fpvracing.client.ClientTick;
import bluevista.fpvracing.network.keybinds.PowerGogglesC2S;
import bluevista.fpvracing.server.ServerInitializer;
import bluevista.fpvracing.server.entities.DroneEntity;
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
            if (key.wasPressed()) {
                if (ClientTick.isServerModded) {
                    if (GogglesItem.isWearingGoggles(client.player)) {
                        PowerGogglesC2S.send(!GogglesItem.isOn(client.player), keyNames);
                    }
                } else {
                    if (client.getCameraEntity() instanceof DroneEntity) {
                        ClientTick.destroyDrone(ClientInitializer.client);
                    } else {
                        ClientTick.createDrone(ClientInitializer.client);
                    }
                }
            }

            if (client.options.keySneak.wasPressed()) {
                if (ClientTick.isServerModded) {
                    if (GogglesItem.isWearingGoggles(client.player)) {
                        PowerGogglesC2S.send(false, keyNames);
                    }
                } else {
                    if (client.getCameraEntity() instanceof DroneEntity) {
                        ClientTick.destroyDrone(ClientInitializer.client);
                    }
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


