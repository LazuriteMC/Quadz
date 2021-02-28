package dev.lazurite.quadz.client.input.keybind.net;

import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.common.entity.QuadcopterEntity;
import dev.lazurite.quadz.common.item.container.TransmitterContainer;
import dev.lazurite.quadz.common.util.type.QuadcopterState;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Optional;

public class ChangeCameraAngleC2S {
    public static final Identifier PACKET_ID = new Identifier(Quadz.MODID, "change_camera_angle_c2s");

    public static void accept(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        int amount = buf.readInt();

        server.execute(() -> {
            if (player.getCameraEntity() instanceof QuadcopterEntity) {
                Optional<TransmitterContainer> transmitter = Quadz.TRANSMITTER_CONTAINER.maybeGet(player.getMainHandStack());

                if (transmitter.isPresent()) {
                    if (player.getCameraEntity() instanceof QuadcopterEntity) {
                        if (((QuadcopterEntity) player.getCameraEntity()).getBindId() == transmitter.get().getBindId()) {
                            ((QuadcopterEntity) player.getCameraEntity()).setCameraAngle(((QuadcopterEntity) player.getCameraEntity()).getCameraAngle() + amount);
                        }
                    } else {
                        QuadcopterEntity quadcopter = QuadcopterState.findQuadcopter(player.getEntityWorld(), player.getPos(), transmitter.get().getBindId(), 100);
                        quadcopter.setCameraAngle(quadcopter.getCameraAngle() + amount);
                    }
                }
            }
        });
    }

    public static void send(int amount) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(amount);
        ClientPlayNetworking.send(PACKET_ID, buf);
    }
}
