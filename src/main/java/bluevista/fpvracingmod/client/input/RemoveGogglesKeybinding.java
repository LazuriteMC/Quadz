package bluevista.fpvracingmod.client.input;

import bluevista.fpvracingmod.server.items.GogglesItem;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;

@Environment(EnvType.CLIENT)
public class RemoveGogglesKeybinding {
    public static void callback(MinecraftClient mc) {
        while (mc.options.keySneak.wasPressed()) {
            if(mc.player.inventory.armor.get(3).getItem() instanceof GogglesItem) {
                PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());


//                mc.player.inventory.armor.get(3).decrement(1);
//                mc.player.giveItemStack(new ItemStack(ServerInitializer.GOGGLES_ITEM));
            }
        }
    }
}
