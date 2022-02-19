package dev.lazurite.quadz.compat.mixin.optifabric;

import com.jme3.math.Quaternion;
import dev.lazurite.quadz.common.quadcopter.entity.QuadcopterEntity;
import dev.lazurite.rayon.impl.bullet.math.Convert;
import dev.lazurite.toolbox.api.math.QuaternionHelper;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Shadow @Final private Camera mainCamera;

    @ModifyArg(
            method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lcom/mojang/math/Quaternion;)V",
                    ordinal = 3
            )
    )
    public com.mojang.math.Quaternion multiplyYaw(com.mojang.math.Quaternion quaternion) {
        if (mainCamera.getEntity() instanceof QuadcopterEntity) {
            return QuaternionHelper.rotateY(Convert.toMinecraft(new Quaternion()), 180);
        }

        return quaternion;
    }

    @ModifyArg(
            method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lcom/mojang/math/Quaternion;)V",
                    ordinal = 4
            )
    )
    public com.mojang.math.Quaternion multiplyPitch(com.mojang.math.Quaternion quaternion) {
        if (mainCamera.getEntity() instanceof QuadcopterEntity) {
            return Convert.toMinecraft(new Quaternion());
        }

        return quaternion;
    }
}
