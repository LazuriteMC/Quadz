package bluevista.fpvracing.network.packets;

import bluevista.fpvracing.network.datatracker.FlyableTrackerRegistry;
import bluevista.fpvracing.server.ServerInitializer;
import bluevista.fpvracing.server.entities.FlyableEntity;
import bluevista.fpvracing.server.items.QuadcopterItem;
import bluevista.fpvracing.server.items.TransmitterItem;
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
            FlyableTrackerRegistry.Entry<Boolean> noClip = FlyableTrackerRegistry.NO_CLIP;
            FlyableTrackerRegistry.Entry<Integer> bind = FlyableTrackerRegistry.BIND_ID;
            CompoundTag tag = hand.getOrCreateSubTag(ServerInitializer.MODID);

            if (hand.getItem() instanceof TransmitterItem) {
                if (bind.getDataType().fromTag(tag, bind.getName()) != null) {
                    List<FlyableEntity> entities = FlyableEntity.getList(player, 500);

                    for (FlyableEntity entity : entities) {
                        if (entity.getValue(FlyableTrackerRegistry.BIND_ID).equals(bind.getDataType().fromTag(tag, bind.getName()))) {
                            entity.noClip = !entity.noClip;

                            if (entity.noClip) player.sendMessage(new LiteralText("No Clip Enabled"), false);
                            else player.sendMessage(new LiteralText("No Clip Disabled"), false);

                            break;
                        }
                    }
                }
            } else if (hand.getItem() instanceof QuadcopterItem) {
                boolean noclip = noClip.getDataType().fromTag(tag, noClip.getName());
                noClip.getDataType().toTag(tag, noClip.getName(), !noclip);
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
