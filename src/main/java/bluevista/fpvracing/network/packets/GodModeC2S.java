package bluevista.fpvracing.network.packets;

import bluevista.fpvracing.network.datatracker.FlyableTrackerRegistry;
import bluevista.fpvracing.server.ServerInitializer;
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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;

import java.util.List;

public class GodModeC2S {
    public static final Identifier PACKET_ID = new Identifier(ServerInitializer.MODID, "godmode_c2s");

    public static void accept(PacketContext context, PacketByteBuf buf) {
        PlayerEntity player = context.getPlayer();
        ItemStack hand = player.getMainHandStack();

        context.getTaskQueue().execute(() -> {
            CompoundTag tag = hand.getOrCreateSubTag(ServerInitializer.MODID);
            FlyableTrackerRegistry.Entry<Integer> bind = FlyableTrackerRegistry.BIND_ID;
            FlyableTrackerRegistry.Entry<Boolean> god = FlyableTrackerRegistry.GOD_MODE;

            if (hand.getItem() instanceof TransmitterItem) {
                if (bind.getDataType().fromTag(tag, bind.getName()) != null) {
                    List<Entity> entities = ((ServerWorld) player.getEntityWorld()).getEntitiesByType(ServerInitializer.QUADCOPTER_ENTITY, EntityPredicates.EXCEPT_SPECTATOR);

                    for (Entity entity : entities) {
                        QuadcopterEntity quad = (QuadcopterEntity) entity;

                        if (quad.getValue(bind).equals(bind.getDataType().fromTag(tag, bind.getName()))) {
                            quad.setValue(god, !quad.getValue(FlyableTrackerRegistry.GOD_MODE));

                            if (quad.getValue(FlyableTrackerRegistry.GOD_MODE)) {
                                player.sendMessage(new LiteralText("God Mode Enabled"), false);
                            } else {
                                player.sendMessage(new LiteralText("God Mode Disabled"), false);
                            }

                            break;
                        }
                    }
                }
            } else if (hand.getItem() instanceof QuadcopterItem) {
                boolean godmode = god.getDataType().fromTag(tag, god.getName());
                god.getDataType().toTag(tag, god.getName(), !godmode);
                if (godmode) {
                    player.sendMessage(new LiteralText("God Mode Disabled"), false);
                } else {
                    player.sendMessage(new LiteralText("God Mode Enabled"), false);
                }
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
