package bluevista.fpvracingmod.network.config;

import bluevista.fpvracingmod.client.ClientInitializer;
import bluevista.fpvracingmod.config.Config;
import bluevista.fpvracingmod.server.ServerInitializer;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class ConfigS2C {
    public static final Identifier PACKET_ID = new Identifier(ServerInitializer.MODID, "server_config_client_packet");

    public static void accept(PacketContext context, PacketByteBuf buf) {

        String key = buf.readString(32767); // majik
        Number value = null;

        if (Config.INT_KEYS.contains(key)) {
            value = buf.readInt();
        } else if (Config.FLOAT_KEYS.contains(key)) {
            value = buf.readFloat();
        }

        final Number finalValue = value; // janky fix to satisfy the lambda needing a final or effectively final value

        context.getTaskQueue().execute(() -> {

            if (finalValue != null) {
                ClientInitializer.getConfig().setOption(key, finalValue);
            } else if (key.equals(Config.WRITE)) {
                ClientInitializer.getConfig().writeConfig();
            } else if (key.equals(Config.REVERT)) {
                ClientInitializer.registerConfig();
                ConfigC2S.send(ClientInitializer.getConfig());
            }

        });

    }

    public static void send(PlayerEntity player, String key) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        Config serverPlayerConfig = ServerInitializer.serverPlayerConfigs.get(player.getUuid());

        buf.writeString(key);

        if (Config.INT_KEYS.contains(key)) {
            buf.writeInt(serverPlayerConfig.getIntOption(key));
        } else if (Config.FLOAT_KEYS.contains(key)) {
            buf.writeFloat(serverPlayerConfig.getFloatOption(key));
        }

        ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, PACKET_ID, buf);
    }

    public static void register() {
        ClientSidePacketRegistry.INSTANCE.register(PACKET_ID, ConfigS2C::accept);
    }
}
