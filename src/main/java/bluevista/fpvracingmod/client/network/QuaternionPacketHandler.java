package bluevista.fpvracingmod.client.network;

import bluevista.fpvracingmod.server.ServerInitializer;
import bluevista.fpvracingmod.server.entities.DroneEntity;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Quaternion;

import java.util.UUID;

@Environment(EnvType.CLIENT)
public class QuaternionPacketHandler {
    public static final Identifier QUATERNION_PACKET_ID = new Identifier(ServerInitializer.MODID, "quaternion_packet");

    public static void accept(PacketContext context, PacketByteBuf buffer) {
        Quaternion orientation = new Quaternion(
                buffer.readFloat(),
                buffer.readFloat(),
                buffer.readFloat(),
                buffer.readFloat()
        );
        UUID droneID = buffer.readUuid();

        context.getTaskQueue().execute(() -> {
            DroneEntity drone = null;
            if(context.getPlayer() != null) drone = DroneEntity.getByUuid(context.getPlayer(), droneID);
            if(drone != null) drone.setOrientation(orientation);
        });
    }

    public static void send(Quaternion q, DroneEntity drone) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeFloat(q.getX());
        buf.writeFloat(q.getY());
        buf.writeFloat(q.getZ());
        buf.writeFloat(q.getW());
        buf.writeUuid(drone.getUuid());
        ClientSidePacketRegistry.INSTANCE.sendToServer(QUATERNION_PACKET_ID, buf);
    }
    public static void register() {
        ServerSidePacketRegistry.INSTANCE.register(QUATERNION_PACKET_ID, QuaternionPacketHandler::accept);
    }
}
