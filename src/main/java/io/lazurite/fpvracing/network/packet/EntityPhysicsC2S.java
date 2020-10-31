package io.lazurite.fpvracing.network.packet;

import io.lazurite.fpvracing.physics.entity.ClientPhysicsHandler;
import io.lazurite.fpvracing.physics.entity.IPhysicsHandler;
import io.lazurite.fpvracing.server.entities.FlyableEntity;
import io.lazurite.fpvracing.util.PacketHelper;
import io.lazurite.fpvracing.server.ServerInitializer;
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
 * The packet responsible for sending physics information from the client to the server. This packet is sent
 * exclusively by the client which is responsible for the entity's physics. The server, after it receives the info,
 * disperses it to the rest of the clients using {@link EntityPhysicsS2C}.
 * @author Ethan Johnson
 */
public class EntityPhysicsC2S {
    public static final Identifier PACKET_ID = new Identifier(ServerInitializer.MODID, "entity_physics_c2s");

    /**
     * Accepts the packet. Client physics information is mainly received.
     * @param context the packet context
     * @param buf the buffer containing the information
     */
    public static void accept(PacketContext context, PacketByteBuf buf) {
        PlayerEntity player = context.getPlayer();
        int id = buf.readInt();

        Vector3f position = PacketHelper.deserializeVector3f(buf);
        Vector3f linearVelocity = PacketHelper.deserializeVector3f(buf);
        Vector3f angularVelocity = PacketHelper.deserializeVector3f(buf);
        Quat4f orientation = PacketHelper.deserializeQuaternion(buf);

        context.getTaskQueue().execute(() -> {
            FlyableEntity entity;

            if (player != null) {
                entity = (FlyableEntity) player.world.getEntityById(id);

                if (entity != null) {
                    entity.getPhysics().setPosition(position);
                    entity.getPhysics().setLinearVelocity(linearVelocity);
                    entity.getPhysics().setAngularVelocity(angularVelocity);
                    entity.getPhysics().setOrientation(orientation);
                }
            }
        });
    }

    /**
     * Sends the packet to the server. Mainly just includes physics information. Also includes
     * camera rotation (yaw, pitch).
     * @param physics the {@link ClientPhysicsHandler} to send
     */
    public static void send(IPhysicsHandler physics) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeInt(physics.getEntity().getEntityId());

        /* Physics Vectors */
        PacketHelper.serializeVector3f(buf, physics.getPosition());
        PacketHelper.serializeVector3f(buf, physics.getLinearVelocity());
        PacketHelper.serializeVector3f(buf, physics.getAngularVelocity());
        PacketHelper.serializeQuaternion(buf, physics.getOrientation());

        ClientSidePacketRegistry.INSTANCE.sendToServer(PACKET_ID, buf);
    }

    /**
     * Registers the packet in {@link ServerInitializer}.
     */
    public static void register() {
        ServerSidePacketRegistry.INSTANCE.register(PACKET_ID, EntityPhysicsC2S::accept);
    }
}
