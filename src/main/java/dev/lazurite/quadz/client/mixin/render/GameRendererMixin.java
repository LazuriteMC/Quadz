package dev.lazurite.quadz.client.mixin.render;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import dev.lazurite.quadz.client.Config;
import dev.lazurite.quadz.common.state.Bindable;
import dev.lazurite.quadz.common.state.entity.QuadcopterEntity;
import dev.lazurite.rayon.core.impl.bullet.math.Converter;
import dev.lazurite.toolbox.api.math.QuaternionHelper;
import dev.lazurite.toolbox.api.math.VectorHelper;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
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
	public void renderWorld(float f, long l, PoseStack poseStack, CallbackInfo ci) {
		/* Rotate the entire view to match the view of the quadcopter */
		if (mainCamera.getEntity() instanceof QuadcopterEntity quadcopter) {
			var q = quadcopter.getPhysicsRotation(new Quaternion(), f);
			q.set(q.getX(), -q.getY(), q.getZ(), -q.getW());
			QuaternionHelper.rotateX(Converter.toMinecraft(q), quadcopter.getCameraAngle());

			var newMat = new Matrix4f(Converter.toMinecraft(q));
			newMat.transpose();
			poseStack.last().pose().multiply(newMat);

		/* Rotate the player's yaw and pitch to follow the quadcopter */
		} else if (Config.getInstance().followLOS) {
			Bindable.get(minecraft.player.getMainHandItem()).ifPresent(transmitter -> {
				for (Entity entity : minecraft.level.getEntities()) { // TODO: Access restricted :(
					if (entity instanceof QuadcopterEntity && ((QuadcopterEntity) entity).isBoundTo(transmitter) && minecraft.player.hasLineOfSight(entity)) {
						/* Get the difference in position between the camera and the quad */
						var delta = minecraft.gameRenderer.getMainCamera().getPosition().subtract(VectorHelper.toVec3d(Converter.toMinecraft(((QuadcopterEntity) entity).getPhysicsLocation(new Vector3f(), f))));

						/* Set new pitch and yaw */
						minecraft.player.setYRot((float) Math.toDegrees(Math.atan2(delta.z, delta.x)) + 90);
						minecraft.player.setXRot(20 + (float) Math.toDegrees(Math.atan2(delta.y, Math.sqrt(delta.x * delta.x + delta.z * delta.z))));
					}
				}
			});
		}
	}

	@Inject(method = "renderItemInHand", at = @At("HEAD"), cancellable = true)
	private void renderHand(PoseStack poseStack, Camera camera, float f, CallbackInfo info) {
		if (camera.getEntity() instanceof QuadcopterEntity) {
			info.cancel();
		}
	}

	@Redirect(method = "getFov", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Options;fov:D"))
	public double getFov(Options options) {
		if (minecraft.getCameraEntity() instanceof QuadcopterEntity && Config.getInstance().firstPersonFOV > 30) {
			return Config.getInstance().firstPersonFOV;
		}

		return options.fov;
	}
}