package bluevista.fpvracingmod.client.network;

import bluevista.fpvracingmod.server.ServerInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;

@Environment(EnvType.CLIENT)
public class RemoveGogglesPacketHandler {
    public static void accept(PacketContext context, PacketByteBuf buffer) {
        context.getTaskQueue().execute(() -> {
            // Execute on the main thread

            if(context.getPlayer() != null) {
                context.getPlayer().inventory.armor.get(3).decrement(1);
                if (!context.getPlayer().abilities.creativeMode) { // Fixes duplication of goggles when in creative
                    context.getPlayer().giveItemStack(new ItemStack(ServerInitializer.GOGGLES_ITEM));
                }
            }
        });
    }
}
