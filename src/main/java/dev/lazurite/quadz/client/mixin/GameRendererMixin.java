package dev.lazurite.quadz.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.lazurite.quadz.client.QuadzClient;
import dev.lazurite.quadz.client.render.RenderHooks;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "renderLevel", at = @At("HEAD"))
    public void renderLevel$HEAD(float f, long l, PoseStack poseStack, CallbackInfo ci) {
        RenderHooks.onRenderLevel(f);
    }

    @ModifyArg(
            method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lorg/joml/Quaternionf;)V",
                    ordinal = 2
            )
    )
    public Quaternionf renderLevel$multiplyYaw(Quaternionf quaternion) {
        return RenderHooks.onMultiplyYaw(quaternion);
    }

    @ModifyArg(
            method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lorg/joml/Quaternionf;)V",
                    ordinal = 3
            )
    )
    public Quaternionf renderLevel$multiplyPitch(Quaternionf quaternion) {
        return RenderHooks.onMultiplyPitch(quaternion);
    }

    @Inject(method = "renderItemInHand", at = @At("HEAD"), cancellable = true)
    private void renderItemInHand$HEAD(PoseStack poseStack, Camera camera, float f, CallbackInfo ci) {
        if (QuadzClient.getQuadcopterFromCamera().isPresent()) {
            ci.cancel();
        }
    }

}
