package dev.lazurite.quadz.client.input.keybind.net;

import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.common.entity.QuadcopterEntity;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;

public class ElectromagneticPulseC2S {
    public static final Identifier PACKET_ID = new Identifier(Quadz.MODID, "electromagnetic_pulse_c2s");

    public static void accept(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        int radius = buf.readInt();
        server.execute(() -> player.getEntityWorld().getEntitiesByClass(QuadcopterEntity.class, new Box(player.getBlockPos()).expand(radius), null).forEach(QuadcopterEntity::kill));
    }

    public static void send(int radius) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(radius);
        ClientPlayNetworking.send(PACKET_ID, buf);
    }
}
