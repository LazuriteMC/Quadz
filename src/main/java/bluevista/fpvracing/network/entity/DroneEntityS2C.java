package bluevista.fpvracing.network.entity;

import bluevista.fpvracing.config.Config;
import bluevista.fpvracing.server.ServerInitializer;
import bluevista.fpvracing.server.entities.QuadcopterEntity;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.fabric.api.server.PlayerStream;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.stream.Stream;

/**
 * The packet responsible for sending drone information from the server to the client.
 * @author Ethan Johnson
 */
public class DroneEntityS2C {
    public static final Identifier PACKET_ID = new Identifier(ServerInitializer.MODID, "drone_entity_s2c");

    /**
     * @param context the packet context
     * @param buf the buffer containing the information
     */
    public static void accept(PacketContext context, PacketByteBuf buf) {
        PlayerEntity player = context.getPlayer();
        int entityID = buf.readInt();
        int cameraAngle = buf.readInt();

        float rate = buf.readFloat();
        float superRate = buf.readFloat();
        float expo = buf.readFloat();

        context.getTaskQueue().execute(() -> {
            QuadcopterEntity drone = null;

            if (player != null)
                drone = (QuadcopterEntity) player.world.getEntityById(entityID);

            if (drone != null) {
                drone.setConfigValues(Config.CAMERA_ANGLE, cameraAngle);
                drone.setConfigValues(Config.RATE, rate);
                drone.setConfigValues(Config.SUPER_RATE, superRate);
                drone.setConfigValues(Config.EXPO, expo);
            }
        });
    }

    /**
     * @param drone the {@link QuadcopterEntity} to send
     */
    public static void send(QuadcopterEntity drone) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeInt(drone.getEntityId());

        buf.writeInt(drone.getConfigValues(Config.CAMERA_ANGLE).intValue());
        buf.writeFloat(drone.getConfigValues(Config.RATE).floatValue());
        buf.writeFloat(drone.getConfigValues(Config.SUPER_RATE).floatValue());
        buf.writeFloat(drone.getConfigValues(Config.EXPO).floatValue());

        Stream<PlayerEntity> watchingPlayers = PlayerStream.watching(drone.getEntityWorld(), new BlockPos(drone.getPos()));
        watchingPlayers.forEach(player -> ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, PACKET_ID, buf));
    }

    /**
     * Registers the packet in {@link bluevista.fpvracing.client.ClientInitializer}.
     */
    public static void register() {
        ClientSidePacketRegistry.INSTANCE.register(PACKET_ID, DroneEntityS2C::accept);
    }
}
