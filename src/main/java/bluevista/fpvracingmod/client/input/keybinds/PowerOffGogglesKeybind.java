package bluevista.fpvracingmod.client.input.keybinds;

import bluevista.fpvracingmod.network.PowerOffGogglesC2S;
import bluevista.fpvracingmod.server.items.GogglesItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public class PowerOffGogglesKeybind {
    public static void callback(MinecraftClient client) {
        if (client.options.keySneak.wasPressed())
            if(client.player.inventory.armor.get(3).getItem() instanceof GogglesItem)
                PowerOffGogglesC2S.send();
    }

    public static void register() {
        ClientTickCallback.EVENT.register(PowerOffGogglesKeybind::callback);
    }
}
