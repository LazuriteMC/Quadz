package bluevista.fpvracingmod.mixin;

import bluevista.fpvracingmod.server.ServerTick;
import bluevista.fpvracingmod.server.entities.DroneEntity;
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

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    @Shadow @Final List<ServerPlayerEntity> players;

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
