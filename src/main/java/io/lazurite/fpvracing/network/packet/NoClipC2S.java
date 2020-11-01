package io.lazurite.fpvracing.network.packet;

import io.lazurite.fpvracing.network.tracker.GenericDataTrackerRegistry;
import io.lazurite.fpvracing.server.ServerInitializer;
import io.lazurite.fpvracing.server.entity.FlyableEntity;
import io.lazurite.fpvracing.server.item.QuadcopterItem;
import io.lazurite.fpvracing.server.item.TransmitterItem;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;

import java.util.List;

public class NoClipC2S {
    public static final Identifier PACKET_ID = new Identifier(ServerInitializer.MODID, "noclip_c2s");

    public static void accept(PacketContext context, PacketByteBuf buf) {
        PlayerEntity player = context.getPlayer();
        ItemStack hand = player.getMainHandStack();

        context.getTaskQueue().execute(() -> {
            GenericDataTrackerRegistry.Entry<Boolean> noClip = FlyableEntity.NO_CLIP;
            GenericDataTrackerRegistry.Entry<Integer> bind = FlyableEntity.BIND_ID;
            CompoundTag tag = hand.getOrCreateSubTag(ServerInitializer.MODID);

            if (hand.getItem() instanceof TransmitterItem) {
                if (bind.getKey().getType().fromTag(tag, bind.getKey().getName()) != null) {
                    List<FlyableEntity> entities = FlyableEntity.getList(player, 500);

                    for (FlyableEntity entity : entities) {
                        if (entity.getValue(FlyableEntity.BIND_ID).equals(bind.getKey().getType().fromTag(tag, bind.getKey().getName()))) {
                            entity.noClip = !entity.noClip;

                            if (entity.noClip) player.sendMessage(new LiteralText("No Clip Enabled"), false);
                            else player.sendMessage(new LiteralText("No Clip Disabled"), false);

                            break;
                        }
                    }
                }
            } else if (hand.getItem() instanceof QuadcopterItem) {
                boolean noclip = noClip.getKey().getType().fromTag(tag, noClip.getKey().getName());
                noClip.getKey().getType().toTag(tag, noClip.getKey().getName(), !noclip);
                if (noclip) {
                    player.sendMessage(new LiteralText("No Clip Disabled"), false);
                } else {
                    player.sendMessage(new LiteralText("No Clip Enabled"), false);
                }
            }
        });
    }

    public static void send() {
        ClientSidePacketRegistry.INSTANCE.sendToServer(PACKET_ID, new PacketByteBuf(Unpooled.buffer()));
    }

    public static void register() {
        ServerSidePacketRegistry.INSTANCE.register(PACKET_ID, NoClipC2S::accept);
    }
}
