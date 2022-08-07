package dev.lazurite.quadz.client.mixin.render;

import com.jme3.math.Quaternion;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import dev.lazurite.quadz.common.entity.QuadcopterEntity;
import dev.lazurite.rayon.impl.bullet.math.Convert;
import dev.lazurite.toolbox.api.math.QuaternionHelper;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This mixin is mainly for manipulating the player's camera (i.e. rotating it according to the quadcopter's view).
 */
@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Shadow @Final private Camera mainCamera;

	@Inject(method = "renderLevel", at = @At("HEAD"))
	public void renderLevel_HEAD(float f, long l, PoseStack poseStack, CallbackInfo ci) {
		// TODO replace this with a corduroy view
		/* Rotate the entire view to match the view of the quadcopter */
		if (mainCamera.getEntity() instanceof QuadcopterEntity quadcopter) {
			var q = quadcopter.getPhysicsRotation(new Quaternion(), f);
			q.set(q.getX(), -q.getY(), q.getZ(), -q.getW());
			q.set(Convert.toBullet(QuaternionHelper.rotateX(Convert.toMinecraft(q), quadcopter.getCameraAngle())));

			var newMat = new Matrix4f(Convert.toMinecraft(q));
			newMat.transpose();
			poseStack.last().pose().multiply(newMat);
		}
	}
}