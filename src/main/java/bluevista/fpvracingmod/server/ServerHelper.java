package bluevista.fpvracingmod.server;

import bluevista.fpvracingmod.server.entities.DroneEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

public class ServerHelper {
    public static boolean isInGoggles(ServerPlayerEntity player) {
        return player.getCameraEntity() instanceof DroneEntity;
    }

    public static void movePlayer(double x, double y, double z, ServerPlayerEntity player) {
        ServerWorld world = player.getServerWorld();
        ChunkPos chunkPos = new ChunkPos(new BlockPos(x, y, z));
        world.getChunkManager().addTicket(ChunkTicketType.field_19347, chunkPos, 1, player.getEntityId());
        player.networkHandler.requestTeleport(x, y, z, player.yaw, player.pitch);
    }
}
