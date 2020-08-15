package bluevista.fpvracingmod.mixin;

import bluevista.fpvracingmod.server.entities.DroneEntity;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkSectionPos.class)
public class ChunkSectionPosMixin {
//    @Inject(at = @At("HEAD"), method = "from(Lnet/minecraft/entity/Entity;)Lnet/minecraft/util/math/ChunkSectionPos;", cancellable = true)
//    private static void from(Entity entity, CallbackInfoReturnable<ChunkSectionPos> info) {
//        if(entity instanceof ServerPlayerEntity) {
//            ServerPlayerEntity player = (ServerPlayerEntity) entity;
//            if(player.getCameraEntity() instanceof DroneEntity) {
//                DroneEntity drone = (DroneEntity) player.getCameraEntity();
//                info.setReturnValue(ChunkSectionPos.from(ChunkSectionPos.getSectionCoord(MathHelper.floor(drone.getX())), ChunkSectionPos.getSectionCoord(MathHelper.floor(drone.getY())), ChunkSectionPos.getSectionCoord(MathHelper.floor(drone.getZ()))));
//            }
//        }
//    }
}
