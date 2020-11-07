package dev.lazurite.fpvracing.network.packet;

import dev.lazurite.fpvracing.server.ServerInitializer;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class AmDeadC2S {
    public static final Identifier PACKET_ID = new Identifier(ServerInitializer.MODID, "am_dead_c2s");

    public static void accept(PacketContext context, PacketByteBuf buf) {
        int entityId = buf.readInt();

        context.getTaskQueue().execute(() -> {
            Entity entity = context.getPlayer().getEntityWorld().getEntityById(entityId);

            if (entity != null) {
                if (!entity.removed) {
                    entity.kill();
                }
            }
        });
    }

    public static void send(Entity entity) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeInt(entity.getEntityId());
        ClientSidePacketRegistry.INSTANCE.sendToServer(PACKET_ID, buf);
    }

    public static void register() {
        ServerSidePacketRegistry.INSTANCE.register(PACKET_ID, AmDeadC2S::accept);
    }
}
