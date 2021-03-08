package dev.lazurite.chonker.mixin.common;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(targets = "net/minecraft/server/world/ThreadedAnvilChunkStorage$EntityTracker")
public abstract class EntityTrackerMixin {

    @Redirect(
            method = "updateCameraPosition(Lnet/minecraft/server/network/ServerPlayerEntity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayerEntity;getPos()Lnet/minecraft/util/math/Vec3d;"
            )
    )
    public Vec3d getPos(ServerPlayerEntity player) {
        return player.getCameraEntity().getPos();
    }

}
