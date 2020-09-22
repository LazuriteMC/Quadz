package bluevista.fpvracingmod.network.entity;

import bluevista.fpvracingmod.config.Config;
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
import java.util.UUID;
import java.util.stream.Stream;

public class PhysicsEntityS2C {
    public static final Identifier PACKET_ID = new Identifier(ServerInitializer.MODID, "physics_entity_s2c");

    public static void accept(PacketContext context, PacketByteBuf buf) {
        PlayerEntity player = context.getPlayer();

        int id = buf.readInt();
        UUID playerID = buf.readUuid();

        float mass = buf.readFloat();
        int size = buf.readInt();
        float dragCoefficient = buf.readFloat();

        Quat4f orientation = PacketHelper.deserializeQuaternion(buf);
        Vector3f position = PacketHelper.deserializeVector3f(buf);
        Vector3f linearVel = PacketHelper.deserializeVector3f(buf);
        Vector3f angularVel = PacketHelper.deserializeVector3f(buf);

        context.getTaskQueue().execute(() -> {
            PhysicsEntity physics = null;

            if (player != null)
                physics = (PhysicsEntity) player.world.getEntityById(id);

            if (physics != null) {
                physics.playerID = playerID;

                physics.setConfigValues(Config.MASS, mass);
                physics.setConfigValues(Config.SIZE, size);
                physics.setConfigValues(Config.DRAG_COEFFICIENT, dragCoefficient);

                if (!physics.isActive()) {
                    physics.netQuat.set(orientation);
                    physics.setPos(position.x, position.y, position.z);
                    physics.setRigidBodyPos(position);
                    physics.getRigidBody().setLinearVelocity(linearVel);
                    physics.getRigidBody().setAngularVelocity(angularVel);
                }
            }
        });
    }

    public static void send(PhysicsEntity physics) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());

        buf.writeInt(physics.getEntityId());
        buf.writeUuid(physics.playerID);

        buf.writeFloat(physics.getConfigValues(Config.MASS).floatValue());
        buf.writeInt(physics.getConfigValues(Config.SIZE).intValue());
        buf.writeFloat(physics.getConfigValues(Config.DRAG_COEFFICIENT).floatValue());

        PacketHelper.serializeQuaternion(buf, physics.getOrientation());
        PacketHelper.serializeVector3f(buf, physics.getRigidBody().getCenterOfMassPosition(new Vector3f()));
        PacketHelper.serializeVector3f(buf, physics.getRigidBody().getLinearVelocity(new Vector3f()));
        PacketHelper.serializeVector3f(buf, physics.getRigidBody().getAngularVelocity(new Vector3f()));

        Stream<PlayerEntity> watchingPlayers = PlayerStream.watching(physics.getEntityWorld(), new BlockPos(physics.getPos()));
        watchingPlayers.forEach(player -> ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, PACKET_ID, buf));
    }

    public static void register() {
        ClientSidePacketRegistry.INSTANCE.register(PACKET_ID, PhysicsEntityS2C::accept);
    }
}
