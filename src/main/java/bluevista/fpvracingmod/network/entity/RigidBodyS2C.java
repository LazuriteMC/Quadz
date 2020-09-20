package bluevista.fpvracingmod.network.entity;

import bluevista.fpvracingmod.network.PacketHelper;
import bluevista.fpvracingmod.server.ServerInitializer;
import bluevista.fpvracingmod.server.entities.PhysicsEntity;
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

public class RigidBodyS2C {
    public static final Identifier PACKET_ID = new Identifier(ServerInitializer.MODID, "rigid_body_s2c");

    public static void accept(PacketContext context, PacketByteBuf buf) {
        PlayerEntity player = context.getPlayer();

        int id = buf.readInt();
        boolean initial = buf.readBoolean();

        Quat4f orientation = PacketHelper.deserializeQuaternion(buf);
        Vector3f position = PacketHelper.deserializeVector3f(buf);
        Vector3f linearVel = PacketHelper.deserializeVector3f(buf);
        Vector3f angularVel = PacketHelper.deserializeVector3f(buf);

        context.getTaskQueue().execute(() -> {
            PhysicsEntity physics = null;

            if (player != null) {
                physics = (PhysicsEntity) player.world.getEntityById(id);
            }

            if (physics != null) {
                if (initial) {
                    System.out.println("SET ORIENTATION +++++++++++++++++++++++++++++++++");
                    physics.setOrientation(orientation);
                }

                physics.netQuat.set(orientation);
                physics.updatePosition(position.x, position.y, position.z);
                physics.setRigidBodyPos(position);
                physics.getRigidBody().setLinearVelocity(linearVel);
                physics.getRigidBody().setAngularVelocity(angularVel);
            }
        });
    }

    public static void send(PhysicsEntity physics, boolean initial) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());

        buf.writeInt(physics.getEntityId());
        buf.writeBoolean(initial);

        PacketHelper.serializeQuaternion(buf, physics.getOrientation());
        PacketHelper.serializeVector3f(buf, physics.getRigidBody().getCenterOfMassPosition(new Vector3f()));
        PacketHelper.serializeVector3f(buf, physics.getRigidBody().getLinearVelocity(new Vector3f()));
        PacketHelper.serializeVector3f(buf, physics.getRigidBody().getAngularVelocity(new Vector3f()));

        Stream<PlayerEntity> watchingPlayers = PlayerStream.watching(physics.getEntityWorld(), new BlockPos(physics.getPos()));
        watchingPlayers.forEach(player -> {
            if (!player.getUuid().equals(physics.playerID) || initial) {
                ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, PACKET_ID, buf);
            }
        });
    }

    public static void register() {
        ClientSidePacketRegistry.INSTANCE.register(PACKET_ID, RigidBodyS2C::accept);
    }
}
