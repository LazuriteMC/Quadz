package bluevista.fpvracing.network;

import bluevista.fpvracing.client.ClientTick;
import bluevista.fpvracing.server.ServerInitializer;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class ModdedServerS2C {
    public static final Identifier PACKET_ID = new Identifier(ServerInitializer.MODID, "modded_server_s2c");

    public static void accept(PacketContext context, PacketByteBuf buf) {
        context.getTaskQueue().execute(() -> ClientTick.isServerModded = true);
    }

    public static void send(ServerPlayerEntity player) {
        ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, PACKET_ID, new PacketByteBuf(Unpooled.buffer()));
    }

    public static void register() {
        ClientSidePacketRegistry.INSTANCE.register(PACKET_ID, ModdedServerS2C::accept);
    }
}
