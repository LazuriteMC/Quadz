package bluevista.fpvracingmod.mixin;

import bluevista.fpvracingmod.server.entities.DroneEntity;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.SetCameraEntityS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {
    public abstract Entity getCameraEntity();
    public abstract void requestTeleport(double destX, double destY, double destZ);

    @Shadow Entity cameraEntity;
    @Shadow ServerPlayNetworkHandler networkHandler;

    @Inject(at = @At("HEAD"), method = "onStoppedTracking", cancellable = true)
    public void onStoppedTracking(Entity entity, CallbackInfo info) {
        if(entity instanceof DroneEntity) {
            DroneEntity drone = (DroneEntity) entity;
            if(drone.hasInfiniteTracking())
                info.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "setCameraEntity", cancellable = true)
    public void setCameraEntity(Entity entity, CallbackInfo info) {
        Entity prevEntity = this.getCameraEntity();
        Entity nextEntity = (Entity)(entity == null ? this : entity);

        if(prevEntity instanceof DroneEntity || nextEntity instanceof DroneEntity) {
            networkHandler.sendPacket(new SetCameraEntityS2CPacket(nextEntity));
            cameraEntity = nextEntity;
            info.cancel();
        }
    }
}
