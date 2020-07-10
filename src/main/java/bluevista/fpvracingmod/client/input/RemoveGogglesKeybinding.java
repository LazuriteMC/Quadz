package bluevista.fpvracingmod.client.input;

import bluevista.fpvracingmod.client.network.RemoveGogglesPacketHandler;
import bluevista.fpvracingmod.server.items.GogglesItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public class RemoveGogglesKeybinding {
    public static void callback(MinecraftClient mc) {
        if (mc.options.keySneak.wasPressed())
            if(mc.player.inventory.armor.get(3).getItem() instanceof GogglesItem)
                RemoveGogglesPacketHandler.send();
    }

    public static void register() {
        ClientTickCallback.EVENT.register(RemoveGogglesKeybinding::callback);
    }
}
