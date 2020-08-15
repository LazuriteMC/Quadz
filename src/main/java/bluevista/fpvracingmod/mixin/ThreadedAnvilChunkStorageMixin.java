package bluevista.fpvracingmod.mixin;

import bluevista.fpvracingmod.server.entities.DroneEntity;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.math.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ThreadedAnvilChunkStorage.class)
public class ThreadedAnvilChunkStorageMixin {
//    @Redirect(
//            method = "Lnet/minecraft/server/world/ThreadedAnvilChunkStorage;isTooFarFromPlayersToSpawnMobs(Lnet/minecraft/util/math/ChunkPos;)Z",
//            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ThreadedAnvilChunkStorage;getSquaredDistance(Lnet/minecraft/util/math/ChunkPos;Lnet/minecraft/entity/Entity;)D", ordinal = 0)
//    )
//    @Inject(at = @At("HEAD"), method = "getSquaredDistance", cancellable = true)
//    private static void getSquaredDistance(ChunkPos pos, Entity entity, CallbackInfoReturnable<Double> info) {
//        if(entity instanceof ServerPlayerEntity) {
//            ServerPlayerEntity player = (ServerPlayerEntity) entity;
//            if(player.getCameraEntity() instanceof DroneEntity) {
//                DroneEntity drone = (DroneEntity) player.getCameraEntity();
//                double d = (double)(pos.x * 16 + 8);
//                double e = (double)(pos.z * 16 + 8);
//                double f = d - drone.getX();
//                double g = e - drone.getZ();
//                info.setReturnValue(f * f + g * g);
//            }
//        }
//    }
}
