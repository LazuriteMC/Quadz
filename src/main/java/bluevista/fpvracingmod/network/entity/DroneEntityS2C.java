package bluevista.fpvracingmod.network.entity;

import bluevista.fpvracingmod.config.Config;
import bluevista.fpvracingmod.network.PacketHelper;
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

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.util.UUID;
import java.util.stream.Stream;

public class DroneEntityS2C {
    public static final Identifier PACKET_ID = new Identifier(ServerInitializer.MODID, "drone_entity_s2c");

    public static void accept(PacketContext context, PacketByteBuf buf) {
        PlayerEntity player = context.getPlayer();

        int droneID = buf.readInt();
        int bindID = buf.readInt();
        UUID playerID = buf.readUuid();

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
        int crashMomentumThreshold = buf.readInt();

        Quat4f orientation = PacketHelper.deserializeQuaternion(buf);
        Vector3f position = PacketHelper.deserializeVector3f(buf);
        Vector3f linearVel = PacketHelper.deserializeVector3f(buf);
        Vector3f angularVel = PacketHelper.deserializeVector3f(buf);

        context.getTaskQueue().execute(() -> {
            DroneEntity drone = null;

            if (player != null)
                drone = (DroneEntity) player.world.getEntityById(droneID);

            if (drone != null) {
                drone.playerID = playerID;

                drone.setConfigValues(Config.BIND, bindID);
                drone.setConfigValues(Config.NO_CLIP, noClip);
                drone.setConfigValues(Config.GOD_MODE, godMode);
                drone.setConfigValues(Config.DAMAGE_COEFFICIENT, damageCoefficient);

                drone.setConfigValues(Config.BAND, band);
                drone.setConfigValues(Config.CHANNEL, channel);
                drone.setConfigValues(Config.CAMERA_ANGLE, cameraAngle);
                drone.setConfigValues(Config.FIELD_OF_VIEW, fieldOfView);

                drone.setConfigValues(Config.RATE, rate);
                drone.setConfigValues(Config.SUPER_RATE, superRate);
                drone.setConfigValues(Config.EXPO, expo);

                drone.setConfigValues(Config.MASS, mass);
                drone.setConfigValues(Config.SIZE, size);
                drone.setConfigValues(Config.THRUST, thrust);
                drone.setConfigValues(Config.THRUST_CURVE, thrustCurve);
                drone.setConfigValues(Config.DRAG_COEFFICIENT, dragCoefficient);
                drone.setConfigValues(Config.CRASH_MOMENTUM_THRESHOLD, crashMomentumThreshold);

                if (!drone.isActive()) {
                    drone.netQuat.set(orientation);
                    drone.setRigidBodyPos(position);
                    drone.getRigidBody().setLinearVelocity(linearVel);
                    drone.getRigidBody().setAngularVelocity(angularVel);
                }
            }
        });
    }

    public static void send(DroneEntity drone) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());

        buf.writeInt(drone.getEntityId());
        buf.writeInt(drone.getConfigValues(Config.BIND).intValue());
        buf.writeUuid(drone.playerID);

        buf.writeInt(drone.getConfigValues(Config.NO_CLIP).intValue());
        buf.writeInt(drone.getConfigValues(Config.GOD_MODE).intValue());
        buf.writeFloat(drone.getConfigValues(Config.DAMAGE_COEFFICIENT).floatValue());

        buf.writeInt(drone.getConfigValues(Config.BAND).intValue());
        buf.writeInt(drone.getConfigValues(Config.CHANNEL).intValue());
        buf.writeInt(drone.getConfigValues(Config.CAMERA_ANGLE).intValue());
        buf.writeFloat(drone.getConfigValues(Config.FIELD_OF_VIEW).floatValue());

        buf.writeFloat(drone.getConfigValues(Config.RATE).floatValue());
        buf.writeFloat(drone.getConfigValues(Config.SUPER_RATE).floatValue());
        buf.writeFloat(drone.getConfigValues(Config.EXPO).floatValue());

        buf.writeFloat(drone.getConfigValues(Config.MASS).floatValue());
        buf.writeInt(drone.getConfigValues(Config.SIZE).intValue());
        buf.writeFloat(drone.getConfigValues(Config.THRUST).floatValue());
        buf.writeFloat(drone.getConfigValues(Config.THRUST_CURVE).floatValue());
        buf.writeFloat(drone.getConfigValues(Config.DRAG_COEFFICIENT).floatValue());
        buf.writeInt(drone.getConfigValues(Config.CRASH_MOMENTUM_THRESHOLD).intValue());

        PacketHelper.serializeQuaternion(buf, drone.getOrientation());
        PacketHelper.serializeVector3f(buf, drone.getRigidBody().getCenterOfMassPosition(new Vector3f()));
        PacketHelper.serializeVector3f(buf, drone.getRigidBody().getLinearVelocity(new Vector3f()));
        PacketHelper.serializeVector3f(buf, drone.getRigidBody().getAngularVelocity(new Vector3f()));

        Stream<PlayerEntity> watchingPlayers = PlayerStream.watching(drone.getEntityWorld(), new BlockPos(drone.getPos()));
        watchingPlayers.forEach(player -> ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, PACKET_ID, buf));
    }

    public static void register() {
        ClientSidePacketRegistry.INSTANCE.register(PACKET_ID, DroneEntityS2C::accept);
    }
}
