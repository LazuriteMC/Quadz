package dev.lazurite.fpvracing.client.input.frame;

import dev.lazurite.fpvracing.FPVRacing;
import dev.lazurite.fpvracing.common.item.TransmitterItem;
import dev.lazurite.fpvracing.common.item.container.TransmitterContainer;
import dev.lazurite.fpvracing.common.util.type.Controllable;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class InputFrameC2S {
    public static final Identifier PACKET_ID = new Identifier(FPVRacing.MODID, "input_frame_c2s");

    public static void accept(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        int entityId = buf.readInt();
        InputFrame frame = new InputFrame(buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat());

        server.execute(() -> {
            if (player.getMainHandStack().getItem() instanceof TransmitterItem) {
                TransmitterContainer transmitter = FPVRacing.TRANSMITTER_CONTAINER.get(player.getMainHandStack());
                Entity entity = player.getEntityWorld().getEntityById(entityId);

                if (entity instanceof Controllable) {
                    if (((Controllable) entity).getBindId() == transmitter.getBindId()) {
                        ((Controllable) entity).setInputFrame(frame);
                    }
                }
            }
        });
    }

    public static void send(Entity entity, InputFrame frame) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(entity.getEntityId());
        buf.writeFloat(frame.getThrottle());
        buf.writeFloat(frame.getPitch());
        buf.writeFloat(frame.getYaw());
        buf.writeFloat(frame.getRoll());
        buf.writeFloat(frame.getRate());
        buf.writeFloat(frame.getSuperRate());
        buf.writeFloat(frame.getExpo());
        ClientPlayNetworking.send(PACKET_ID, buf);
    }
}
