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
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

import java.util.List;

public class GodModeC2S {
    public static final Identifier PACKET_ID = new Identifier(ServerInitializer.MODID, "godmode_c2s");

    public static void accept(PacketContext context, PacketByteBuf buf) {
        PlayerEntity player = context.getPlayer();
        ItemStack hand = player.getMainHandStack();

        context.getTaskQueue().execute(() -> {
            String t;

            if (hand.getItem() instanceof TransmitterItem) {
                if (TransmitterItem.getTagValue(hand, Config.BIND) != null) {
                    List<Entity> entities = ((ServerWorld) player.getEntityWorld()).getEntitiesByType(ServerInitializer.QUADCOPTER_ENTITY, EntityPredicates.EXCEPT_SPECTATOR);
                    QuadcopterEntity quad = null;

                    for (Entity entity : entities) {
                        quad = (QuadcopterEntity) entity;

                        if (quad.getDataTracker().get(FlyableEntity.BIND_ID) == TransmitterItem.getTagValue(hand, Config.BIND).intValue()) {
                            break;
                        }
                    }

                    if (quad != null) {
                        quad.getDataTracker().set(FlyableEntity.GOD_MODE, !quad.getDataTracker().get(FlyableEntity.GOD_MODE));

                        if (quad.getDataTracker().get(FlyableEntity.GOD_MODE)) {
                            t = "God Mode Enabled";
                        } else {
                            t = "God Mode Disabled";
                        }

                        player.sendMessage(new TranslatableText(t), false);
                    }
                }
            } else if (hand.getItem() instanceof QuadcopterItem) {
                QuadcopterItem.setTagValue(hand, Config.GOD_MODE, QuadcopterItem.getTagValue(hand, Config.GOD_MODE) != null && QuadcopterItem.getTagValue(hand, Config.GOD_MODE).intValue() == 0 ? 1 : 0);

                if (QuadcopterItem.getTagValue(hand, Config.GOD_MODE) != null && QuadcopterItem.getTagValue(hand, Config.GOD_MODE).intValue() == 1) {
                    t = "God Mode Enabled";
                } else {
                    t = "God Mode Disabled";
                }

                player.sendMessage(new TranslatableText(t), false);
            }
        });
    }

    public static void send() {
        ClientSidePacketRegistry.INSTANCE.sendToServer(PACKET_ID, new PacketByteBuf(Unpooled.buffer()));
    }

    public static void register() {
        ServerSidePacketRegistry.INSTANCE.register(PACKET_ID, GodModeC2S::accept);
    }
}
