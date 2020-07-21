package bluevista.fpvracingmod.client.input.keybinds;

import bluevista.fpvracingmod.network.RemoveGogglesPacketToServer;
import bluevista.fpvracingmod.server.items.GogglesItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public class RemoveGogglesKeybind {
    public static void callback(MinecraftClient mc) {
        if (mc.options.keySneak.wasPressed())
            if(mc.player.inventory.armor.get(3).getItem() instanceof GogglesItem)
                RemoveGogglesPacketToServer.send();
    }

    public static void register() {
        ClientTickCallback.EVENT.register(RemoveGogglesKeybind::callback);
    }
}
