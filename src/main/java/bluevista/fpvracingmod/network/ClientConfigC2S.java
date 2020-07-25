package bluevista.fpvracingmod.network;

import bluevista.fpvracingmod.client.ClientInitializer;
import bluevista.fpvracingmod.server.ServerInitializer;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClientConfigC2S {
    public static final Identifier PACKET_ID = new Identifier(ServerInitializer.MODID, "client_config_c2s");

    public static void accept(PacketContext context, PacketByteBuf buf) {

        UUID uuid = context.getPlayer().getUuid();
        List<Integer> values = new ArrayList<>();
        values.add(buf.readInt());
        values.add(buf.readInt());
        values.add(buf.readInt());

        context.getTaskQueue().execute(() -> {
            ServerInitializer.serverPlayerConfig.put(uuid, values);
        });
    }

    public static void send() {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());

        buf.writeInt(Integer.parseInt(ClientInitializer.getConfig().getValue("cameraAngle")));
        buf.writeInt(Integer.parseInt(ClientInitializer.getConfig().getValue("band")));
        buf.writeInt(Integer.parseInt(ClientInitializer.getConfig().getValue("channel")));

        ClientSidePacketRegistry.INSTANCE.sendToServer(PACKET_ID, buf);
    }

    public static void register() {
        ServerSidePacketRegistry.INSTANCE.register(PACKET_ID, ClientConfigC2S::accept);
    }
}
