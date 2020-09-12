package bluevista.fpvracingmod.server;

import bluevista.fpvracingmod.server.entities.DroneEntity;
import bluevista.fpvracingmod.server.items.GogglesItem;
import bluevista.fpvracingmod.server.items.TransmitterItem;
import net.fabricmc.fabric.api.event.server.ServerTickCallback;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class ServerTick {
    public static void tick(MinecraftServer server) {
        List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();

        for (ServerPlayerEntity player : players) { // for every player in the server
            if (GogglesItem.isWearingGoggles(player) && !isInGoggles(player)) {
                List<Entity> drones = DroneEntity.getNearbyDrones(player);

                for (Entity entity : drones) { // for every drone in range of given player
                    DroneEntity drone = (DroneEntity) entity;

                    if (GogglesItem.isOnSameChannel(drone, player)) {
                        setView(player, drone);
                    }
                }
            }

            if (isInGoggles(player)) {
                DroneEntity drone = (DroneEntity) player.getCameraEntity();
                Vec3d pos = drone.getPlayerStartPos().get(player);

                if (Math.sqrt(drone.squaredDistanceTo(pos)) < DroneEntity.NEAR_TRACKING_RANGE && !player.getPos().equals(pos)) {
                    /* If the drone is in range of where the player started, teleport the player there. */
                    player.requestTeleport(pos.x, pos.y, pos.z);

                } else if (player.distanceTo(drone) > DroneEntity.NEAR_TRACKING_RANGE) {
                    /* Else, teleport the player to the drone if it's nearing the end of it's tracking range. */
                    player.requestTeleport(drone.getX(), DroneEntity.PLAYER_HEIGHT, drone.getZ());

                }
            }

            if (player.getCameraEntity() instanceof DroneEntity && (  // currently viewing through the goggles AND one of the following:
                !GogglesItem.isWearingGoggles(player) || // not wearing goggles on head
                !GogglesItem.isOn(player) ||             // goggles are powered off on head
                player.getCameraEntity().removed))       // camera entity is dead
            {
                    resetView(player);
            }
        }
    }

    public static void setView(ServerPlayerEntity player, DroneEntity drone) {
        if(!(player.getCameraEntity() instanceof DroneEntity)) {
            drone.addPlayerStartPos(player);
            player.setNoGravity(true);
            player.setCameraEntity(drone);

            String[] keys = ServerInitializer.SERVER_PLAYER_KEYS.get(player.getUuid());
            if(keys != null) {
                String subString = keys[0] + " or " + keys[1];
                player.sendMessage(new LiteralText("Press " + subString + " power off goggles"), true);
            }
        }
    }

    public static void resetView(ServerPlayerEntity player) {
        if(player.getCameraEntity() instanceof DroneEntity) {
            DroneEntity drone = (DroneEntity) player.getCameraEntity();

            Vec3d pos = drone.getPlayerStartPos().get(player);
            movePlayer(pos.x, pos.y, pos.z, player);
            drone.removePlayerStartPos(player);

            player.setNoGravity(false);
            player.setCameraEntity(player);
        }
    }

    public static boolean isInGoggles(ServerPlayerEntity player) {
        return player.getCameraEntity() instanceof DroneEntity;
    }

    public static void movePlayer(double x, double y, double z, ServerPlayerEntity player) {
        ServerWorld world = player.getServerWorld();
        ChunkPos chunkPos = new ChunkPos(new BlockPos(x, y, z));
        world.getChunkManager().addTicket(ChunkTicketType.POST_TELEPORT, chunkPos, 1, player.getEntityId());
        player.networkHandler.requestTeleport(x, y, z, player.yaw, player.pitch);
    }

    public static void register() {
        ServerTickCallback.EVENT.register(ServerTick::tick);
    }
}
