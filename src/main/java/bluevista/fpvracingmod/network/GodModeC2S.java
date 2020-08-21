package bluevista.fpvracingmod.network;

import bluevista.fpvracingmod.server.ServerInitializer;
import bluevista.fpvracingmod.server.entities.DroneEntity;
import bluevista.fpvracingmod.server.items.DroneSpawnerItem;
import bluevista.fpvracingmod.server.items.TransmitterItem;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

public class GodModeC2S {
    public static final Identifier PACKET_ID = new Identifier(ServerInitializer.MODID, "godmode_c2s");

    public static void accept(PacketContext context, PacketByteBuf buf) {
        PlayerEntity player = context.getPlayer();
        ItemStack hand = player.getMainHandStack();

        context.getTaskQueue().execute(() -> {
            if(hand.getItem() instanceof TransmitterItem) {
                if(hand.getSubTag("bind") != null) {
                    DroneEntity drone = DroneEntity.getByUuid(context.getPlayer(), hand.getSubTag("bind").getUuid("bind"));

                    if(drone != null) {
                        drone.godMode = !drone.godMode;

                        String t;
                        if(drone.godMode) t = "God Mode Enabled";
                        else t = "God Mode Disabled";

                        player.sendMessage(new TranslatableText(t), false);
                    }
                }
            } else if (hand.getItem() instanceof DroneSpawnerItem) {
                DroneSpawnerItem.setGodMode(hand, DroneSpawnerItem.getGodMode(hand) == 0 ? 1 : 0);

                String t;
                if(DroneSpawnerItem.getGodMode(hand) == 1) t = "God Mode Enabled";
                else t = "God Mode Disabled";

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
