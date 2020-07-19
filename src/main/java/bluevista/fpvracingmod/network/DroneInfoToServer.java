package bluevista.fpvracingmod.network;

import bluevista.fpvracingmod.server.ServerInitializer;
import bluevista.fpvracingmod.server.entities.DroneEntity;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Quaternion;

import java.util.UUID;

public class DroneInfoToServer {
    public static final Identifier PACKET_ID = new Identifier(ServerInitializer.MODID, "drone_info_server_packet");

    public static void accept(PacketContext context, PacketByteBuf buf) {
        Quaternion orientation = new Quaternion(
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat()
        );

        float throttle = buf.readFloat();
        UUID droneID = buf.readUuid();

        context.getTaskQueue().execute(() -> {
            DroneEntity drone = null;

            if(context.getPlayer() != null)
                drone = DroneEntity.getByUuid(context.getPlayer(), droneID);

            if(drone != null) {
                drone.setOrientation(orientation);
                drone.setThrottle(throttle);
            }
        });
    }

    public static void send(DroneEntity drone) {
        Quaternion q = drone.getOrientation();
        float throttle = drone.getThrottle();

        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeFloat(q.getX());
        buf.writeFloat(q.getY());
        buf.writeFloat(q.getZ());
        buf.writeFloat(q.getW());
        buf.writeFloat(throttle);
        buf.writeUuid(drone.getUuid());

        ClientSidePacketRegistry.INSTANCE.sendToServer(PACKET_ID, buf);
    }

    public static void register() {
        ServerSidePacketRegistry.INSTANCE.register(PACKET_ID, DroneInfoToServer::accept);
    }
}
