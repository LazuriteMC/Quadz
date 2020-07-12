package bluevista.fpvracingmod.client.network;

import bluevista.fpvracingmod.server.ServerInitializer;
import bluevista.fpvracingmod.server.entities.DroneEntity;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.UUID;

@Environment(EnvType.CLIENT)
public class InputPacketHandler {
    public static final Identifier INPUT_PACKET_ID = new Identifier(ServerInitializer.MODID, "input_packet");

    public static void accept(PacketContext context, PacketByteBuf buffer) {
        float throttle = buffer.readFloat();
        UUID droneID = buffer.readUuid();

        context.getTaskQueue().execute(() -> {
            DroneEntity drone = null;
            if(context.getPlayer() != null) drone = DroneEntity.getByUuid(context.getPlayer(), droneID);
            if(drone != null) drone.setThrottle(throttle);
        });
    }

    public static void send(float throttle, DroneEntity drone) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeFloat(throttle);
        buf.writeUuid(drone.getUuid());
        ClientSidePacketRegistry.INSTANCE.sendToServer(INPUT_PACKET_ID, buf);
    }

    public static void register() {
        ServerSidePacketRegistry.INSTANCE.register(INPUT_PACKET_ID, InputPacketHandler::accept);
    }
}
