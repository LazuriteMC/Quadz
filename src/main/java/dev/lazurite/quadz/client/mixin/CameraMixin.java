package dev.lazurite.quadz.client.mixin;

import dev.lazurite.quadz.client.hooks.CameraHooks;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public class CameraMixin {

    @Shadow private Entity entity;

    @Inject(method = "setPosition(DDD)V", at = @At("HEAD"), cancellable = true)
    public void setPosition_HEAD(double x, double y, double z, CallbackInfo ci) {
        if (CameraHooks.onSetPosition(entity)) {
            ci.cancel();
        }
    }

    @Inject(method = "setRotation", at = @At("HEAD"), cancellable = true)
    public void setRotation_HEAD(float yaw, float pitch, CallbackInfo ci) {
        if (CameraHooks.onSetRotation(entity)) {
            ci.cancel();
        }
    }

    @Inject(
            method = "setup",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Camera;setRotation(FF)V"
            )
    )
    public void setup_setRotation(BlockGetter blockGetter, Entity entity, boolean bl, boolean bl2, float f, CallbackInfo ci) {
        CameraHooks.onRotate((Camera) (Object) this, entity, f, bl);
    }

}
