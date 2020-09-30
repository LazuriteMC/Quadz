package bluevista.fpvracing.network.keybinds;

import bluevista.fpvracing.server.ServerInitializer;
import bluevista.fpvracing.server.ServerTick;
import bluevista.fpvracing.server.entities.DroneEntity;
import bluevista.fpvracing.server.items.GogglesItem;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.List;

public class PowerGogglesC2S {
    public static final Identifier PACKET_ID = new Identifier(ServerInitializer.MODID, "power_goggles_c2s");

    public static void accept(PacketContext context, PacketByteBuf buf) {
        PlayerEntity player = context.getPlayer();
        ItemStack hat = player.inventory.armor.get(3);
        boolean on = buf.readBoolean();
        String[] keys = new String[] {
            buf.readString(32767),
            buf.readString(32767)
        };

        context.getTaskQueue().execute(() -> {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;

            ServerInitializer.SERVER_PLAYER_KEYS.remove(player.getUuid());
            ServerInitializer.SERVER_PLAYER_KEYS.put(player.getUuid(), keys);

            if (hat.getItem() instanceof GogglesItem) {
                if (on && !ServerTick.isInGoggles(serverPlayer)) {
                    List<Entity> drones = DroneEntity.getDrones(serverPlayer); // list of all drones

                    for (Entity entity : drones) { // For every drone...
                        DroneEntity drone = (DroneEntity) entity;

                        // Make sure drone is within range of player...
                        if (serverPlayer.distanceTo(drone) > DroneEntity.TRACKING_RANGE) {
                            continue;

                            // Otherwise, see if the drone is on the same channel as the goggles..lie
                        } else if (GogglesItem.isOnSameChannel(drone, serverPlayer)) {
                            GogglesItem.setOn(hat, true);
                            ServerTick.setView(serverPlayer, drone);
                        }
                    }
                } else {
                    GogglesItem.setOn(hat, false);
                    ServerTick.resetView(serverPlayer);
                }
            }
        });
    }

    public static void send(boolean on, String[] keys) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeBoolean(on);

        for (String key : keys) {
            buf.writeString(key);
        }

        ClientSidePacketRegistry.INSTANCE.sendToServer(PACKET_ID, buf);
    }

    public static void register() {
        ServerSidePacketRegistry.INSTANCE.register(PACKET_ID, PowerGogglesC2S::accept);
    }
}
