package dev.lazurite.fpvracing.network.packet;

import dev.lazurite.fpvracing.client.ClientInitializer;
import dev.lazurite.fpvracing.network.tracker.ConfigFile;
import dev.lazurite.fpvracing.server.Commands;
import dev.lazurite.fpvracing.server.ServerInitializer;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class ConfigCommandS2C {
    public static final Identifier PACKET_ID = new Identifier(ServerInitializer.MODID, "config_command_s2c");

    public static void accept(PacketContext context, PacketByteBuf buf) {
        String cmd = buf.readString(32767);

        context.getTaskQueue().execute(() -> {
            if (cmd.equals(Commands.WRITE)) {
                ConfigFile.writeConfig(ClientInitializer.getConfig(), ClientInitializer.CONFIG_NAME);
            } else if (cmd.equals(Commands.REVERT)) {
                ClientInitializer.config = ConfigFile.readConfig(ClientInitializer.CONFIG_NAME);
            }
        });
    }

    public static void send(PlayerEntity player, String key) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeString(key);
        ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, PACKET_ID, buf);
    }

    public static void register() {
        ClientSidePacketRegistry.INSTANCE.register(PACKET_ID, ConfigCommandS2C::accept);
    }
}
