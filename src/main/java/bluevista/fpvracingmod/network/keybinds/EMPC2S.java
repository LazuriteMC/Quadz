package bluevista.fpvracingmod.network.keybinds;

import bluevista.fpvracingmod.server.ServerInitializer;
import bluevista.fpvracingmod.server.entities.DroneEntity;
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

public class EMPC2S {
    public static final Identifier PACKET_ID = new Identifier(ServerInitializer.MODID, "emp_c2s");

    public static void accept(PacketContext context, PacketByteBuf buf) {
        PlayerEntity p = context.getPlayer();
        int r = buf.readInt();

        context.getTaskQueue().execute(() -> {
            List<Entity> entities = p.getEntityWorld().getEntities(p, p.getBoundingBox().expand(r));
            int kill = 0;

            for (Entity e : entities) {
                if (e instanceof DroneEntity) {
                    DroneEntity drone = (DroneEntity) e;
                    drone.kill();
                    kill++;
                }
            }

            String t;
            if (kill == 1) t = "Rekt 1 drone";
            else t = "Rekt " + kill + " drones";

            List<? extends PlayerEntity> players = context.getPlayer().getEntityWorld().getPlayers();
            for (PlayerEntity player : players)
                player.sendMessage(new TranslatableText(t), false);
        });
    }

    public static void send(int r) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeInt(r);
        ClientSidePacketRegistry.INSTANCE.sendToServer(PACKET_ID, buf);
    }

    public static void register() {
        ServerSidePacketRegistry.INSTANCE.register(PACKET_ID, EMPC2S::accept);
    }
}
