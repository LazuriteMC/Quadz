package dev.lazurite.quadz.client.mixin;

import dev.lazurite.quadz.client.hooks.CameraHooks;
import dev.lazurite.quadz.client.extension.CameraTypeExtension;
import net.minecraft.client.CameraType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CameraType.class)
public class CameraTypeMixin implements CameraTypeExtension {

    @Inject(method = "cycle", at = @At("HEAD"), cancellable = true)
    public void cycle_HEAD(CallbackInfoReturnable<CameraType> cir) {
        var type = CameraHooks.onCycle();
        if (type != null) cir.setReturnValue(type);
    }

    @Override
    public void reset() {
        CameraHooks.onCameraReset();
    }

}
