package dev.lazurite.quadz.common.network;

import dev.lazurite.quadz.common.item.GogglesItem;
import dev.lazurite.quadz.common.state.Bindable;
import dev.lazurite.quadz.common.state.QuadcopterState;
import dev.lazurite.quadz.common.state.entity.QuadcopterEntity;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class KeybindNetworkHandler {
    public static void onNoClipKey(MinecraftServer server, ServerPlayer player, ServerGamePacketListener handler, FriendlyByteBuf buf, PacketSender sender) {
        server.execute(() ->
            Bindable.get(player.getMainHandItem()).flatMap(transmitter -> QuadcopterState.getQuadcopterByBindId(player.getLevel(), player.getCamera().position(), transmitter.getBindId(), server.getPlayerList().getViewDistance())).ifPresent(quadcopter -> {
                boolean lastNoClip = quadcopter.getRigidBody().shouldDoTerrainLoading();
                quadcopter.getRigidBody().setDoTerrainLoading(!lastNoClip);

                if (lastNoClip) {
                    player.sendMessage(new TranslatableComponent("message.quadz.noclip_on"), true);
                } else {
                    player.sendMessage(new TranslatableComponent("message.quadz.noclip_off"), true);
                }
            })
        );
    }

    public static void onChangeCameraAngleKey(MinecraftServer server, ServerPlayer player, ServerGamePacketListener handler, FriendlyByteBuf buf, PacketSender sender) {
        int amount = buf.readInt();

        server.execute(() ->
            Bindable.get(player.getMainHandItem()).ifPresent(transmitter -> {
                if (player.getCamera() instanceof QuadcopterEntity) {
                    QuadcopterEntity quadcopter = (QuadcopterEntity) player.getCamera();

                    if (quadcopter.isBoundTo(transmitter)) {
                        quadcopter.setCameraAngle(quadcopter.getCameraAngle() + amount);
                    }
                } else {
                    QuadcopterState.getQuadcopterByBindId(player.getLevel(), player.getCamera().position(), transmitter.getBindId(), server.getPlayerList().getViewDistance())
                            .ifPresent(quadcopter -> quadcopter.setCameraAngle(quadcopter.getCameraAngle() + amount));
                }
            })
        );
    }

    public static void onPowerGogglesKey(MinecraftServer server, ServerPlayer player, ServerGamePacketListener handler, FriendlyByteBuf buf, PacketSender sender) {
        boolean enable = buf.readBoolean();

        server.execute(() -> {
            ItemStack hat = player.getInventory().armor.get(3);

            if (hat.getItem() instanceof GogglesItem) {
                hat.getOrCreateTag().putBoolean("enabled", enable);
            }
        });
    }
}
