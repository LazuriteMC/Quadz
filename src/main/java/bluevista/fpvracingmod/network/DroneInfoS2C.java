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

import java.util.UUID;
import java.util.stream.Stream;

public class DroneInfoS2C {
    public static final Identifier PACKET_ID = new Identifier(ServerInitializer.MODID, "drone_info_s2c");

    public static void accept(PacketContext context, PacketByteBuf buf) {
        PlayerEntity player = context.getPlayer();

        UUID droneID = buf.readUuid();
        int band = buf.readInt();
        int channel = buf.readInt();
        int cameraAngle = buf.readInt();
        boolean infiniteTracking = buf.readBoolean();

        context.getTaskQueue().execute(() -> {
            DroneEntity drone = null;

            if(player != null)
                drone = DroneEntity.getByUuid(player, droneID);

            if(drone != null) {
                drone.setBand(band);
                drone.setChannel(channel);
                drone.setCameraAngle(cameraAngle);
                drone.setInfiniteTracking(infiniteTracking);
            }
        });
    }

    public static void send(DroneEntity drone) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());

        buf.writeUuid(drone.getUuid());
        buf.writeInt(drone.getBand());
        buf.writeInt(drone.getChannel());
        buf.writeInt(drone.getCameraAngle());
        buf.writeBoolean(drone.hasInfiniteTracking());

        Stream<PlayerEntity> watchingPlayers = PlayerStream.watching(drone.getEntityWorld(), new BlockPos(drone.getPos()));
        watchingPlayers.forEach(player -> {
            ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, PACKET_ID, buf);
        });
    }

    public static void register() {
        ClientSidePacketRegistry.INSTANCE.register(PACKET_ID, DroneInfoS2C::accept);
    }
}
