package bluevista.fpvracingmod.network;

import bluevista.fpvracingmod.client.math.QuaternionHelper;
import bluevista.fpvracingmod.server.ServerInitializer;
import bluevista.fpvracingmod.server.entities.DroneEntity;
import bluevista.fpvracingmod.server.items.TransmitterItem;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.fabric.api.server.PlayerStream;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Quaternion;

import java.util.UUID;
import java.util.stream.Stream;

public class DroneInfoToClient {
    public static final Identifier PACKET_ID = new Identifier(ServerInitializer.MODID, "drone_info_client_packet");

    public static void accept(PacketContext context, PacketByteBuf buf) {
        Quaternion q = QuaternionHelper.deserialize(buf);
        UUID droneID = buf.readUuid();
        int band = buf.readInt();
        int channel = buf.readInt();
        int cameraAngle = buf.readInt();

        context.getTaskQueue().execute(() -> {
            DroneEntity drone = null;

            if(context.getPlayer() != null)
                drone = DroneEntity.getByUuid(context.getPlayer(), droneID);

            if(drone != null) {
                drone.setOrientation(q);
                drone.setBand(band);
                drone.setChannel(channel);
                drone.setCameraAngle(cameraAngle);
            }
        });
    }

    public static void send(DroneEntity drone) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());

        QuaternionHelper.serialize(drone.getOrientation(), buf);
        buf.writeUuid(drone.getUuid());
        buf.writeInt(drone.getBand());
        buf.writeInt(drone.getChannel());
        buf.writeInt(drone.getCameraAngle());

        Stream<PlayerEntity> watchingPlayers = PlayerStream.watching(drone.getEntityWorld(), new BlockPos(drone.getPos()));
        watchingPlayers.forEach(player -> {
            ItemStack handStack = player.getMainHandStack();
            if(!(handStack.getItem() instanceof TransmitterItem) && !drone.isTransmitterBound(handStack))
                ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, PACKET_ID , buf);
        });
    }

    public static void register() {
        ClientSidePacketRegistry.INSTANCE.register(PACKET_ID, DroneInfoToClient::accept);
    }
}
