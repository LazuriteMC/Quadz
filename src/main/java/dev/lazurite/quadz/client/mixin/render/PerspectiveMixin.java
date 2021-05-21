package dev.lazurite.quadz.client.mixin.render;

import dev.lazurite.quadz.common.state.entity.QuadcopterEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.Perspective;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Perspective.class)
public class PerspectiveMixin {
    @Shadow @Final private static Perspective[] VALUES;

    @Inject(method = "next", at = @At("HEAD"), cancellable = true)
    public void next(CallbackInfoReturnable<Perspective> info) {
        boolean isQuadcopter = MinecraftClient.getInstance().getCameraEntity() instanceof QuadcopterEntity;
        boolean isNextFront = VALUES[(((Perspective) (Object) this).ordinal() + 1) % VALUES.length] == Perspective.THIRD_PERSON_FRONT;

        if (isNextFront && isQuadcopter) {
            info.setReturnValue(Perspective.FIRST_PERSON);
        }
    }
}
