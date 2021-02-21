package dev.lazurite.fpvracing.common.packet;

import dev.lazurite.fpvracing.FPVRacing;
import dev.lazurite.fpvracing.client.config.Config;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class ShouldRenderPlayerS2C {
    public static final Identifier PACKET_ID = new Identifier(FPVRacing.MODID, "should_render_player_s2c");

    public static void accept(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        boolean shouldRenderPlayer = buf.readBoolean();
        client.execute(() -> Config.getInstance().shouldRenderPlayer = shouldRenderPlayer);
    }

    public static void send(ServerPlayerEntity player, boolean shouldRenderPlayer) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(shouldRenderPlayer);
        ServerPlayNetworking.send(player, PACKET_ID, buf);
    }
}
