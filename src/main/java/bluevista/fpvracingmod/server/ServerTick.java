package bluevista.fpvracingmod.server;

import bluevista.fpvracingmod.server.entities.DroneEntity;
import bluevista.fpvracingmod.server.items.GogglesItem;
import bluevista.fpvracingmod.server.items.TransmitterItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.server.ServerTickCallback;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

@Environment(EnvType.CLIENT)
public class ServerTick {

    public static void tick(MinecraftServer server) {
        List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();
        for (ServerPlayerEntity player : players) {

            if (GogglesItem.isWearingGoggles(player) && !isInGoggles(player)) {
                List<DroneEntity> drones = DroneEntity.getNearbyDrones(player, 320);
                for (DroneEntity drone : drones) {
                    if (GogglesItem.isOnRightChannel(drone, player)) {
                        setView(player, drone);
                    }
                }
            }

            if (player.getCameraEntity() instanceof DroneEntity) {
                DroneEntity drone = (DroneEntity) player.getCameraEntity();
                if (isInGoggles(player) && TransmitterItem.isBoundTransmitter(player.getMainHandStack(), drone)) {
                    Packet packet = new PlayerPositionLookS2CPacket(drone.getX(), drone.getY(), drone.getZ(), 0, 0, null, 10);
                    ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, packet);
                }
            }

            if (!GogglesItem.isWearingGoggles(player) && isInGoggles(player) || !GogglesItem.isOn(player) || player.getCameraEntity() != null && player.getCameraEntity().removed)
                resetView(player);
        }
    }

    public static void setView(ServerPlayerEntity player, DroneEntity drone) {
        if(!(player.getCameraEntity() instanceof DroneEntity)) {
            drone.setInfiniteTracking(true);
            player.setCameraEntity(drone);
            if (TransmitterItem.isHoldingTransmitter(player));
                drone.setPlayerPos(player.getBlockPos());
        }
    }

    public static void resetView(ServerPlayerEntity player) {
        if(player.getCameraEntity() instanceof DroneEntity) {
            DroneEntity drone = (DroneEntity) player.getCameraEntity();
            drone.setInfiniteTracking(false);

            // TODO Put it back!!!
//            BlockPos pp = drone.getOriginalPlayerPos();
//            mc.player.setPos(pp.getX(), pp.getY(), pp.getZ());

            player.setCameraEntity(player);
        }
    }

    public static boolean isInGoggles(ServerPlayerEntity player) {
        return player.getCameraEntity() instanceof DroneEntity;
    }

    public static void register() {
        ServerTickCallback.EVENT.register(ServerTick::tick);
    }
}
