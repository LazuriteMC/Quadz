package bluevista.fpvracingmod.network;

import bluevista.fpvracingmod.client.math.QuaternionHelper;
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

public class DroneInfoC2S {
    public static final Identifier PACKET_ID = new Identifier(ServerInitializer.MODID, "drone_info_c2s");

    public static void accept(PacketContext context, PacketByteBuf buf) {
        UUID droneID = buf.readUuid();
        float throttle = buf.readFloat();
        boolean infiniteTracking = buf.readBoolean();
        Quaternion q = QuaternionHelper.deserialize(buf);

        context.getTaskQueue().execute(() -> {
            DroneEntity drone = null;

            if(context.getPlayer() != null)
                drone = DroneEntity.getByUuid(context.getPlayer(), droneID);

            if(drone != null) {
                drone.setOrientation(q);
                drone.setThrottle(throttle);
                drone.setInfiniteTracking(infiniteTracking);
            }
        });
    }

    public static void send(DroneEntity drone) {
        Quaternion q = drone.getOrientation();
        float throttle = drone.getThrottle();
        boolean infiniteTracking = drone.hasInfiniteTracking();

        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeUuid(drone.getUuid());
        buf.writeFloat(throttle);
        buf.writeBoolean(infiniteTracking);
        QuaternionHelper.serialize(q, buf);

        ClientSidePacketRegistry.INSTANCE.sendToServer(PACKET_ID, buf);
    }

    public static void register() {
        ServerSidePacketRegistry.INSTANCE.register(PACKET_ID, DroneInfoC2S::accept);
    }
}
