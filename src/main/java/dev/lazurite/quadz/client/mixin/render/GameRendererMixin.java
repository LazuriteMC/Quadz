package dev.lazurite.quadz.client.mixin.render;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import dev.lazurite.quadz.client.Config;
import dev.lazurite.quadz.common.state.Bindable;
import dev.lazurite.quadz.common.state.entity.QuadcopterEntity;
import dev.lazurite.rayon.core.impl.util.math.QuaternionHelper;
import dev.lazurite.rayon.core.impl.util.math.VectorHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.GameOptions;
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
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This mixin is mainly for manipulating the player's camera.
 */
@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Shadow @Final private MinecraftClient client;
	@Shadow @Final private Camera camera;

	@Inject(method = "renderWorld", at = @At("HEAD"))
	public void renderWorld(float tickDelta, long limitTime, MatrixStack matrix, CallbackInfo info) {
		/* Rotate the entire view to match the view of the quadcopter */
		if (camera.getFocusedEntity() instanceof QuadcopterEntity) {
			QuadcopterEntity quadcopter = (QuadcopterEntity) camera.getFocusedEntity();

			Quaternion q = quadcopter.getPhysicsRotation(new Quaternion(), tickDelta);
			q.set(q.getX(), -q.getY(), q.getZ(), -q.getW());

			/* Third Person */
			if (!client.options.getPerspective().isFirstPerson()) {
				if (client.options.getPerspective().isFrontView()) {
					QuaternionHelper.rotateY(q, 180);
					QuaternionHelper.rotateX(q, -quadcopter.getCameraAngle());
				} else {
					QuaternionHelper.rotateX(q, quadcopter.getCameraAngle());
				}

				QuaternionHelper.rotateX(q, -Config.getInstance().thirdPersonAngle);
			} else {
				QuaternionHelper.rotateX(q, quadcopter.getCameraAngle());
			}

			Matrix4f newMat = new Matrix4f(QuaternionHelper.bulletToMinecraft(q));
			newMat.transpose();
			matrix.peek().getModel().multiply(newMat);

		/* Rotate the player's yaw and pitch to follow the quadcopter */
		} else if (Config.getInstance().followLOS) {
			Bindable.get(client.player.getMainHandStack()).ifPresent(transmitter -> {
				for (Entity entity : client.world.getEntities()) {
					if (entity instanceof QuadcopterEntity && ((QuadcopterEntity) entity).isBoundTo(transmitter) && client.player.canSee(entity)) {
						/* Get the difference in position between the camera and the quad */
						Vec3d delta = client.gameRenderer.getCamera().getPos().subtract(VectorHelper.vector3fToVec3d(((QuadcopterEntity) entity).getPhysicsLocation(new Vector3f(), tickDelta)));

						/* Set new pitch and yaw */
						client.player.yaw = (float) Math.toDegrees(Math.atan2(delta.z, delta.x)) + 90;
						client.player.pitch = 20 + (float) Math.toDegrees(Math.atan2(delta.y, Math.sqrt(delta.x * delta.x + delta.z * delta.z)));
					}
				}
			});
		}
	}

	@Inject(method = "renderHand", at = @At("HEAD"), cancellable = true)
	private void renderHand(MatrixStack matrices, Camera camera, float tickDelta, CallbackInfo info) {
		if (camera.getFocusedEntity() instanceof QuadcopterEntity) {
			info.cancel();
		}
	}

	@Redirect(
			method = "getFov",
			at = @At(
					value = "FIELD",
					target = "Lnet/minecraft/client/options/GameOptions;fov:D"
			)
	)
	public double getFov(GameOptions options) {
		if (client.getCameraEntity() instanceof QuadcopterEntity && Config.getInstance().firstPersonFOV > 30) {
			return Config.getInstance().firstPersonFOV;
		}

		return options.fov;
	}
}