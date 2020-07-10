package bluevista.fpvracingmod.client.network;

import bluevista.fpvracingmod.server.ServerInitializer;
import bluevista.fpvracingmod.server.entities.DroneEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.UUID;

@Environment(EnvType.CLIENT)
public class InputPacketHandler {
    public static final Identifier INPUT_PACKET_ID = new Identifier(ServerInitializer.MODID, "input");

    public static void accept(PacketContext context, PacketByteBuf buffer) {
        float throttle = buffer.readFloat();
        UUID droneID = buffer.readUuid();

        context.getTaskQueue().execute(() -> {
            PlayerEntity player = context.getPlayer();
            DroneEntity drone = DroneEntity.getByUuid(player, droneID);
            if(drone != null) drone.setThrottle(throttle);
        });
    }

    public static void send(PacketByteBuf buf) {
        ClientSidePacketRegistry.INSTANCE.sendToServer(INPUT_PACKET_ID, buf);
    }

    public static void register() {
        ServerSidePacketRegistry.INSTANCE.register(INPUT_PACKET_ID, InputPacketHandler::accept);
    }
}
