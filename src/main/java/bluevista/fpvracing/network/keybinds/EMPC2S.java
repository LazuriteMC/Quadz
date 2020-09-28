package bluevista.fpvracing.network.keybinds;

import bluevista.fpvracing.server.ServerInitializer;
import bluevista.fpvracing.server.ServerTick;
import bluevista.fpvracing.server.entities.DroneEntity;
import bluevista.fpvracing.server.items.TransmitterItem;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

public class EMPC2S {
    public static final Identifier PACKET_ID = new Identifier(ServerInitializer.MODID, "emp_c2s");

    public static void accept(PacketContext context, PacketByteBuf buf) {
        PlayerEntity playerEntity = context.getPlayer();

        context.getTaskQueue().execute(() -> {
            ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity) playerEntity;
            ItemStack itemStack = serverPlayerEntity.getMainHandStack();

            if (itemStack.getItem() instanceof TransmitterItem) {
                DroneEntity drone = TransmitterItem.droneFromTransmitter(itemStack, serverPlayerEntity);

                if (drone != null) {
                    drone.kill();
                    serverPlayerEntity.getServerWorld().removeEntity(drone);
                    ServerTick.resetView(serverPlayerEntity);
                    serverPlayerEntity.sendMessage(new TranslatableText("Destroyed drone"), false);
                }
            }
        });
    }

    public static void send() {
        ClientSidePacketRegistry.INSTANCE.sendToServer(PACKET_ID, new PacketByteBuf(Unpooled.buffer()));
    }

    public static void register() {
        ServerSidePacketRegistry.INSTANCE.register(PACKET_ID, EMPC2S::accept);
    }
}
