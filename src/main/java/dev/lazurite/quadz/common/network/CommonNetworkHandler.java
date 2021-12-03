package dev.lazurite.quadz.common.network;

import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.client.input.Mode;
import dev.lazurite.quadz.common.data.DataDriver;
import dev.lazurite.quadz.common.data.model.Template;
import dev.lazurite.quadz.common.state.Bindable;
import dev.lazurite.quadz.common.state.entity.QuadcopterEntity;
import dev.lazurite.quadz.common.util.PlayerData;
import dev.lazurite.quadz.common.util.input.InputFrame;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class CommonNetworkHandler {
    public static void onQuadcopterSettingsReceived(MinecraftServer server, ServerPlayer player, ServerGamePacketListener listener, FriendlyByteBuf buf, PacketSender sender) {
        int entityId = buf.readInt();
        int cameraAngle = buf.readInt();

        server.execute(() -> {
            Entity entity = player.getLevel().getEntity(entityId);

            if (entity instanceof QuadcopterEntity) {
                ((QuadcopterEntity) entity).setCameraAngle(cameraAngle);
            }
        });
    }

    public static void onPlayerDataReceived(MinecraftServer server, ServerPlayer player, ServerGamePacketListener listener, FriendlyByteBuf buf, PacketSender sender) {
        String callSign = buf.readUtf(32767);
        server.execute(() -> ((PlayerData) player).setCallSign(callSign));
    }

    public static void onTemplateReceived(MinecraftServer server, ServerPlayer player, ServerGamePacketListener listener, FriendlyByteBuf buf, PacketSender sender) {
        Template template = Template.deserialize(buf);

        server.execute(() -> {
            PlayerLookup.all(server).forEach(p -> {
                if (!p.equals(player)) {
                    ServerPlayNetworking.send(p, Quadz.TEMPLATE, template.serialize());
                }
            });

            DataDriver.load(template);
        });
    }

    public static void onInputFrame(MinecraftServer server, ServerPlayer player, ServerGamePacketListener listener, FriendlyByteBuf buf, PacketSender sender) {
        int entityId = buf.readInt();
        InputFrame frame = new InputFrame(
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readEnum(Mode.class));

        server.execute(() -> {
            Bindable.get(player.getMainHandItem()).ifPresent(transmitter -> {
                Entity entity = player.getLevel().getEntity(entityId);

                if (entity instanceof QuadcopterEntity) {
                    if (((QuadcopterEntity) entity).isBoundTo(transmitter)) {
                        ((QuadcopterEntity) entity).getInputFrame().set(frame);
                    }
                }
            });
        });
    }
}
