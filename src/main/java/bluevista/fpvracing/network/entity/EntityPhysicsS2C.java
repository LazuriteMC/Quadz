package bluevista.fpvracing.network.entity;

import bluevista.fpvracing.config.Config;
import bluevista.fpvracing.physics.entity.ClientEntityPhysics;
import bluevista.fpvracing.physics.entity.EntityPhysics;
import bluevista.fpvracing.server.entities.FlyableEntity;
import bluevista.fpvracing.util.PacketHelper;
import bluevista.fpvracing.server.ServerInitializer;
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
 * The packet responsible for sending entity physics information from the server to the client.
 * @author Ethan Johnson
 */
public class EntityPhysicsS2C {
    public static final Identifier PACKET_ID = new Identifier(ServerInitializer.MODID, "entity_physics_s2c");

    /**
     * Accepts the packet. Server-side attributes are received from the server in this method including camera info,
     * physics info, and rate info.
     * @param context the packet context
     * @param buf the buffer containing the information
     */
    public static void accept(PacketContext context, PacketByteBuf buf) {
        PlayerEntity player = context.getPlayer();

        boolean force = buf.readBoolean();

        int entityID = buf.readInt();
        int playerID = buf.readInt();

        int noClip = buf.readInt();
        int godMode = buf.readInt();

        float mass = buf.readFloat();
        int size = buf.readInt();
        float thrust = buf.readFloat();
        float thrustCurve = buf.readFloat();
        float dragCoefficient = buf.readFloat();

        Vector3f position = PacketHelper.deserializeVector3f(buf);
        Vector3f linearVel = PacketHelper.deserializeVector3f(buf);
        Vector3f angularVel = PacketHelper.deserializeVector3f(buf);
        Quat4f orientation = PacketHelper.deserializeQuaternion(buf);

        context.getTaskQueue().execute(() -> {
            FlyableEntity entity;
            ClientEntityPhysics physics;

            if (player != null) {
                entity = (FlyableEntity) player.world.getEntityById(entityID);

                if (entity != null) {
                    physics = (ClientEntityPhysics) entity.getPhysics();

                    /* Misc Attributes */
                    physics.setConfigValues(Config.NO_CLIP, noClip);
                    physics.setConfigValues(Config.GOD_MODE, godMode);
                    physics.setConfigValues(Config.PLAYER_ID, playerID);

                    /* Physics Settings */
                    physics.setConfigValues(Config.MASS, mass);
                    physics.setConfigValues(Config.SIZE, size);
                    physics.setConfigValues(Config.THRUST, thrust);
                    physics.setConfigValues(Config.THRUST_CURVE, thrustCurve);
                    physics.setConfigValues(Config.DRAG_COEFFICIENT, dragCoefficient);

                    /* Physics Vectors (orientation, position, velocity, etc.) */
                    if (force || !physics.isActive()) {
                        physics.setPosition(position);
                        physics.getRigidBody().setLinearVelocity(linearVel);
                        physics.getRigidBody().setAngularVelocity(angularVel);
                        physics.setNetOrientation(orientation);
                    }
                }
            }
        });
    }

    /**
     * The method that send the drone information from the server to the client. Contains all
     * server-side values such as camera settings, physics settings, and rate settings.
     * @param physics the {@link bluevista.fpvracing.physics.entity.EntityPhysics} to send
     */
    public static void send(EntityPhysics physics, boolean force) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());

        buf.writeBoolean(force);

        /* Identifiers */
        buf.writeInt(physics.getEntity().getEntityId());
        buf.writeInt(physics.getConfigValues(Config.PLAYER_ID).intValue());

        /* Misc Attributes */
        buf.writeInt(physics.getConfigValues(Config.NO_CLIP).intValue());
        buf.writeInt(physics.getConfigValues(Config.GOD_MODE).intValue());

        /* Physics Settings */
        buf.writeFloat(physics.getConfigValues(Config.MASS).floatValue());
        buf.writeInt(physics.getConfigValues(Config.SIZE).intValue());
        buf.writeFloat(physics.getConfigValues(Config.THRUST).floatValue());
        buf.writeFloat(physics.getConfigValues(Config.THRUST_CURVE).floatValue());
        buf.writeFloat(physics.getConfigValues(Config.DRAG_COEFFICIENT).floatValue());

        /* Physics Vectors */
        PacketHelper.serializeVector3f(buf, physics.getPosition());
        PacketHelper.serializeVector3f(buf, physics.getLinearVelocity());
        PacketHelper.serializeVector3f(buf, physics.getAngularVelocity());
        PacketHelper.serializeQuaternion(buf, physics.getOrientation());

        Stream<PlayerEntity> watchingPlayers = PlayerStream.watching(physics.getEntity().getEntityWorld(), new BlockPos(physics.getEntity().getPos()));
        watchingPlayers.forEach(player -> ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, PACKET_ID, buf));
    }

    /**
     * Registers the packet in {@link bluevista.fpvracing.client.ClientInitializer}.
     */
    public static void register() {
        ClientSidePacketRegistry.INSTANCE.register(PACKET_ID, EntityPhysicsS2C::accept);
    }
}
