package bluevista.fpvracingmod.server;

import bluevista.fpvracingmod.server.entities.DroneEntity;
import bluevista.fpvracingmod.server.items.GogglesItem;
import bluevista.fpvracingmod.server.items.TransmitterItem;
import net.fabricmc.fabric.api.event.server.ServerTickCallback;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class ServerTick {
    public static void tick(MinecraftServer server) {
        List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();

        for (ServerPlayerEntity player : players) { // for every player in the server
            if (GogglesItem.isWearingGoggles(player) && !ServerHelper.isInGoggles(player)) {
                List<Entity> drones = DroneEntity.getNearbyDrones(player, DroneEntity.TRACKING_RANGE);

                for (Entity entity : drones) { // for every drone in range of given player
                    DroneEntity drone = (DroneEntity) entity;

                    if (GogglesItem.isOnSameChannel(drone, player)) {
                        setView(player, drone);
                    }

                    player.inventory.main.forEach(itemStack -> {
                        if(itemStack.getItem() instanceof TransmitterItem) {
                            if(TransmitterItem.isBoundTransmitter(itemStack, drone)) {
                                drone.playerID = player.getUuid();
                            }
                        }
                    });

                    if(drone.playerID == null)
                        drone.kill();
                }
            }

            if (ServerHelper.isInGoggles(player)) {
                DroneEntity drone = (DroneEntity) player.getCameraEntity();
                Vec3d pos = drone.getPlayerStartPos().get(player);
//                player.getServerWorld().getChunkManager().updateCameraPosition(player);

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
        }
    }

    public static void resetView(ServerPlayerEntity player) {
        if(player.getCameraEntity() instanceof DroneEntity) {
            DroneEntity drone = (DroneEntity) player.getCameraEntity();

            Vec3d pos = drone.getPlayerStartPos().get(player);
            ServerHelper.movePlayer(pos.x, pos.y, pos.z, player);
            drone.removePlayerStartPos(player);

            player.setNoGravity(false);
            player.setCameraEntity(player);
        }
    }

    public static void register() {
        ServerTickCallback.EVENT.register(ServerTick::tick);
    }
}
