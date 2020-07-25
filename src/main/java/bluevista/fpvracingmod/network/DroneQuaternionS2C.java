package bluevista.fpvracingmod.network;

import bluevista.fpvracingmod.client.math.QuaternionHelper;
import bluevista.fpvracingmod.server.ServerInitializer;
import bluevista.fpvracingmod.server.entities.DroneEntity;
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

public class DroneQuaternionS2C {
    public static final Identifier PACKET_ID = new Identifier(ServerInitializer.MODID, "drone_quaternion_s2c");

    public static void accept(PacketContext context, PacketByteBuf buf) {
        PlayerEntity player = context.getPlayer();

        Quaternion q = QuaternionHelper.deserialize(buf);
        UUID droneID = buf.readUuid();

        context.getTaskQueue().execute(() -> {
            DroneEntity drone = null;

            if(player != null)
                drone = DroneEntity.getByUuid(player, droneID);

            if(drone != null)
                drone.setOrientation(q);
        });
    }

    public static void send(DroneEntity drone) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());

        QuaternionHelper.serialize(drone.getOrientation(), buf);
        buf.writeUuid(drone.getUuid());

        Stream<PlayerEntity> watchingPlayers = PlayerStream.watching(drone.getEntityWorld(), new BlockPos(drone.getPos()));
        watchingPlayers.forEach(player -> {
            ItemStack handStack = player.getMainHandStack();
            if (!drone.isTransmitterBound(handStack))
                ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, PACKET_ID, buf);
        });
    }

    public static void register() {
        ClientSidePacketRegistry.INSTANCE.register(PACKET_ID, DroneQuaternionS2C::accept);
    }
}
