package bluevista.fpvracingmod.client.input.keybinds;

import bluevista.fpvracingmod.network.PowerGogglesC2S;
import bluevista.fpvracingmod.server.items.GogglesItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public class PowerOffGogglesKeybind {
    public static void callback(MinecraftClient client) {
        if(client.player != null) {
            if (client.options.keySneak.wasPressed()) {
                if (GogglesItem.isWearingGoggles(client.player)) {
//                    PowerGogglesC2S.send(false);
                    GogglesItem.setOn(client.player.inventory.armor.get(3), false, client.player);
                }
            }
        }
    }

    public static void register() {
        ClientTickCallback.EVENT.register(PowerOffGogglesKeybind::callback);
    }
}
