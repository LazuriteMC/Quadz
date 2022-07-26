package dev.lazurite.quadz.client.mixin.render;

import com.jme3.math.Quaternion;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import dev.lazurite.quadz.common.data.Config;
import dev.lazurite.quadz.common.data.model.Bindable;
import dev.lazurite.quadz.common.entity.QuadcopterEntity;
import dev.lazurite.quadz.common.entity.RemoteControllableEntity;
import dev.lazurite.rayon.impl.bullet.math.Convert;
import dev.lazurite.toolbox.api.math.QuaternionHelper;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This mixin is mainly for manipulating the player's camera (i.e. rotating it according to the quadcopter's view).
 */
@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Shadow @Final private Minecraft minecraft;
	@Shadow @Final private Camera mainCamera;

	@Inject(method = "renderLevel", at = @At("HEAD"))
	public void renderLevel_HEAD(float f, long l, PoseStack poseStack, CallbackInfo ci) {

		// Quadz specific
		/* Rotate the entire view to match the view of the quadcopter */
		if (mainCamera.getEntity() instanceof QuadcopterEntity quadcopter) {
			var q = quadcopter.getPhysicsRotation(new Quaternion(), f);
			q.set(q.getX(), -q.getY(), q.getZ(), -q.getW());
			q.set(Convert.toBullet(QuaternionHelper.rotateX(Convert.toMinecraft(q), quadcopter.getCameraAngle())));

			var newMat = new Matrix4f(Convert.toMinecraft(q));
			newMat.transpose();
			poseStack.last().pose().multiply(newMat);

		/* Rotate the player's yaw and pitch to follow the quadcopter */
		} else if (Config.followLOS) {
			Bindable.get(minecraft.player.getMainHandItem()).ifPresent(transmitter -> {
				for (var entity : minecraft.level.getEntities().getAll()) {
					if (entity instanceof RemoteControllableEntity remoteControllableEntity && remoteControllableEntity.isBoundTo(transmitter) && minecraft.player.hasLineOfSight(remoteControllableEntity)) {
						/* Get the difference in position between the camera and the quad */
						var delta = minecraft.gameRenderer.getMainCamera().getPosition().subtract(remoteControllableEntity.position());

						/* Set new pitch and yaw */
						minecraft.player.setYRot((float) Math.toDegrees(Math.atan2(delta.z, delta.x)) + 90);
						minecraft.player.setXRot(20 + (float) Math.toDegrees(Math.atan2(delta.y, Math.sqrt(delta.x * delta.x + delta.z * delta.z))));
					}
				}
			});
		}
	}

	@Inject(method = "renderItemInHand", at = @At("HEAD"), cancellable = true)
	private void renderItemInHand_HEAD(PoseStack poseStack, Camera camera, float f, CallbackInfo info) {
		if (camera.getEntity() instanceof RemoteControllableEntity) {
			info.cancel();
		}
	}

	@Redirect(method = "getFov", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;"))
	public Object getFov_FIELD(OptionInstance<Integer> optionInstance) {
		if (minecraft.getCameraEntity() instanceof RemoteControllableEntity && Config.firstPersonFOV > 30) {
			return Config.firstPersonFOV;
		}

		return optionInstance.get();
	}
}