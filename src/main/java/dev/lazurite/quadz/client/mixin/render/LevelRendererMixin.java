package dev.lazurite.quadz.client.mixin.render;

import com.mojang.blaze3d.vertex.PoseStack;
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

    @Redirect(
            method = "renderLevel",
            at = @At(
                    value = "CONSTANT",
                    args = "classValue=net/minecraft/client/player/LocalPlayer"
            ),
            require = 0
    )
    public boolean renderLevel_CONSTANT_dev(Object entity, Class<?> clazz, PoseStack poseStack, float f, long l, boolean bl, Camera camera) {
        return clazz.isInstance(entity) && camera.getEntity() instanceof QuadcopterEntity quadcopter && !quadcopter.shouldRenderPlayer();
    }

    @Redirect(
            method = "renderLevel",
            at = @At(
                    value = "CONSTANT",
                    args = "classValue=net/minecraft/class_746"
            ),
            require = 0
    )
    public boolean renderLevel_CONSTANT_prod(Object entity, Class<?> clazz, PoseStack poseStack, float f, long l, boolean bl, Camera camera) {
        return clazz.isInstance(entity) && camera.getEntity() instanceof QuadcopterEntity quadcopter && !quadcopter.shouldRenderPlayer();
    }

}
