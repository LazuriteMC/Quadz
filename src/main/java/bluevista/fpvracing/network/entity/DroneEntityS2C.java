package bluevista.fpvracing.network.entity;

import bluevista.fpvracing.config.Config;
import bluevista.fpvracing.util.PacketHelper;
import bluevista.fpvracing.server.ServerInitializer;
import bluevista.fpvracing.server.entities.DroneEntity;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.fabric.api.server.PlayerStream;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.util.stream.Stream;

/**
 * The packet responsible for sending drone information from the server to the client.
 * @author Ethan Johnson
 */
public class DroneEntityS2C {
    public static final Identifier PACKET_ID = new Identifier(ServerInitializer.MODID, "drone_entity_s2c");

    /**
     * Accepts the packet. Server-side attributes are received from the server in this method including camera info,
     * physics info, and rate info.
     * @param context the packet context
     * @param buf the buffer containing the information
     */
    public static void accept(PacketContext context, PacketByteBuf buf) {
        PlayerEntity player = context.getPlayer();

        int droneID = buf.readInt();
        int bindID = buf.readInt();
        int playerID = buf.readInt();

        int noClip = buf.readInt();
        int godMode = buf.readInt();
        float damageCoefficient = buf.readFloat();

        int band = buf.readInt();
        int channel = buf.readInt();
        int cameraAngle = buf.readInt();
        float fieldOfView = buf.readFloat();

        float rate = buf.readFloat();
        float superRate = buf.readFloat();
        float expo = buf.readFloat();

        float mass = buf.readFloat();
        int size = buf.readInt();
        float thrust = buf.readFloat();
        float thrustCurve = buf.readFloat();
        float dragCoefficient = buf.readFloat();
//        int crashMomentumThreshold = buf.readInt();

        Quat4f orientation = PacketHelper.deserializeQuaternion(buf);
        Vector3f position = PacketHelper.deserializeVector3f(buf);
        Vector3f linearVel = PacketHelper.deserializeVector3f(buf);
        Vector3f angularVel = PacketHelper.deserializeVector3f(buf);

        context.getTaskQueue().execute(() -> {
            DroneEntity drone = null;

            if (player != null)
                drone = (DroneEntity) player.world.getEntityById(droneID);

            if (drone != null) {
                /* Misc Attributes */
                drone.setConfigValues(Config.PLAYER_ID, playerID);
                drone.setConfigValues(Config.BIND, bindID);
                drone.setConfigValues(Config.NO_CLIP, noClip);
                drone.setConfigValues(Config.GOD_MODE, godMode);

                /* Camera Settings */
                drone.setConfigValues(Config.BAND, band);
                drone.setConfigValues(Config.CHANNEL, channel);
                drone.setConfigValues(Config.CAMERA_ANGLE, cameraAngle);
                drone.setConfigValues(Config.FIELD_OF_VIEW, fieldOfView);

                /* Rate Settings */
                drone.setConfigValues(Config.RATE, rate);
                drone.setConfigValues(Config.SUPER_RATE, superRate);
                drone.setConfigValues(Config.EXPO, expo);

                /* Physics Settings */
                drone.setConfigValues(Config.MASS, mass);
                drone.setConfigValues(Config.SIZE, size);
                drone.setConfigValues(Config.THRUST, thrust);
                drone.setConfigValues(Config.THRUST_CURVE, thrustCurve);
                drone.setConfigValues(Config.DRAG_COEFFICIENT, dragCoefficient);
                drone.setConfigValues(Config.DAMAGE_COEFFICIENT, damageCoefficient);
//                drone.setConfigValues(Config.CRASH_MOMENTUM_THRESHOLD, crashMomentumThreshold);

                /* Physics Vectors (orientation, position, velocity, etc.) */
                if (!drone.physics.isActive()) {
                    drone.physics.setNetOrientation(orientation);
                    drone.physics.setRigidBodyPos(position);
                    drone.physics.getRigidBody().setLinearVelocity(linearVel);
                    drone.physics.getRigidBody().setAngularVelocity(angularVel);
                }
            }
        });
    }

    /**
     * The method that send the drone information from the server to the client. Contains all
     * server-side values such as camera settings, physics settings, and rate settings.
     * @param drone the {@link DroneEntity} to send
     */
    public static void send(DroneEntity drone) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeInt(drone.getEntityId());

        /* Misc Attributes */
        buf.writeInt(drone.getConfigValues(Config.BIND).intValue());
        buf.writeInt(drone.getConfigValues(Config.PLAYER_ID).intValue());
        buf.writeInt(drone.getConfigValues(Config.NO_CLIP).intValue());
        buf.writeInt(drone.getConfigValues(Config.GOD_MODE).intValue());
        buf.writeFloat(drone.getConfigValues(Config.DAMAGE_COEFFICIENT).floatValue());

        /* Camera Settings */
        buf.writeInt(drone.getConfigValues(Config.BAND).intValue());
        buf.writeInt(drone.getConfigValues(Config.CHANNEL).intValue());
        buf.writeInt(drone.getConfigValues(Config.CAMERA_ANGLE).intValue());
        buf.writeFloat(drone.getConfigValues(Config.FIELD_OF_VIEW).floatValue());

        /* Rate Settings */
        buf.writeFloat(drone.getConfigValues(Config.RATE).floatValue());
        buf.writeFloat(drone.getConfigValues(Config.SUPER_RATE).floatValue());
        buf.writeFloat(drone.getConfigValues(Config.EXPO).floatValue());

        /* Physics Settings */
        buf.writeFloat(drone.getConfigValues(Config.MASS).floatValue());
        buf.writeInt(drone.getConfigValues(Config.SIZE).intValue());
        buf.writeFloat(drone.getConfigValues(Config.THRUST).floatValue());
        buf.writeFloat(drone.getConfigValues(Config.THRUST_CURVE).floatValue());
        buf.writeFloat(drone.getConfigValues(Config.DRAG_COEFFICIENT).floatValue());
//        buf.writeInt(drone.getConfigValues(Config.CRASH_MOMENTUM_THRESHOLD).intValue());

        /* Physics Vectors */
        PacketHelper.serializeQuaternion(buf, drone.physics.getOrientation());
        PacketHelper.serializeVector3f(buf, drone.physics.getRigidBody().getCenterOfMassPosition(new Vector3f()));
        PacketHelper.serializeVector3f(buf, drone.physics.getRigidBody().getLinearVelocity(new Vector3f()));
        PacketHelper.serializeVector3f(buf, drone.physics.getRigidBody().getAngularVelocity(new Vector3f()));

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
