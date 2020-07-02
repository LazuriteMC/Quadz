package bluevista.fpvracingmod.client.input;

import bluevista.fpvracingmod.server.ServerInitializer;
import bluevista.fpvracingmod.server.items.GogglesItem;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;

@Environment(EnvType.CLIENT)
public class RemoveGogglesKeybinding {
    public static void callback(MinecraftClient mc) {
        if (mc.options.keySneak.wasPressed()) {
            if(mc.player.inventory.armor.get(3).getItem() instanceof GogglesItem) {
                ClientSidePacketRegistry.INSTANCE.sendToServer(ServerInitializer.REMOVE_GOGGLES_PACKET_ID, new PacketByteBuf(Unpooled.buffer()));
            }
        }
    }
}
