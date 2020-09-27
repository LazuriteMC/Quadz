package bluevista.fpvracing.mixin;

import bluevista.fpvracing.server.ServerTick;
import bluevista.fpvracing.server.entities.DroneEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.Packet;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * This mixin class modifies the way sound works in minecraft. Normally, the distance to a sound is
 * calculated using the player's position and the source of the sound's position. When flying a drone, this
 * system doesn't work so well. Instead, the distance is now calculated between the source and the drone being
 * flown rather than the actual player's position.
 * @author Ethan Johnson
 */
@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    @Shadow @Final List<ServerPlayerEntity> players;

    /**
     * This mixin method recalculates the distance between the {@link ServerPlayerEntity} and the entity
     * which produced a sound by using the position of the drone if the player is flying a drone.
     * @param player the player
     * @param x the x position of the sound
     * @param y the y position of the sound
     * @param z the z position of the sound
     * @param distance the distance to the sound
     * @param worldKey the world key
     * @param packet the packet that will be sent
     * @param info required by every mixin injection
     */
    @Inject(at = @At("HEAD"), method = "sendToAround", cancellable = true)
    public void sendToAround(PlayerEntity player, double x, double y, double z, double distance, RegistryKey<World> worldKey, Packet<?> packet, CallbackInfo info) {
        for (int i = 0; i < this.players.size(); ++i) {
            ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity) this.players.get(i);

            if (serverPlayerEntity != player && serverPlayerEntity.world.getRegistryKey() == worldKey) {
                if(ServerTick.isInGoggles(serverPlayerEntity)) {
                    DroneEntity drone = (DroneEntity) serverPlayerEntity.getCameraEntity();

                    double d = x - drone.getX();
                    double e = y - drone.getY();
                    double f = z - drone.getZ();
                    if (d * d + e * e + f * f < distance * distance) {
                        serverPlayerEntity.networkHandler.sendPacket(packet);
                    }
                }
            }
        }

        info.cancel();
    }
}
