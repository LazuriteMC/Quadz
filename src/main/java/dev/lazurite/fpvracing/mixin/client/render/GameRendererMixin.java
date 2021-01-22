package dev.lazurite.fpvracing.mixin.client.render;

import dev.lazurite.fpvracing.client.input.InputTick;
import dev.lazurite.fpvracing.common.entity.QuadcopterEntity;
import dev.lazurite.rayon.physics.body.EntityRigidBody;
import dev.lazurite.rayon.physics.helper.math.QuaternionHelper;
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
import physics.javax.vecmath.Quat4f;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Shadow @Final private MinecraftClient client;

	/**
	 * Updates the user's controller input. Calling {@link InputTick#tick}
	 * here allows for input to be available in the menus as well as
	 * in-game for configuration purposes.
	 * @param tickDelta
	 * @param startTime
	 * @param tick
	 * @param info
	 */
	@Inject(method = "render", at = @At("HEAD"))
	public void render(float tickDelta, long startTime, boolean tick, CallbackInfo info) {
		if (!client.isPaused()) {
			InputTick.getInstance().tick();
		}
	}

	@Inject(method = "renderWorld", at = @At("HEAD"))
	public void renderWorld(float tickDelta, long limitTime, MatrixStack matrix, CallbackInfo info) {
		Entity entity = client.getCameraEntity();

		if (entity instanceof QuadcopterEntity) {
			EntityRigidBody body = EntityRigidBody.get(entity);

			Quat4f q = QuaternionHelper.slerp(body.getPrevOrientation(new Quat4f()), body.getTickOrientation(new Quat4f()), tickDelta);
			q.set(q.x, -q.y, q.z, -q.w);

			/* Camera Angle */
			QuaternionHelper.rotateX(q, ((QuadcopterEntity) entity).getCameraAngle());

			Matrix4f newMat = new Matrix4f(QuaternionHelper.quat4fToQuaternion(q));
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