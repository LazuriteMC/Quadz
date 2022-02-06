package dev.lazurite.quadz.client.mixin.render;

import dev.lazurite.quadz.common.state.entity.QuadcopterEntity;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

    @Redirect(
            method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Camera;isDetached()Z"
            )
    )
    public boolean renderLevel_isDetached(Camera camera) {
        return camera.isDetached() || camera.getEntity() instanceof QuadcopterEntity quadcopter && quadcopter.shouldRenderSelf();
    }

}
