package bluevista.fpvracing.network.keybinds;

import bluevista.fpvracing.config.Config;
import bluevista.fpvracing.server.ServerInitializer;
import bluevista.fpvracing.server.entities.DroneEntity;
import bluevista.fpvracing.server.items.DroneSpawnerItem;
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
                    List<Entity> entities = ((ServerWorld) player.getEntityWorld()).getEntitiesByType(ServerInitializer.DRONE_ENTITY, EntityPredicates.EXCEPT_SPECTATOR);
                    DroneEntity drone = null;

                    for (Entity entity : entities) {
                        drone = (DroneEntity) entity;

                        if (drone.getConfigValues(Config.BIND).intValue() == TransmitterItem.getTagValue(hand, Config.BIND).intValue()) {
                            break;
                        }
                    }

                    if (drone != null) {
                        drone.setConfigValues(Config.GOD_MODE, drone.getConfigValues(Config.GOD_MODE).intValue() == 1 ? 0 : 1);

                        if (drone.getConfigValues(Config.GOD_MODE).intValue() == 1) {
                            t = "God Mode Enabled";
                        } else {
                            t = "God Mode Disabled";
                        }

                        player.sendMessage(new TranslatableText(t), false);
                    }
                }
            } else if (hand.getItem() instanceof DroneSpawnerItem) {
                DroneSpawnerItem.setTagValue(hand, Config.GOD_MODE, DroneSpawnerItem.getTagValue(hand, Config.GOD_MODE) != null && DroneSpawnerItem.getTagValue(hand, Config.GOD_MODE).intValue() == 0 ? 1 : 0);

                if (DroneSpawnerItem.getTagValue(hand, Config.GOD_MODE) != null && DroneSpawnerItem.getTagValue(hand, Config.GOD_MODE).intValue() == 1) {
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
