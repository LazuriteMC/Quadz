package bluevista.fpvracingmod.client.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.network.PacketByteBuf;

@Environment(EnvType.CLIENT)
public class RemoveGogglesPacketHandler {
    public static void accept(PacketContext context, PacketByteBuf buffer) {
        // Get the BlockPos we put earlier in the IO thread
//			BlockPos pos = attachedData.readBlockPos();
        context.getTaskQueue().execute(() -> {
            // Execute on the main thread

            // ALWAYS validate that the information received is valid in a C2S packet!
//				if(packetContext.getPlayer().world.canSetBlock(pos)){
//					 Turn to diamond
//					packetContext.getPlayer().world.setBlockState(pos, Blocks.DIAMOND_BLOCK.getDefaultState());
        });
    }
}
