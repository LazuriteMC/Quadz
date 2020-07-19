package bluevista.fpvracingmod.server.network;

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

import java.util.stream.Stream;

public class QuaternionPacketHandler {
    public static final Identifier QUATERNION_PACKET_ID = new Identifier(ServerInitializer.MODID, "quaternion_packet");

    public static void accept(PacketContext context, PacketByteBuf buf) {
        Quaternion q = new Quaternion(
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat()
        );
        DroneEntity drone = DroneEntity.getByUuid(context.getPlayer(), buf.readUuid());

        context.getTaskQueue().execute(() -> {
            if(drone != null && (drone.age < 20 || !context.getPlayer().isMainPlayer()))
                drone.setOrientation(q);
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
                ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, QUATERNION_PACKET_ID, buf));
    }

    public static void register() {
        ClientSidePacketRegistry.INSTANCE.register(QUATERNION_PACKET_ID, QuaternionPacketHandler::accept);
    }
}
