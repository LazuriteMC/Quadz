package bluevista.fpvracing.server;

import bluevista.fpvracing.network.SelectedSlotS2C;
import bluevista.fpvracing.network.ShouldRenderPlayerS2C;
import bluevista.fpvracing.server.entities.DroneEntity;
import bluevista.fpvracing.server.items.GogglesItem;
import bluevista.fpvracing.server.items.TransmitterItem;
import net.fabricmc.fabric.api.event.server.ServerTickCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.Vec3d;

import java.util.List;

/**
 * This class handles most of the game logic involved with putting on the goggles
 * and controlling whether or not players should render.
 * @author Ethan Johnson
 */
public class ServerTick {
    public static final PlayerPositionManager playerPositionManager = new PlayerPositionManager();

    /**
     * This method is responsible for handling whether or not a
     * {@link ServerPlayerEntity} is viewing a {@link DroneEntity} through their {@link GogglesItem}.
     * @param server the {@link MinecraftServer} object
     */
    public static void tick(MinecraftServer server) {
        List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();

        for (ServerPlayerEntity player : players) { // For every player in the server...

            // If the player is already viewing a DroneEntity...
            if (GogglesItem.isInGoggles(player)) {
                DroneEntity drone = (DroneEntity) player.getCameraEntity();
                Vec3d pos = playerPositionManager.getPos(player);

                // If the drone is in range of where the player started...
                if (Math.sqrt(drone.squaredDistanceTo(pos)) < DroneEntity.PSEUDO_TRACKING_RANGE) {
                    ShouldRenderPlayerS2C.send(player, true);

                    if (!player.getPos().equals(pos)) {
                        player.requestTeleport(pos.x, pos.y, pos.z);
                        player.setNoGravity(false);
                    }

                // If the drone is nearing the end of it's tracking range...
                } else if (player.distanceTo(drone) > DroneEntity.PSEUDO_TRACKING_RANGE) {
                    player.requestTeleport(drone.getX(), drone.getY() + 50, drone.getZ());
                    player.setNoGravity(true);
                    ShouldRenderPlayerS2C.send(player, false);
                }
            }

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
        playerPositionManager.addPos(player);
        player.setCameraEntity(drone);

        for (int i = 0; i < 9; i++) {
            ItemStack itemStack = player.inventory.getStack(i);

            if (TransmitterItem.isBoundTransmitter(itemStack, drone)) {
                player.inventory.selectedSlot = i;
                SelectedSlotS2C.send(player, i);
            }
        }

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
            ShouldRenderPlayerS2C.send(player, false);
            GogglesItem.setOn(player.inventory.armor.get(3), false);

            playerPositionManager.resetPos(player);
            player.setNoGravity(false);
            player.setCameraEntity(player);
        }
    }

    /**
     * Register the {@link ServerTick#tick(MinecraftServer)} method.
     */
    public static void register() {
        ServerTickCallback.EVENT.register(ServerTick::tick);
    }

    /**
     * Determines whether or not the given player entity has
     * the FPV Drone Racing mod.
     * @param player the player to check for in the list of configs
     * @return whether or not the player is in the list of configs
     */
    public static boolean playerHasMod(ServerPlayerEntity player) {
        return ServerInitializer.SERVER_PLAYER_CONFIGS.containsKey(player.getUuid());
    }
}
