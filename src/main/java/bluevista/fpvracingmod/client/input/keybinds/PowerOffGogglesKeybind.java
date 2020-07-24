package bluevista.fpvracingmod.client.input.keybinds;

import bluevista.fpvracingmod.network.PowerOffGogglesPacketToServer;
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
                PowerOffGogglesPacketToServer.send();
    }

    public static void register() {
        ClientTickCallback.EVENT.register(PowerOffGogglesKeybind::callback);
    }
}
