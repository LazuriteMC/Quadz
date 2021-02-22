package dev.lazurite.fpvracing.client.input.keybind.net;

import dev.lazurite.fpvracing.FPVRacing;
import dev.lazurite.fpvracing.common.entity.QuadcopterEntity;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class ChangeCameraAngleC2S {
    public static final Identifier PACKET_ID = new Identifier(FPVRacing.MODID, "change_camera_angle_c2s");

    public static void accept(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        int amount = buf.readInt();

        server.execute(() -> {
            if (player.getCameraEntity() instanceof QuadcopterEntity) {
                QuadcopterEntity quadcopter = (QuadcopterEntity) player.getCameraEntity();
                quadcopter.setCameraAngle(quadcopter.getCameraAngle() + amount);
            }
        });
    }

    public static void send(int amount) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(amount);
        ClientPlayNetworking.send(PACKET_ID, buf);
    }
}
