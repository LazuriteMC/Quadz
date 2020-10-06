package bluevista.fpvracing.network.entity;

import bluevista.fpvracing.util.PacketHelper;
import bluevista.fpvracing.server.ServerInitializer;
import bluevista.fpvracing.server.entities.DroneEntity;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

/**
 * The packet responsible for sending drone information from the client to the server. This packet is sent
 * exclusively by the client which is responsible for the drone's physics. The server, after it receives the info,
 * disperses it to the rest of the clients using {@link DroneEntityS2C}.
 * @author Ethan Johnson
 */
public class DroneEntityC2S {
    public static final Identifier PACKET_ID = new Identifier(ServerInitializer.MODID, "drone_entity_c2s");

    /**
     * Accepts the packet. Client physics information is mainly received.
     * @param context the packet context
     * @param buf the buffer containing the information
     */
    public static void accept(PacketContext context, PacketByteBuf buf) {
        PlayerEntity player = context.getPlayer();

        int id = buf.readInt();
        boolean removed = buf.readBoolean();

        float yaw = buf.readFloat();
        float pitch = buf.readFloat();

        Quat4f orientation = PacketHelper.deserializeQuaternion(buf);
        Vector3f position = PacketHelper.deserializeVector3f(buf);
        Vector3f linearVel = PacketHelper.deserializeVector3f(buf);
        Vector3f angularVel = PacketHelper.deserializeVector3f(buf);

        context.getTaskQueue().execute(() -> {
            DroneEntity drone = null;

            if (player != null)
                drone = (DroneEntity) player.world.getEntityById(id);

            if (drone != null) {
                if (removed && !drone.removed) {
                    drone.kill();
                }

                /* Minecraft Camera Orientation */
                drone.yaw = yaw;
                drone.pitch = pitch;

                /* Physics Vectors (orientation, position, velocity) */
                drone.physics.setRigidBodyPos(position);
                drone.physics.setOrientation(orientation);
                drone.physics.getRigidBody().setLinearVelocity(linearVel);
                drone.physics.getRigidBody().setAngularVelocity(angularVel);
            }
        });
    }

    /**
     * Sends the packet to the server. Mainly just includes physics information. Also includes
     * camera rotation (yaw, pitch).
     * @param drone the {@link DroneEntity} to send
     */
    public static void send(DroneEntity drone) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());

        buf.writeInt(drone.getEntityId());
        buf.writeBoolean(drone.removed);

        /* Minecraft Camera Orientation */
        buf.writeFloat(drone.yaw);
        buf.writeFloat(drone.pitch);

        /* Physics Vectors */
        PacketHelper.serializeQuaternion(buf, drone.physics.getOrientation());
        PacketHelper.serializeVector3f(buf, drone.physics.getRigidBody().getCenterOfMassPosition(new Vector3f()));
        PacketHelper.serializeVector3f(buf, drone.physics.getRigidBody().getLinearVelocity(new Vector3f()));
        PacketHelper.serializeVector3f(buf, drone.physics.getRigidBody().getAngularVelocity(new Vector3f()));

        ClientSidePacketRegistry.INSTANCE.sendToServer(PACKET_ID, buf);
    }

    /**
     * Registers the packet in {@link ServerInitializer}.
     */
    public static void register() {
        ServerSidePacketRegistry.INSTANCE.register(PACKET_ID, DroneEntityC2S::accept);
    }
}
