package bluevista.fpvracingmod.network;

import bluevista.fpvracingmod.server.ServerInitializer;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class RemoveGogglesPacketHandler {
    public static final Identifier REMOVE_GOGGLES_PACKET_ID = new Identifier(ServerInitializer.MODID, "goggles_packet");

    public static void accept(PacketContext context, PacketByteBuf buffer) {
        context.getTaskQueue().execute(() -> {
            if(context.getPlayer() != null) {
                context.getPlayer().inventory.armor.get(3).decrement(1);
                if (!context.getPlayer().abilities.creativeMode) { // Fixes duplication of goggles when in creative
                    context.getPlayer().giveItemStack(new ItemStack(ServerInitializer.GOGGLES_ITEM));
                }
            }
        });
    }

    public static void send() {
        ClientSidePacketRegistry.INSTANCE.sendToServer(REMOVE_GOGGLES_PACKET_ID, new PacketByteBuf(Unpooled.buffer()));
    }

    public static void register() {
        ServerSidePacketRegistry.INSTANCE.register(REMOVE_GOGGLES_PACKET_ID, RemoveGogglesPacketHandler::accept);
    }
}
