package dev.lazurite.fpvracing.network.packet;

import dev.lazurite.fpvracing.network.tracker.GenericDataTrackerRegistry;
import dev.lazurite.fpvracing.server.ServerInitializer;
import dev.lazurite.fpvracing.server.entity.FlyableEntity;
import dev.lazurite.fpvracing.server.item.QuadcopterItem;
import dev.lazurite.fpvracing.server.item.TransmitterItem;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
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
                    List<FlyableEntity> entities = FlyableEntity.getList(player, FlyableEntity.class, 500);

                    for (FlyableEntity entity : entities) {
                        if (entity.getValue(FlyableEntity.BIND_ID).equals(bind.getKey().getType().fromTag(tag, bind.getKey().getName()))) {
                            entity.noClip = !entity.noClip;

                            if (entity.noClip) {
                                player.sendMessage(new TranslatableText("message.fpvracing.noclip_on"), false);
                            } else {
                                player.sendMessage(new TranslatableText("message.fpvracing.noclip_off"), false);
                            }

                            break;
                        }
                    }
                }
            } else if (hand.getItem() instanceof QuadcopterItem) {
                boolean noclip = noClip.getKey().getType().fromTag(tag, noClip.getKey().getName());
                noClip.getKey().getType().toTag(tag, noClip.getKey().getName(), !noclip);
                if (noclip) {
                    player.sendMessage(new TranslatableText("message.fpvracing.noclip_off"), false);
                } else {
                    player.sendMessage(new TranslatableText("message.fpvracing.noclip_on"), false);
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
