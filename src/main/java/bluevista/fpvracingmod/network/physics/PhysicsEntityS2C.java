package bluevista.fpvracingmod.network.physics;

import bluevista.fpvracingmod.physics.PhysicsEntity;
import bluevista.fpvracingmod.server.ServerInitializer;
import bluevista.fpvracingmod.server.entities.DroneEntity;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.fabric.api.server.PlayerStream;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.util.stream.Stream;

public class PhysicsEntityS2C {
    public static final Identifier PACKET_ID = new Identifier(ServerInitializer.MODID, "physics_entity_s2c");

    public static void accept(PacketContext context, PacketByteBuf buf) {
        PlayerEntity player = context.getPlayer();

        int entityID = buf.readInt();
        float mass = buf.readFloat();
        Vector3f position = new Vector3f(
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat());
        Vector3f linearVel = new Vector3f(
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat());
        Vector3f angularVel = new Vector3f(
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat());
        Quat4f orientation = new Quat4f(
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat());

        context.getTaskQueue().execute(() -> {
            Entity entity;
            if(player != null) {
                entity = player.world.getEntityById(entityID);
                if(entity instanceof DroneEntity) {
                    DroneEntity drone = (DroneEntity) entity;

                    Quat4f realOrientation = orientation;
                    if(drone.physics != null) {
                        realOrientation = drone.physics.getOrientation();
                        drone.physics.remove();
                    }

                    drone.physics = new PhysicsEntity(drone);
                    drone.physics.getRigidBody().setLinearVelocity(linearVel);
                    drone.physics.getRigidBody().setAngularVelocity(angularVel);
                    drone.physics.setPosition(position);
                    drone.physics.setOrientation(realOrientation);
                    drone.physics.setMass(mass);
                }
            }
        });
    }

    public static void send(PhysicsEntity physics) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        Entity entity = physics.getEntity();

        buf.writeInt(physics.getEntity().getEntityId());
        buf.writeFloat(physics.getMass());
        buf.writeFloat((float) physics.getPosition().x);
        buf.writeFloat((float) physics.getPosition().y);
        buf.writeFloat((float) physics.getPosition().z);
        buf.writeFloat(physics.getRigidBody().getLinearVelocity(new Vector3f()).x);
        buf.writeFloat(physics.getRigidBody().getLinearVelocity(new Vector3f()).y);
        buf.writeFloat(physics.getRigidBody().getLinearVelocity(new Vector3f()).z);
        buf.writeFloat(physics.getRigidBody().getAngularVelocity(new Vector3f()).x);
        buf.writeFloat(physics.getRigidBody().getAngularVelocity(new Vector3f()).y);
        buf.writeFloat(physics.getRigidBody().getAngularVelocity(new Vector3f()).z);
        buf.writeFloat(physics.getOrientation().x);
        buf.writeFloat(physics.getOrientation().y);
        buf.writeFloat(physics.getOrientation().z);
        buf.writeFloat(physics.getOrientation().w);

        Stream<PlayerEntity> watchingPlayers = PlayerStream.watching(entity.getEntityWorld(), new BlockPos(entity.getPos()));
        watchingPlayers.forEach(player -> ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, PACKET_ID, buf));
    }

    public static void register() {
        ClientSidePacketRegistry.INSTANCE.register(PACKET_ID, PhysicsEntityS2C::accept);
    }
}
