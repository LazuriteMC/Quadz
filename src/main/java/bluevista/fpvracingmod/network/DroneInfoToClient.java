package bluevista.fpvracingmod.network;

import bluevista.fpvracingmod.server.ServerInitializer;
import bluevista.fpvracingmod.server.entities.DroneEntity;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.fabric.api.server.PlayerStream;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Quaternion;

import java.util.UUID;
import java.util.stream.Stream;

public class DroneInfoToClient {
    public static final Identifier PACKET_ID = new Identifier(ServerInitializer.MODID, "drone_info_client_packet");

    public static void accept(PacketContext context, PacketByteBuf buf) {
        Quaternion orientation = new Quaternion(
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat()
        );
        UUID droneID = buf.readUuid();

        context.getTaskQueue().execute(() -> {
            DroneEntity drone = null;

            if(context.getPlayer() != null)
                drone = DroneEntity.getByUuid(context.getPlayer(), droneID);

            if(drone != null) {
                drone.setOrientation(orientation);
            }
        });
    }

    public static void send(DroneEntity drone) {
        Quaternion q = drone.getOrientation();

        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeFloat(q.getX());
        buf.writeFloat(q.getY());
        buf.writeFloat(q.getZ());
        buf.writeFloat(q.getW());
        buf.writeUuid(drone.getUuid());

        Stream<PlayerEntity> watchingPlayers = PlayerStream.watching(drone.getEntityWorld(), new BlockPos(drone.getPos()));
        watchingPlayers.forEach(player ->
                ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, PACKET_ID , buf));
    }

    public static void register() {
        ClientSidePacketRegistry.INSTANCE.register(PACKET_ID, DroneInfoToClient::accept);
    }
}
