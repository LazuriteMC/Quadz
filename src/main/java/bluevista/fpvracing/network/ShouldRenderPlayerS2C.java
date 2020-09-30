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

public class ShouldRenderPlayerS2C {
    public static final Identifier PACKET_ID = new Identifier(ServerInitializer.MODID, "should_render_player_s2c");

    public static void accept(PacketContext context, PacketByteBuf buf) {
        boolean shouldRenderPlayer = buf.readBoolean();
        context.getTaskQueue().execute(() -> ClientTick.shouldRenderPlayer = shouldRenderPlayer);
    }

    public static void send(ServerPlayerEntity player, boolean shouldRenderPlayer) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeBoolean(shouldRenderPlayer);
        ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, PACKET_ID, buf);
    }

    public static void register() {
        ClientSidePacketRegistry.INSTANCE.register(PACKET_ID, ShouldRenderPlayerS2C::accept);
    }
}
