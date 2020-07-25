package bluevista.fpvracingmod.network;

import bluevista.fpvracingmod.server.ServerInitializer;
import bluevista.fpvracingmod.server.items.GogglesItem;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class PowerGogglesC2S {
    public static final Identifier PACKET_ID = new Identifier(ServerInitializer.MODID, "power_goggles_c2s");

    public static void accept(PacketContext context, PacketByteBuf buf) {
        PlayerEntity player = context.getPlayer();
        ItemStack hat = player.inventory.armor.get(3);
        boolean on = buf.readBoolean();

        context.getTaskQueue().execute(() -> {
            if(hat.getItem() instanceof GogglesItem) {
                System.out.println("Before: " + GogglesItem.isOn(player));
                GogglesItem.setOn(hat, on, player);
                System.out.println("After: " + GogglesItem.isOn(player));
            }
        });
    }

    public static void send(boolean on) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeBoolean(on);
        ClientSidePacketRegistry.INSTANCE.sendToServer(PACKET_ID, buf);
    }

    public static void register() {
        ServerSidePacketRegistry.INSTANCE.register(PACKET_ID, PowerGogglesC2S::accept);
    }
}
