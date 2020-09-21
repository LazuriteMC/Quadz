package bluevista.fpvracingmod.network.entity;

import bluevista.fpvracingmod.network.PacketHelper;
import bluevista.fpvracingmod.server.ServerInitializer;
import bluevista.fpvracingmod.server.entities.PhysicsEntity;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

public class PhysicsEntityC2S {
    public static final Identifier PACKET_ID = new Identifier(ServerInitializer.MODID, "physics_entity_c2s");

    public static void accept(PacketContext context, PacketByteBuf buf) {
        PlayerEntity player = context.getPlayer();

        int id = buf.readInt();
        float yaw = buf.readFloat();
        float pitch = buf.readFloat();

        Quat4f orientation = PacketHelper.deserializeQuaternion(buf);
        Vector3f position = PacketHelper.deserializeVector3f(buf);
        Vector3f linearVel = PacketHelper.deserializeVector3f(buf);
        Vector3f angularVel = PacketHelper.deserializeVector3f(buf);

        context.getTaskQueue().execute(() -> {
            PhysicsEntity physics = null;

            if (player != null)
                physics = (PhysicsEntity) player.world.getEntityById(id);

            if (physics != null) {
                physics.yaw = yaw;
                physics.pitch = pitch;

                physics.setOrientation(orientation);
                physics.setRigidBodyPos(position);
                physics.resetPosition(position.x, position.y, position.z);
                physics.getRigidBody().setLinearVelocity(linearVel);
                physics.getRigidBody().setAngularVelocity(angularVel);
            }
        });
    }

    public static void send(PhysicsEntity physics) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());

        buf.writeInt(physics.getEntityId());
        buf.writeFloat(physics.yaw);
        buf.writeFloat(physics.pitch);

        PacketHelper.serializeQuaternion(buf, physics.getOrientation());
        PacketHelper.serializeVector3f(buf, physics.getRigidBody().getCenterOfMassPosition(new Vector3f()));
        PacketHelper.serializeVector3f(buf, physics.getRigidBody().getLinearVelocity(new Vector3f()));
        PacketHelper.serializeVector3f(buf, physics.getRigidBody().getAngularVelocity(new Vector3f()));

        ClientSidePacketRegistry.INSTANCE.sendToServer(PACKET_ID, buf);
    }

    public static void register() {
        ServerSidePacketRegistry.INSTANCE.register(PACKET_ID, PhysicsEntityC2S::accept);
    }
}
