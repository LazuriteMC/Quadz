package bluevista.fpvracingmod.mixin;

import bluevista.fpvracingmod.inject.ServerPlayerEntityInject;
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
public abstract class ServerPlayerEntityMixin implements ServerPlayerEntityInject {
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

    @Override
    public void setDroneView(Entity entity) {
        Entity entity2 = this.getCameraEntity();
        this.cameraEntity = (Entity)(entity == null ? this : entity);
        if (entity2 != this.cameraEntity) {
            this.networkHandler.sendPacket(new SetCameraEntityS2CPacket(this.cameraEntity));
            this.requestTeleport(this.cameraEntity.getX(), this.cameraEntity.getY(), this.cameraEntity.getZ());
        }
    }
}
