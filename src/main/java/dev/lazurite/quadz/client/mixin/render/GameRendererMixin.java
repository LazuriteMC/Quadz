package dev.lazurite.quadz.client.mixin.render;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.client.Config;
import dev.lazurite.quadz.common.entity.QuadcopterEntity;
import dev.lazurite.quadz.common.util.type.QuadcopterState;
import dev.lazurite.rayon.core.impl.util.math.QuaternionHelper;
import dev.lazurite.rayon.core.impl.util.math.VectorHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
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
		/* Rotate the entire view to match the view of the quadcopter. */
		if (camera.getFocusedEntity() instanceof QuadcopterEntity) {
			QuadcopterEntity quadcopter = (QuadcopterEntity) camera.getFocusedEntity();

			Quaternion q = quadcopter.getPhysicsRotation(new Quaternion(), tickDelta);
			q.set(q.getX(), -q.getY(), q.getZ(), -q.getW());

			/* Camera Angle */
			q.set(QuaternionHelper.rotateX(q, quadcopter.getCameraAngle()));

			Matrix4f newMat = new Matrix4f(QuaternionHelper.bulletToMinecraft(q));
			newMat.transpose();
			matrix.peek().getModel().multiply(newMat);

		/* Rotate the player's yaw and pitch to follow the quadcopter in the world. */
		} else if (Config.getInstance().followLOS) {
			Quadz.TRANSMITTER_CONTAINER.maybeGet(client.player.getMainHandStack()).ifPresent(transmitter -> {
				QuadcopterEntity quad = QuadcopterState.findQuadcopter(client.world, client.player.getPos(), transmitter.getBindId(), QuadcopterState.RANGE);

				if (quad != null && client.player.canSee(quad)) {
					/* Get the difference in position between the camera and the quad */
					Vec3d delta = client.gameRenderer.getCamera().getPos().subtract(VectorHelper.vector3fToVec3d(quad.getPhysicsLocation(new Vector3f(), tickDelta)));

					/* Set new pitch and yaw */
					client.player.yaw = (float) Math.toDegrees(Math.atan2(delta.z, delta.x)) + 90;
					client.player.pitch = 20 + (float) Math.toDegrees(Math.atan2(delta.y, Math.sqrt(delta.x * delta.x + delta.z * delta.z)));
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

	// TODO optifine goes borkus here

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