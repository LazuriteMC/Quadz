package dev.lazurite.fpvracing.mixin.client.render;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import dev.lazurite.fpvracing.FPVRacing;
import dev.lazurite.fpvracing.client.Config;
import dev.lazurite.fpvracing.common.entity.QuadcopterEntity;
import dev.lazurite.fpvracing.common.util.type.QuadcopterState;
import dev.lazurite.rayon.impl.util.math.QuaternionHelper;
import dev.lazurite.rayon.impl.util.math.VectorHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This mixin is mainly for manipulating the player's camera.
 */
@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Shadow @Final private MinecraftClient client;
	@Shadow private boolean renderHand;

	@Inject(method = "renderWorld", at = @At("HEAD"))
	public void renderWorld(float tickDelta, long limitTime, MatrixStack matrix, CallbackInfo info) {
		Entity cameraEntity = client.getCameraEntity();
		this.renderHand = !(cameraEntity instanceof QuadcopterEntity);

		/* Rotate the entire view to match the view of the quadcopter. */
		if (cameraEntity instanceof QuadcopterEntity) {
			QuadcopterEntity quadcopter = (QuadcopterEntity) cameraEntity;

			Quaternion q = quadcopter.getPhysicsRotation(new Quaternion(), tickDelta);
			q.set(q.getX(), -q.getY(), q.getZ(), -q.getW());

			/* Camera Angle */
			q.set(QuaternionHelper.rotateX(q, ((QuadcopterEntity) cameraEntity).getCameraAngle()));

			Matrix4f newMat = new Matrix4f(QuaternionHelper.bulletToMinecraft(q));
			newMat.transpose();
			matrix.peek().getModel().multiply(newMat);

			Camera camera = client.gameRenderer.getCamera();
			matrix.multiply(QuaternionHelper.bulletToMinecraft(QuaternionHelper.rotateY(new Quaternion(), camera.getYaw()).inverse()));
			matrix.multiply(QuaternionHelper.bulletToMinecraft(QuaternionHelper.rotateX(new Quaternion(), camera.getPitch()).inverse()));

		/* Rotate the player's yaw and pitch to follow the quadcopter in the world. */
		} else if (Config.getInstance().followLOS && FPVRacing.TRANSMITTER_CONTAINER.maybeGet(client.player.getMainHandStack()).isPresent()) {
			QuadcopterEntity quad = QuadcopterState.findQuadcopter(
					client.world,
					client.player.getPos(),
					FPVRacing.TRANSMITTER_CONTAINER.get(client.player.getMainHandStack()).getBindId(),
					100);

			if (quad != null) {
				/* Get the difference in position between the camera and the quad */
				Vec3d delta = client.gameRenderer.getCamera().getPos().subtract(VectorHelper.vector3fToVec3d(quad.getPhysicsLocation(new Vector3f(), tickDelta)));

				/* Set new pitch and yaw */
				client.player.yaw = (float) Math.toDegrees(Math.atan2(delta.z, delta.x)) + 90;
				client.player.pitch = 20 + (float) Math.toDegrees(Math.atan2(delta.y, Math.sqrt(delta.x * delta.x + delta.z * delta.z)));
			}
		}
	}
}