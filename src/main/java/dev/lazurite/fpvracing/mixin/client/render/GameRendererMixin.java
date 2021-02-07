package dev.lazurite.fpvracing.mixin.client.render;

import dev.lazurite.fpvracing.common.entity.QuadcopterEntity;
import dev.lazurite.rayon.Rayon;
import dev.lazurite.rayon.impl.physics.body.EntityRigidBody;
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
import physics.com.jme3.math.Quaternion;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Shadow @Final private MinecraftClient client;

	@Inject(method = "renderWorld", at = @At("HEAD"))
	public void renderWorld(float tickDelta, long limitTime, MatrixStack matrix, CallbackInfo info) {
		Entity entity = client.getCameraEntity();

		if (entity instanceof QuadcopterEntity) {
			EntityRigidBody body = Rayon.ENTITY.get(entity);

			Quaternion q = body.getPhysicsRotation(new Quaternion(), tickDelta);
			q.set(q.getX(), -q.getY(), q.getZ(), -q.getW());

			/* Camera Angle */
			QuaternionHelper.rotateX(q, ((QuadcopterEntity) entity).getCameraAngle());

			Matrix4f newMat = new Matrix4f(QuaternionHelper.bulletToMinecraft(q));
			newMat.transpose();
			matrix.peek().getModel().multiply(newMat);
		}

//		if (client.player != null && !client.isPaused()) {
//			if (client.getCameraEntity() instanceof FlyableEntity) {
//				FlyableEntity flyable = (FlyableEntity) client.getCameraEntity();
//				float droneFOV = 0; //flyable.getValue(FlyableEntity.FIELD_OF_VIEW);
//
//				if (droneFOV != 0.0f && client.options.fov != droneFOV) {
//					prevFOV = client.options.fov;
//					client.options.fov = droneFOV;
//				}
//
//				if (droneFOV == 0.0f && prevFOV != 0.0f) {
//					client.options.fov = prevFOV;
//					prevFOV = 0.0f;
//				}
//			} else if (prevFOV != 0.0f) {
//				client.options.fov = prevFOV;
//				prevFOV = 0.0f;
//			}
//		}
	}
}