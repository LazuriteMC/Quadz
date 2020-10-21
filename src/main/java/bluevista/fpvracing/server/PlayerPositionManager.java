package bluevista.fpvracing.server;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;

public class PlayerPositionManager {
    private final Map<PlayerEntity, Vec3d> playerPositions;

    public PlayerPositionManager() {
        this.playerPositions = new HashMap<>();
    }

    public void addPos(PlayerEntity player) {
        this.playerPositions.put(player, player.getPos());
    }

    public Vec3d getPos(PlayerEntity player) {
        return this.playerPositions.get(player);
    }

    public void resetPos(ServerPlayerEntity player) {
        Vec3d pos = this.playerPositions.get(player);

        /* Submit a chunk ticket */
        ServerWorld world = player.getServerWorld();
        ChunkPos chunkPos = new ChunkPos(new BlockPos(pos));
        world.getChunkManager().addTicket(ChunkTicketType.POST_TELEPORT, chunkPos, 1, player.getEntityId());

        /* Request a teleport */
        player.networkHandler.requestTeleport(pos.x, pos.y, pos.z, player.yaw, player.pitch);
    }
}