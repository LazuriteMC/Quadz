package dev.lazurite.quadz.client.mixin.render;

import dev.lazurite.quadz.common.state.entity.QuadcopterEntity;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CameraType.class)
public class CameraTypeMixin {
    @Shadow @Final private static CameraType[] VALUES;

    @Inject(method = "cycle", at = @At("HEAD"), cancellable = true)
    public void cycle_HEAD(CallbackInfoReturnable<CameraType> info) {
        boolean isQuadcopter = Minecraft.getInstance().getCameraEntity() instanceof QuadcopterEntity;
        boolean isNextFront = VALUES[(((CameraType) (Object) this).ordinal() + 1) % VALUES.length] == CameraType.THIRD_PERSON_FRONT;

        if (isNextFront && isQuadcopter) {
            info.setReturnValue(CameraType.FIRST_PERSON);
        }
    }
}
