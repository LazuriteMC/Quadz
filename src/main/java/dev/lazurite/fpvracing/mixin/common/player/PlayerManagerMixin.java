package dev.lazurite.fpvracing.mixin.common.player;

import dev.lazurite.fpvracing.common.access.PlayerAccess;
import dev.lazurite.fpvracing.common.entity.QuadcopterEntity;
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
 */
@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    @Shadow @Final private List<ServerPlayerEntity> players;

    @Inject(method = "sendToAround", at = @At("TAIL"))
    public void sendToAround(PlayerEntity player, double x, double y, double z, double distance, RegistryKey<World> worldKey, Packet<?> packet, CallbackInfo info) {
        for (ServerPlayerEntity serverPlayer : this.players) {
            if (!serverPlayer.equals(player) && serverPlayer.world.getRegistryKey() == worldKey) {
                if (((PlayerAccess) (Object) serverPlayer).isInGoggles()) {
                    QuadcopterEntity quadcopter = (QuadcopterEntity) serverPlayer.getCameraEntity();

                    double d = x - quadcopter.getX();
                    double e = y - quadcopter.getY();
                    double f = z - quadcopter.getZ();

                    if (d * d + e * e + f * f < distance * distance) {
                        serverPlayer.networkHandler.sendPacket(packet);
                    }
                }
            }
        }
    }
}
