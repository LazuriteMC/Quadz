package bluevista.fpvracing.server;

import bluevista.fpvracing.network.entity.ShouldRenderPlayerS2C;
import bluevista.fpvracing.server.entities.DroneEntity;
import bluevista.fpvracing.server.items.GogglesItem;
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

/**
 * This class handles most of the game logic involved with putting on the goggles
 * and controlling whether or not players should render.
 * @author Ethan Johnson
 */
public class ServerTick {
    /**
     * This method is responsible for handling whether or not a
     * {@link ServerPlayerEntity} is viewing a {@link DroneEntity} through their {@link GogglesItem}.
     * @param server the {@link MinecraftServer} object
     */
    public static void tick(MinecraftServer server) {
        List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();

        for (ServerPlayerEntity player : players) { // For every player in the server...
            boolean shouldRenderPlayer = false;

            // If the player is wearing powered-on goggles but not in a DroneEntity's view...
            if (GogglesItem.isOn(player) && !isInGoggles(player)) {
                List<Entity> drones = DroneEntity.getDrones(player); // list of all drones

                for (Entity entity : drones) { // For every drone...
                    DroneEntity drone = (DroneEntity) entity;

                    // Make sure drone is within range of player...
                    if(player.distanceTo(drone) > DroneEntity.TRACKING_RANGE) {
                        continue;

                    // Otherwise, see if the drone is on the same channel as the goggles..lie
                    } else if (GogglesItem.isOnSameChannel(drone, player)) {
                        setView(player, drone);
                    }
                }
            }

            // If the player is already viewing a DroneEntity...
            if (isInGoggles(player)) {
                DroneEntity drone = (DroneEntity) player.getCameraEntity();
                Vec3d pos = drone.getPlayerStartPos().get(player);

                // If the drone is in range of where the player started...
                if (Math.sqrt(drone.squaredDistanceTo(pos)) < DroneEntity.PSEUDO_TRACKING_RANGE) {
                    shouldRenderPlayer = true;

                    if (!player.getPos().equals(pos)) {
                        player.requestTeleport(pos.x, pos.y, pos.z);
                        player.setNoGravity(false);
                    }

                // If the drone is nearing the end of it's tracking range...
                } else if (player.distanceTo(drone) > DroneEntity.PSEUDO_TRACKING_RANGE) {
                    player.requestTeleport(drone.getX(), drone.getY() + 50, drone.getZ());
                    player.setNoGravity(true);
                    shouldRenderPlayer = false;

                }
            }

            ShouldRenderPlayerS2C.send(player, shouldRenderPlayer);

            if (player.getCameraEntity() instanceof DroneEntity && (                            // currently viewing through the goggles AND one of the following:
                !GogglesItem.isWearingGoggles(player) ||                                        // not wearing goggles on head
                !GogglesItem.isOn(player) ||                                                    // goggles are powered off on head
                !GogglesItem.isOnSameChannel((DroneEntity) player.getCameraEntity(), player) || // suddenly on wrong channel
                player.getCameraEntity().removed))                                              // camera entity is dead
            {
                    resetView(player);
            }
        }
    }

    /**
     * Changes the camera entity for the given {@link ServerPlayerEntity}
     * to the given {@link DroneEntity}. It also prints out a message on
     * how to go back to the player view.
     * @param player the {@link ServerPlayerEntity} to change the camera from
     * @param drone the {@link DroneEntity} to change the camera to
     */
    public static void setView(ServerPlayerEntity player, DroneEntity drone) {
        drone.addPlayerStartPos(player);
        player.setCameraEntity(drone);

        String[] keys = ServerInitializer.SERVER_PLAYER_KEYS.get(player.getUuid());
        if (keys != null) {
            String subString = keys[0] + " or " + keys[1];
            player.sendMessage(new LiteralText("Press " + subString + " power off goggles"), true);
        }
    }

    /**
     * Resets the view of the {@link ServerPlayerEntity} back to
     * itself from the {@link DroneEntity} it was set to.
     * @param player the {@link ServerPlayerEntity} to change the view of
     */
    public static void resetView(ServerPlayerEntity player) {
        if (player.getCameraEntity() instanceof DroneEntity) {
            DroneEntity drone = (DroneEntity) player.getCameraEntity();

            Vec3d pos = drone.getPlayerStartPos().get(player);
            movePlayer(pos.x, pos.y, pos.z, player);
            drone.removePlayerStartPos(player);

            ShouldRenderPlayerS2C.send(player, true);
            player.setNoGravity(false);
            player.setCameraEntity(player);
        }
    }

    /**
     * Moves the {@link ServerPlayerEntity} to the given coordinates.
     * Most notably, it submits a chunk ticket. Should be used when teleporting
     * a long distance.
     * @param x
     * @param y
     * @param z
     * @param player the given {@link ServerPlayerEntity} to teleport
     */
    protected static void movePlayer(double x, double y, double z, ServerPlayerEntity player) {
        ServerWorld world = player.getServerWorld();
        ChunkPos chunkPos = new ChunkPos(new BlockPos(x, y, z));
        world.getChunkManager().addTicket(ChunkTicketType.POST_TELEPORT, chunkPos, 1, player.getEntityId());
        player.networkHandler.requestTeleport(x, y, z, player.yaw, player.pitch);
    }

    /**
     * Determines whether or not the given {@link ServerPlayerEntity}
     * has a camera entity of type {@link DroneEntity}.
     * @param player the given {@link ServerPlayerEntity}
     * @return whether or not the camera entity is of type {@link DroneEntity}
     */
    public static boolean isInGoggles(ServerPlayerEntity player) {
        return player.getCameraEntity() instanceof DroneEntity;
    }

    /**
     * Register the {@link ServerTick#tick(MinecraftServer)} method.
     */
    public static void register() {
        ServerTickCallback.EVENT.register(ServerTick::tick);
    }
}
