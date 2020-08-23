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

        int droneID = buf.readInt();
        float throttle = buf.readFloat();
        Quat4f orientation = PacketHelper.deserializeQuaternion(buf);
        Vector3f position = PacketHelper.deserializeVector3f(buf);
        Vector3f linearVel = PacketHelper.deserializeVector3f(buf);
        Vector3f angularVel = PacketHelper.deserializeVector3f(buf);

        context.getTaskQueue().execute(() -> {
            DroneEntity drone = null;

            if(player != null)
                drone = (DroneEntity) player.world.getEntityById(droneID);

            if(drone != null) {
                drone.setThrottle(throttle);
                drone.setOrientation(orientation);
                drone.setPos(position.x, position.y, position.z);
                drone.physics.setPosition(position);
                drone.physics.getRigidBody().setLinearVelocity(linearVel);
                drone.physics.getRigidBody().setAngularVelocity(angularVel);
            }
        });
    }

    public static void send(DroneEntity drone) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());

        buf.writeInt(drone.getEntityId());
        buf.writeFloat(drone.getThrottle());
        PacketHelper.serializeQuaternion(buf, drone.getOrientation());
        PacketHelper.serializeVector3f(buf, drone.physics.getPosition());
        PacketHelper.serializeVector3f(buf, drone.physics.getRigidBody().getLinearVelocity(new Vector3f()));
        PacketHelper.serializeVector3f(buf, drone.physics.getRigidBody().getAngularVelocity(new Vector3f()));

        ClientSidePacketRegistry.INSTANCE.sendToServer(PACKET_ID, buf);
    }

    public static void register() {
        ServerSidePacketRegistry.INSTANCE.register(PACKET_ID, DroneEntityC2S::accept);
    }
}
