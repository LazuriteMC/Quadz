package bluevista.fpvracingmod.network;

import bluevista.fpvracingmod.server.ServerInitializer;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

import java.util.List;

public class EMPPacketToServer {
    public static final Identifier EMP_PACKET_ID = new Identifier(ServerInitializer.MODID, "emp_packet");

    public static void accept(PacketContext context, PacketByteBuf buf) {
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
        for(PlayerEntity player : players)
            player.sendMessage(new TranslatableText(t), false);
    }

    public static void send() {
        ClientSidePacketRegistry.INSTANCE.sendToServer(EMP_PACKET_ID, new PacketByteBuf(Unpooled.buffer()));
    }

    public static void register() {
        ServerSidePacketRegistry.INSTANCE.register(EMP_PACKET_ID, EMPPacketToServer::accept);
    }
}
