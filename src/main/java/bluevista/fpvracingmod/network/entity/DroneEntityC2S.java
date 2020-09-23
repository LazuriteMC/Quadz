package bluevista.fpvracingmod.network.entity;

import bluevista.fpvracingmod.network.PacketHelper;
import bluevista.fpvracingmod.server.ServerInitializer;
import bluevista.fpvracingmod.server.entities.DroneEntity;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

public class DroneEntityC2S {
    public static final Identifier PACKET_ID = new Identifier(ServerInitializer.MODID, "drone_entity_c2s");

    public static void accept(PacketContext context, PacketByteBuf buf) {
        PlayerEntity player = context.getPlayer();

        int id = buf.readInt();
        float yaw = buf.readFloat();
        float pitch = buf.readFloat();
        boolean removed = buf.readBoolean();

        Quat4f orientation = PacketHelper.deserializeQuaternion(buf);
        Vector3f position = PacketHelper.deserializeVector3f(buf);
        Vector3f linearVel = PacketHelper.deserializeVector3f(buf);
        Vector3f angularVel = PacketHelper.deserializeVector3f(buf);

        context.getTaskQueue().execute(() -> {
            DroneEntity drone = null;

            if (player != null)
                drone = (DroneEntity) player.world.getEntityById(id);

            if (drone != null) {
                drone.yaw = yaw;
                drone.pitch = pitch;

                if (removed && !drone.removed) {
                    drone.kill();
                }

                drone.setOrientation(orientation);
                drone.setRigidBodyPos(position);

                drone.getRigidBody().setLinearVelocity(linearVel);
                drone.getRigidBody().setAngularVelocity(angularVel);
            }
        });
    }

    public static void send(DroneEntity drone) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());

        buf.writeInt(drone.getEntityId());
        buf.writeFloat(drone.yaw);
        buf.writeFloat(drone.pitch);
        buf.writeBoolean(drone.removed);

        PacketHelper.serializeQuaternion(buf, drone.getOrientation());
        PacketHelper.serializeVector3f(buf, drone.getRigidBody().getCenterOfMassPosition(new Vector3f()));
        PacketHelper.serializeVector3f(buf, drone.getRigidBody().getLinearVelocity(new Vector3f()));
        PacketHelper.serializeVector3f(buf, drone.getRigidBody().getAngularVelocity(new Vector3f()));

        ClientSidePacketRegistry.INSTANCE.sendToServer(PACKET_ID, buf);
    }

    public static void register() {
        ServerSidePacketRegistry.INSTANCE.register(PACKET_ID, DroneEntityC2S::accept);
    }
}
