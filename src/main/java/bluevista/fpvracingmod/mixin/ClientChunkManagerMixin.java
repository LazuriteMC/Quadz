package bluevista.fpvracingmod.mixin;

import bluevista.fpvracingmod.server.entities.DroneEntity;
import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientChunkManager.class)
public class ClientChunkManagerMixin {

    @Inject(at = @At("HEAD"), method = "shouldTickEntity", cancellable = true)
    public void shouldTickEntity(Entity entity, CallbackInfoReturnable info) {
        if(entity instanceof DroneEntity) {
            DroneEntity drone = (DroneEntity) entity;
            if(drone.hasInfiniteTracking())
                info.setReturnValue(true);
        }
    }
}
