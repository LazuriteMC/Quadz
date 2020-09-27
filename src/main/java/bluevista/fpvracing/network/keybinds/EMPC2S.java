package bluevista.fpvracing.network.keybinds;

import bluevista.fpvracing.server.ServerInitializer;
import bluevista.fpvracing.server.ServerTick;
import bluevista.fpvracing.server.entities.DroneEntity;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

import java.util.List;

public class EMPC2S {
    public static final Identifier PACKET_ID = new Identifier(ServerInitializer.MODID, "emp_c2s");

    public static void accept(PacketContext context, PacketByteBuf buf) {
        PlayerEntity p = context.getPlayer();
        int r = buf.readInt();

        context.getTaskQueue().execute(() -> {
            int kill = 0;

            ServerPlayerEntity sp = (ServerPlayerEntity) p;
            if (ServerTick.isInGoggles(sp)) {
                sp.getCameraEntity().kill();
                sp.getServerWorld().removeEntity(sp.getCameraEntity());
                ServerTick.resetView(sp);
                kill++;
            }

            List<Entity> entities = p.getEntityWorld().getOtherEntities(p, p.getBoundingBox().expand(r));
            for (Entity e : entities) {
                if (e instanceof DroneEntity) {
                    DroneEntity drone = (DroneEntity) e;
                    drone.kill();
                    kill++;
                }
            }

            String t;
            if (kill == 1) t = "Destroyed 1 drone";
            else t = "Destroyed " + kill + " drones";

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
