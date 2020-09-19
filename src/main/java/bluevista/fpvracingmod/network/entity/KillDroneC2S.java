package bluevista.fpvracingmod.network.entity;

import bluevista.fpvracingmod.server.ServerInitializer;
import bluevista.fpvracingmod.server.entities.DroneEntity;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class KillDroneC2S {
    public static final Identifier PACKET_ID = new Identifier(ServerInitializer.MODID, "kill_drone_c2s");

    public static void accept(PacketContext context, PacketByteBuf buf) {
        PlayerEntity player = context.getPlayer();
        int droneID = buf.readInt();

        context.getTaskQueue().execute(() -> {
            DroneEntity drone = null;

            if (player != null)
                drone = (DroneEntity) player.world.getEntityById(droneID);

            if (drone != null) {
                drone.kill();
            }
        });
    }

    public static void send(DroneEntity drone) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeInt(drone.getEntityId());

        ClientSidePacketRegistry.INSTANCE.sendToServer(PACKET_ID, buf);
    }

    public static void register() {
        ServerSidePacketRegistry.INSTANCE.register(PACKET_ID, KillDroneC2S::accept);
    }
}
