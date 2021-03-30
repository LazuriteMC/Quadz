package dev.lazurite.quadz.client.mixin.render;

import com.jme3.math.Quaternion;
import dev.lazurite.quadz.common.entity.QuadcopterEntity;
import dev.lazurite.rayon.core.impl.util.math.QuaternionHelper;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(GameRenderer.class)
public class MatrixRotationMixin {
    @Shadow @Final private Camera camera;

    @ModifyArg(
            method = "renderWorld",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/util/math/MatrixStack;multiply(Lnet/minecraft/util/math/Quaternion;)V",
                    ordinal = 2
            )
    )
    public net.minecraft.util.math.Quaternion multiplyYaw(net.minecraft.util.math.Quaternion quaternion) {
        if (camera.getFocusedEntity() instanceof QuadcopterEntity) {
            return QuaternionHelper.bulletToMinecraft(QuaternionHelper.rotateY(new Quaternion(), 180));
        }

        return quaternion;
    }

    @ModifyArg(
            method = "renderWorld",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/util/math/MatrixStack;multiply(Lnet/minecraft/util/math/Quaternion;)V",
                    ordinal = 3
            )
    )
    public net.minecraft.util.math.Quaternion multiplyPitch(net.minecraft.util.math.Quaternion quaternion) {
        if (camera.getFocusedEntity() instanceof QuadcopterEntity) {
            return QuaternionHelper.bulletToMinecraft(new Quaternion());
        }

        return quaternion;
    }
}
