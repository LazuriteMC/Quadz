package bluevista.fpvracingmod.network;

import bluevista.fpvracingmod.server.ServerInitializer;
import bluevista.fpvracingmod.server.entities.DroneEntity;
import bluevista.fpvracingmod.server.items.TransmitterItem;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class NoClipPacketToServer {
    public static final Identifier PACKET_ID = new Identifier(ServerInitializer.MODID, "noclip_packet");

    public static void accept(PacketContext context, PacketByteBuf buf) {
        context.getTaskQueue().execute(() -> {
            ItemStack hand = context.getPlayer().getMainHandStack();
            if(hand.getItem() instanceof TransmitterItem) {
                if(hand.getSubTag("bind") != null) {
                    DroneEntity drone = DroneEntity.getByUuid(context.getPlayer(), hand.getSubTag("bind").getUuid("bind"));
                    drone.noClip = !drone.noClip;
                }
            }

        });
    }

    public static void send() {
        ClientSidePacketRegistry.INSTANCE.sendToServer(PACKET_ID, new PacketByteBuf(Unpooled.buffer()));
    }

    public static void register() {
        ServerSidePacketRegistry.INSTANCE.register(PACKET_ID, NoClipPacketToServer::accept);
    }
}
