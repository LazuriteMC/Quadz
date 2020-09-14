package bluevista.fpvracingmod.network.entity;

import bluevista.fpvracingmod.config.Config;
import bluevista.fpvracingmod.network.GenericBuffer;
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

        int droneID = buf.readInt();
        UUID playerID = buf.readUuid();

        float mass = buf.readFloat();
        float linearDamping = buf.readFloat();

        GenericBuffer<Quat4f> quatBuf = PacketHelper.deserializeQuaternionBuffer(buf);
        GenericBuffer<Vector3f> posBuf = PacketHelper.deserializePositionBuffer(buf);
        Vector3f linearVel = PacketHelper.deserializeVector3f(buf);
        Vector3f angularVel = PacketHelper.deserializeVector3f(buf);

        context.getTaskQueue().execute(() -> {
            PhysicsEntity physics = null;

            if(player != null)
                physics = (PhysicsEntity) player.world.getEntityById(droneID);

            if(physics != null) {
                physics.playerID = playerID;

                physics.setConfigValues(Config.MASS, mass);
                physics.setConfigValues(Config.LINEAR_DAMPING, linearDamping);

                if(!physics.playerID.equals(player.getUuid())) { // if any player besides physics-controlling player
                    physics.setQuaternionBuffer(quatBuf);
                    physics.setPositionBuffer(posBuf);
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

        buf.writeFloat(physics.getMass());
        buf.writeFloat(physics.getLinearDamping());

        PacketHelper.serializeQuaternionBuffer(buf, physics.getQuaternionBuffer());
        PacketHelper.serializePositionBuffer(buf, physics.getPositionBuffer());
        PacketHelper.serializeVector3f(buf, physics.getRigidBody().getLinearVelocity(new Vector3f()));
        PacketHelper.serializeVector3f(buf, physics.getRigidBody().getAngularVelocity(new Vector3f()));

        Stream<PlayerEntity> watchingPlayers = PlayerStream.watching(physics.getEntityWorld(), new BlockPos(physics.getPos()));
        watchingPlayers.forEach(player -> ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, PACKET_ID, buf));
    }

    public static void register() {
        ClientSidePacketRegistry.INSTANCE.register(PACKET_ID, PhysicsEntityS2C::accept);
    }
}
