package dev.lazurite.quadz.common.network;

import dev.lazurite.quadz.common.item.GogglesItem;
import dev.lazurite.quadz.common.state.Bindable;
import dev.lazurite.quadz.common.state.QuadcopterState;
import dev.lazurite.quadz.common.state.entity.QuadcopterEntity;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;

public class KeybindNetworkHandler {
    public static void onNoClipKey(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        server.execute(() ->
            Bindable.get(player.getMainHandStack()).flatMap(transmitter -> QuadcopterState.getQuadcopterByBindId(player.getEntityWorld(), player.getCameraEntity().getPos(), transmitter.getBindId(), server.getPlayerManager().getViewDistance())).ifPresent(quadcopter -> {
                boolean lastNoClip = quadcopter.getRigidBody().shouldDoTerrainLoading();
                quadcopter.getRigidBody().setDoTerrainLoading(!lastNoClip);

                if (lastNoClip) {
                    player.sendMessage(new TranslatableText("message.quadz.noclip_on"), true);
                } else {
                    player.sendMessage(new TranslatableText("message.quadz.noclip_off"), true);
                }
            })
        );
    }

    public static void onChangeCameraAngleKey(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        int amount = buf.readInt();

        server.execute(() ->
            Bindable.get(player.getMainHandStack()).ifPresent(transmitter -> {
                if (player.getCameraEntity() instanceof QuadcopterEntity) {
                    QuadcopterEntity quadcopter = (QuadcopterEntity) player.getCameraEntity();

                    if (quadcopter.isBoundTo(transmitter)) {
                        quadcopter.setCameraAngle(quadcopter.getCameraAngle() + amount);
                    }
                } else {
                    QuadcopterState.getQuadcopterByBindId(player.getEntityWorld(), player.getCameraEntity().getPos(), transmitter.getBindId(), server.getPlayerManager().getViewDistance())
                            .ifPresent(quadcopter -> quadcopter.setCameraAngle(quadcopter.getCameraAngle() + amount));
                }
            })
        );
    }

    public static void onPowerGogglesKey(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        boolean enable = buf.readBoolean();

        server.execute(() -> {
            ItemStack hat = player.inventory.armor.get(3);

            if (hat.getItem() instanceof GogglesItem) {
                hat.getOrCreateTag().putBoolean("enabled", enable);
            }
        });
    }
}
