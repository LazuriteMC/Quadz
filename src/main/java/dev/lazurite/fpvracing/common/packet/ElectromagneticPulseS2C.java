package dev.lazurite.fpvracing.common.packet;

import dev.lazurite.fpvracing.FPVRacing;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public class ElectromagneticPulseS2C {
    public static final Identifier PACKET_ID = new Identifier(FPVRacing.MODID, "electromagnetic_pulse_s2c");

    public static void accept(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {

    }

    public static void send(Vec3d origin) {
        // TODO make emp go boom
    }

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(PACKET_ID, ElectromagneticPulseS2C::accept);
    }
}
