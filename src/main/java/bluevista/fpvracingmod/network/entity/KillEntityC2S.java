package bluevista.fpvracingmod.network.entity;

import bluevista.fpvracingmod.server.ServerInitializer;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class KillEntityC2S {
    public static final Identifier PACKET_ID = new Identifier(ServerInitializer.MODID, "kill_entity_c2s");

    public static void accept(PacketContext context, PacketByteBuf buf) {
        PlayerEntity player = context.getPlayer();
        int entityID = buf.readInt();

        context.getTaskQueue().execute(() -> {
            Entity entity = null;

            if (player != null) {
                entity = player.world.getEntityById(entityID);

                if (entity != null) {
                    entity .kill();
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
        ServerSidePacketRegistry.INSTANCE.register(PACKET_ID, KillEntityC2S::accept);
    }
}
