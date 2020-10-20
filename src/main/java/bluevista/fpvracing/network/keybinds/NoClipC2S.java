package bluevista.fpvracing.network.keybinds;

import bluevista.fpvracing.config.Config;
import bluevista.fpvracing.server.ServerInitializer;
import bluevista.fpvracing.server.entities.FlyableEntity;
import bluevista.fpvracing.server.entities.QuadcopterEntity;
import bluevista.fpvracing.server.items.QuadcopterItem;
import bluevista.fpvracing.server.items.TransmitterItem;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

import java.util.List;

public class NoClipC2S {
    public static final Identifier PACKET_ID = new Identifier(ServerInitializer.MODID, "noclip_c2s");

    public static void accept(PacketContext context, PacketByteBuf buf) {
        PlayerEntity player = context.getPlayer();
        ItemStack hand = player.getMainHandStack();

        context.getTaskQueue().execute(() -> {
            String t;

            if (hand.getItem() instanceof TransmitterItem) {
                if (TransmitterItem.getTagValue(hand, Config.BIND) != null) {
                    List<FlyableEntity> entities = FlyableEntity.getList(player, 500);
                    FlyableEntity flyable = null;

                    for (FlyableEntity entity : entities) {
                        if (entity.getDataTracker().get(FlyableEntity.BIND_ID).equals(TransmitterItem.getTagValue(hand, Config.BIND))) {
                            flyable = entity;
                            break;
                        }
                    }

                    if (flyable != null) {
                        flyable.noClip = !flyable.noClip;

                        if (flyable.noClip) {
                            t = "No Clip Enabled";
                        } else {
                            t = "No Clip Disabled";
                        }

                        player.sendMessage(new TranslatableText(t), false);
                    }
                }
            } else if (hand.getItem() instanceof QuadcopterItem) {
                QuadcopterItem.setTagValue(hand, Config.NO_CLIP, QuadcopterItem.getTagValue(hand, Config.NO_CLIP) != null && QuadcopterItem.getTagValue(hand, Config.NO_CLIP).intValue() == 0 ? 1 : 0);

                if (QuadcopterItem.getTagValue(hand, Config.NO_CLIP) != null && QuadcopterItem.getTagValue(hand, Config.NO_CLIP).intValue() == 1) {
                    t = "No Clip Enabled";
                } else {
                    t = "No Clip Disabled";
                }

                player.sendMessage(new TranslatableText(t), false);
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
