package bluevista.fpvracing.network;

import bluevista.fpvracing.server.ServerInitializer;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class SelectedSlotS2C {
    public static final Identifier PACKET_ID = new Identifier(ServerInitializer.MODID, "selected_slot_s2c");

    public static void accept(PacketContext context, PacketByteBuf buf) {
        int slot = buf.readInt();
        context.getTaskQueue().execute(() -> context.getPlayer().inventory.selectedSlot = slot);
    }

    public static void send(ServerPlayerEntity player, int slot) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeInt(slot);
        ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, PACKET_ID, buf);
    }

    public static void register() {
        ClientSidePacketRegistry.INSTANCE.register(PACKET_ID, SelectedSlotS2C::accept);
    }
}
