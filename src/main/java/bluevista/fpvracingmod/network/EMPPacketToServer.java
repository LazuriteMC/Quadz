package bluevista.fpvracingmod.network;

import bluevista.fpvracingmod.server.ServerInitializer;
import bluevista.fpvracingmod.server.entities.DroneEntity;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class EMPPacketToServer {
    public static final Identifier PACKET_ID = new Identifier(ServerInitializer.MODID, "emp_packet");

    public static void accept(PacketContext context, PacketByteBuf buf) {
        int[] ids = buf.readIntArray();

        PlayerEntity p = context.getPlayer();
        List<Entity> entities = p.getEntityWorld().getEntities(p, p.getBoundingBox().expand(100, 100, 100));
        int kill = 0;

        for (Entity e : entities) {
            if (e.getType() == ServerInitializer.DRONE_ENTITY) {
                e.kill();
                kill++;
            }
        }

        String t;
        if(kill == 1) t = "Rekt 1 drone";
        else t = "Rekt " + kill + " drones";
        List<? extends PlayerEntity> players = context.getPlayer().getEntityWorld().getPlayers();
        for(PlayerEntity player : players) {
            ServerSidePacketRegistry.INSTANCE.sendToPlayer(p, new EntitiesDestroyS2CPacket(ids));
            player.sendMessage(new TranslatableText(t), false);
        }
    }

    public static void send(PlayerEntity player, int r) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());

        List<Entity> entities = player.getEntityWorld().getEntities(player, player.getBoundingBox().expand(r, r, r));
        List<Entity> remove = new ArrayList<>();

        for (Entity entity : entities)
            if(entity instanceof DroneEntity)
                remove.add(entity);

        int[] ids = new int[remove.size()];
        for(int i = 0; i < ids.length; i++)
            ids[i] = remove.get(i).getEntityId();

        buf.writeIntArray(ids);
        ClientSidePacketRegistry.INSTANCE.sendToServer(PACKET_ID, buf);
    }

    public static void register() {
        ServerSidePacketRegistry.INSTANCE.register(PACKET_ID, EMPPacketToServer::accept);
    }
}
