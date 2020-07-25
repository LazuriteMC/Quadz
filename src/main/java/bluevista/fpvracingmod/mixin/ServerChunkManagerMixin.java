package bluevista.fpvracingmod.mixin;

import bluevista.fpvracingmod.server.entities.DroneEntity;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.server.world.ServerChunkManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerChunkManager.class)
public class ServerChunkManagerMixin {
    @Shadow @Final ChunkTicketManager ticketManager;

    @Inject(at = @At("HEAD"), method = "shouldTickEntity", cancellable = true)
    public void shouldTickEntity(Entity entity, CallbackInfoReturnable info) {
        if(entity instanceof DroneEntity) {
            DroneEntity drone = (DroneEntity) entity;
            if(drone.hasInfiniteTracking())
                info.setReturnValue(true);
        }
    }
}
