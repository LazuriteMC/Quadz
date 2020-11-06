package dev.lazurite.fpvracing.network.packet;

import dev.lazurite.fpvracing.network.tracker.GenericDataTrackerRegistry;
import dev.lazurite.fpvracing.server.ServerInitializer;
import dev.lazurite.fpvracing.server.entity.FlyableEntity;
import dev.lazurite.fpvracing.server.entity.flyable.QuadcopterEntity;
import dev.lazurite.fpvracing.server.item.QuadcopterItem;
import dev.lazurite.fpvracing.server.item.TransmitterItem;
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
            GenericDataTrackerRegistry.Entry<Integer> bind = FlyableEntity.BIND_ID;
            GenericDataTrackerRegistry.Entry<Boolean> god = FlyableEntity.GOD_MODE;

            if (hand.getItem() instanceof TransmitterItem) {
                if (bind.getKey().getType().fromTag(tag, bind.getKey().getName()) != null) {
                    List<Entity> entities = ((ServerWorld) player.getEntityWorld()).getEntitiesByType(ServerInitializer.QUADCOPTER_ENTITY, EntityPredicates.EXCEPT_SPECTATOR);

                    for (Entity entity : entities) {
                        QuadcopterEntity quad = (QuadcopterEntity) entity;

                        if (quad.getValue(bind).equals(bind.getKey().getType().fromTag(tag, bind.getKey().getName()))) {
                            quad.setValue(god, !quad.getValue(FlyableEntity.GOD_MODE));

                            if (quad.getValue(FlyableEntity.GOD_MODE)) {
                                player.sendMessage(new LiteralText("God Mode Enabled"), false);
                            } else {
                                player.sendMessage(new LiteralText("God Mode Disabled"), false);
                            }

                            break;
                        }
                    }
                }
            } else if (hand.getItem() instanceof QuadcopterItem) {
                boolean godmode = god.getKey().getType().fromTag(tag, god.getKey().getName());
                god.getKey().getType().toTag(tag, god.getKey().getName(), !godmode);
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
