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
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public class CommonNetworkHandler {
    public static void onQuadcopterSettingsReceived(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        int entityId = buf.readInt();
        int cameraAngle = buf.readInt();

        server.execute(() -> {
            Entity entity = player.getEntityWorld().getEntityById(entityId);

            if (entity instanceof QuadcopterEntity) {
                ((QuadcopterEntity) entity).setCameraAngle(cameraAngle);
            }
        });
    }

    public static void onPlayerDataReceived(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        String callSign = buf.readString(32767);
        server.execute(() -> ((PlayerData) player).setCallSign(callSign));
    }

    public static void onTemplateReceived(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
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

    public static void onInputFrame(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
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
                buf.readEnumConstant(Mode.class));

        server.execute(() -> {
            Bindable.get(player.getMainHandStack()).ifPresent(transmitter -> {
                Entity entity = player.getEntityWorld().getEntityById(entityId);

                if (entity instanceof QuadcopterEntity) {
                    if (((QuadcopterEntity) entity).isBoundTo(transmitter)) {
                        ((QuadcopterEntity) entity).getInputFrame().set(frame);
                    }
                }
            });
        });
    }
}
