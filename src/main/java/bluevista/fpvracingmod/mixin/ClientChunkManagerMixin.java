package bluevista.fpvracingmod.mixin;

import bluevista.fpvracingmod.server.entities.DroneEntity;
import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientChunkManager.class)
public abstract class ClientChunkManagerMixin {
    @Shadow public abstract void setChunkMapCenter(int x, int z);
    @Shadow @Final ClientWorld world;

    @Inject(at = @At("HEAD"), method = "shouldTickEntity", cancellable = true)
    public void shouldTickEntity(Entity entity, CallbackInfoReturnable info) {
        if(entity instanceof DroneEntity) {
            DroneEntity drone = (DroneEntity) entity;
            if(drone.hasInfiniteTracking()) {
//                Chunk c = world.getChunkManager().getChunk((int) drone.getX(), (int) drone.getZ(), ChunkStatus.FULL, false);
//                setChunkMapCenter(c.getPos().x, c.getPos().z);
                info.setReturnValue(true);
            }
        }
    }
}
