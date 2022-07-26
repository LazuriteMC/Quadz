package dev.lazurite.quadz.client.mixin.render;

import com.mojang.math.Quaternion;
import dev.lazurite.quadz.common.entity.RemoteControllableEntity;
import dev.lazurite.toolbox.api.math.QuaternionHelper;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(GameRenderer.class)
public class MatrixRotationMixin {
    @Shadow @Final private Camera mainCamera;

    @ModifyArg(
            method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lcom/mojang/math/Quaternion;)V",
                    ordinal = 2
            )
    )
    public com.mojang.math.Quaternion multiplyYaw(com.mojang.math.Quaternion quaternion) {
        if (mainCamera.getEntity() instanceof RemoteControllableEntity) {
            return QuaternionHelper.rotateY(new Quaternion(Quaternion.ONE), 180);
        }

        return quaternion;
    }

    @ModifyArg(
            method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lcom/mojang/math/Quaternion;)V",
                    ordinal = 3
            )
    )
    public com.mojang.math.Quaternion multiplyPitch(com.mojang.math.Quaternion quaternion) {
        if (mainCamera.getEntity() instanceof RemoteControllableEntity) {
            return new Quaternion(Quaternion.ONE);
        }

        return quaternion;
    }
}
