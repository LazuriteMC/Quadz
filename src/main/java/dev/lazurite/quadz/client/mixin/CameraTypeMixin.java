package dev.lazurite.quadz.client.mixin;

import dev.lazurite.quadz.client.extension.CameraTypeExtension;
import dev.lazurite.quadz.client.render.camera.CameraHooks;
import net.minecraft.client.CameraType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CameraType.class)
public class CameraTypeMixin implements CameraTypeExtension {

    @Inject(method = "cycle", at = @At("HEAD"), cancellable = true)
    public void cycle$HEAD(CallbackInfoReturnable<CameraType> cir) {
        CameraHooks.onCycle().ifPresent(cir::setReturnValue);
    }

    @Override
    public void quadz$reset() {
        CameraHooks.onCameraReset();
    }

}
