package bluevista.fpvracingmod.network.entity;

import bluevista.fpvracingmod.config.Config;
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

import java.util.stream.Stream;

public class DroneEntityS2C {
    public static final Identifier PACKET_ID = new Identifier(ServerInitializer.MODID, "drone_entity_s2c");

    public static void accept(PacketContext context, PacketByteBuf buf) {
        PlayerEntity player = context.getPlayer();

        int droneID = buf.readInt();
        int noClip = buf.readInt();
        int godMode = buf.readInt();

        int band = buf.readInt();
        int channel = buf.readInt();
        int cameraAngle = buf.readInt();
        float fieldOfView = buf.readFloat();
        float thrust = buf.readFloat();
        int crashMomentumThreshold = buf.readInt();

        float rate = buf.readFloat();
        float superRate = buf.readFloat();
        float expo = buf.readFloat();

        context.getTaskQueue().execute(() -> {
            DroneEntity drone = null;

            if(player != null)
                drone = (DroneEntity) player.world.getEntityById(droneID);

            if(drone != null) {
                drone.setConfigValues(Config.NO_CLIP, noClip);
                drone.setConfigValues(Config.GOD_MODE, godMode);

                drone.setConfigValues(Config.BAND, band);
                drone.setConfigValues(Config.CHANNEL, channel);
                drone.setConfigValues(Config.CAMERA_ANGLE, cameraAngle);
                drone.setConfigValues(Config.FIELD_OF_VIEW, fieldOfView);
                drone.setConfigValues(Config.THRUST, thrust);
                drone.setConfigValues(Config.CRASH_MOMENTUM_THRESHOLD, crashMomentumThreshold);

                drone.setConfigValues(Config.RATE, rate);
                drone.setConfigValues(Config.SUPER_RATE, superRate);
                drone.setConfigValues(Config.EXPO, expo);
            }
        });
    }

    public static void send(DroneEntity drone) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());

        buf.writeInt(drone.getEntityId());
        buf.writeInt(drone.getConfigValues(Config.NO_CLIP).intValue());
        buf.writeInt(drone.getConfigValues(Config.GOD_MODE).intValue());

        buf.writeInt(drone.getConfigValues(Config.BAND).intValue());
        buf.writeInt(drone.getConfigValues(Config.CHANNEL).intValue());
        buf.writeInt(drone.getConfigValues(Config.CAMERA_ANGLE).intValue());
        buf.writeFloat(drone.getConfigValues(Config.FIELD_OF_VIEW).floatValue());
        buf.writeFloat(drone.getConfigValues(Config.THRUST).floatValue());
        buf.writeInt(drone.getConfigValues(Config.CRASH_MOMENTUM_THRESHOLD).intValue());

        buf.writeFloat(drone.getConfigValues(Config.RATE).floatValue());
        buf.writeFloat(drone.getConfigValues(Config.SUPER_RATE).floatValue());
        buf.writeFloat(drone.getConfigValues(Config.EXPO).floatValue());

        Stream<PlayerEntity> watchingPlayers = PlayerStream.watching(drone.getEntityWorld(), new BlockPos(drone.getPos()));
        watchingPlayers.forEach(player -> ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, PACKET_ID, buf));
    }

    public static void register() {
        ClientSidePacketRegistry.INSTANCE.register(PACKET_ID, DroneEntityS2C::accept);
    }
}
