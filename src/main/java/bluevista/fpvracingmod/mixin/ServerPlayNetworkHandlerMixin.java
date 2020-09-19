package bluevista.fpvracingmod.mixin;

import bluevista.fpvracingmod.server.entities.DroneEntity;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    @Shadow int requestedTeleportId;
    @Shadow int teleportRequestTick;
    @Shadow Vec3d requestedTeleportPos;
    @Shadow ServerPlayerEntity player;
    @Shadow int ticks;

    @Inject(at = @At("HEAD"), method = "teleportRequest(DDDFFLjava/util/Set;)V", cancellable = true)
    public void teleportRequest(double x, double y, double z, float yaw, float pitch, Set<PlayerPositionLookS2CPacket.Flag> set, CallbackInfo info) {
        double d = set.contains(PlayerPositionLookS2CPacket.Flag.X) ? this.player.getX() : 0.0D;
        double e = set.contains(PlayerPositionLookS2CPacket.Flag.Y) ? this.player.getY() : 0.0D;
        double f = set.contains(PlayerPositionLookS2CPacket.Flag.Z) ? this.player.getZ() : 0.0D;
        float g = set.contains(PlayerPositionLookS2CPacket.Flag.Y_ROT) ? this.player.yaw : 0.0F;
        float h = set.contains(PlayerPositionLookS2CPacket.Flag.X_ROT) ? this.player.pitch : 0.0F;
        this.requestedTeleportPos = new Vec3d(x, y, z);
        if (++this.requestedTeleportId == Integer.MAX_VALUE) {
            this.requestedTeleportId = 0;
        }

        this.teleportRequestTick = this.ticks;
        this.player.updatePositionAndAngles(x, y, z, yaw, pitch);

        if(!(this.player.getCameraEntity() instanceof DroneEntity)) {
            this.player.networkHandler.sendPacket(new PlayerPositionLookS2CPacket(x - d, y - e, z - f, yaw - g, pitch - h, set, this.requestedTeleportId));
        }

        info.cancel();
    }
}
