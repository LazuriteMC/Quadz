package bluevista.fpvracing.network.entity;

import bluevista.fpvracing.config.Config;
import bluevista.fpvracing.server.ServerInitializer;
import bluevista.fpvracing.server.entities.FlyableEntity;
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
 * @author Ethan Johnson
 */
public class FlyableEntityS2C {
    public static final Identifier PACKET_ID = new Identifier(ServerInitializer.MODID, "flyable_entity_s2c");

    /**
     * @param context the packet context
     * @param buf the buffer containing the information
     */
    public static void accept(PacketContext context, PacketByteBuf buf) {
        PlayerEntity player = context.getPlayer();

        int entityID = buf.readInt();
        int bindID = buf.readInt();

        int band = buf.readInt();
        int channel = buf.readInt();
        float fieldOfView = buf.readFloat();

        context.getTaskQueue().execute(() -> {
            FlyableEntity flyable;

            if (player != null) {
                flyable = (FlyableEntity) player.world.getEntityById(entityID);

                if (flyable != null) {
                    flyable.setConfigValues(Config.BIND, bindID);
                    flyable.setConfigValues(Config.BAND, band);
                    flyable.setConfigValues(Config.CHANNEL, channel);
                    flyable.setConfigValues(Config.FIELD_OF_VIEW, fieldOfView);
                }
            }
        });
    }

    /**
     * @param flyable the {@link FlyableEntity} to send
     */
    public static void send(FlyableEntity flyable) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());

        buf.writeInt(flyable.getEntityId());
        buf.writeInt(flyable.getConfigValues(Config.BIND).intValue());

        buf.writeInt(flyable.getConfigValues(Config.BAND).intValue());
        buf.writeInt(flyable.getConfigValues(Config.CHANNEL).intValue());
        buf.writeFloat(flyable.getConfigValues(Config.FIELD_OF_VIEW).floatValue());

        Stream<PlayerEntity> watchingPlayers = PlayerStream.watching(flyable.getEntityWorld(), new BlockPos(flyable.getPos()));
        watchingPlayers.forEach(player -> ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, PACKET_ID, buf));
    }

    /**
     * Registers the packet in {@link bluevista.fpvracing.client.ClientInitializer}.
     */
    public static void register() {
        ClientSidePacketRegistry.INSTANCE.register(PACKET_ID, FlyableEntityS2C::accept);
    }
}
