package dev.lazurite.fpvracing.mixin.client.render;

import com.jme3.math.Quaternion;
import dev.lazurite.fpvracing.common.entity.QuadcopterEntity;
import dev.lazurite.rayon.impl.util.math.QuaternionHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Shadow @Final private MinecraftClient client;
	@Shadow private boolean renderHand;

	@Inject(method = "renderWorld", at = @At("HEAD"))
	public void renderWorld(float tickDelta, long limitTime, MatrixStack matrix, CallbackInfo info) {
		Entity entity = client.getCameraEntity();
		this.renderHand = !(entity instanceof QuadcopterEntity);

		if (entity instanceof QuadcopterEntity) {
			QuadcopterEntity quadcopter = (QuadcopterEntity) entity;

			Quaternion q = quadcopter.getPhysicsRotation(new Quaternion(), tickDelta);
			q.set(q.getX(), -q.getY(), q.getZ(), -q.getW());

			/* Camera Angle */
			q.set(QuaternionHelper.rotateX(q, ((QuadcopterEntity) entity).getCameraAngle()));

			Matrix4f newMat = new Matrix4f(QuaternionHelper.bulletToMinecraft(q));
			newMat.transpose();
			matrix.peek().getModel().multiply(newMat);
		}
	}
}