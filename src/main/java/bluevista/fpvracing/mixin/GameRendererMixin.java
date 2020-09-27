package bluevista.fpvracing.mixin;

import bluevista.fpvracing.client.ClientInitializer;
import bluevista.fpvracing.client.RenderTick;
import bluevista.fpvracing.client.input.InputTick;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.math.Quaternion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This mixin class is responsible for changing several behaviors in the {@link GameRenderer}.
 * The render of the player's hand is modified, the rendering of the camera's pitch and yaw are modified,
 * and updating the screens rotation is handled here as well as updating the player's controller input.
 * @author Ethan Johnson
 */
@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Shadow MinecraftClient client;

	/**
	 * Mainly for calling the {@link RenderTick#tick(MinecraftClient, MatrixStack, float)} and
	 * {@link InputTick#tick()} methods. They must run every frame rather than every tick.
	 * @param tickDelta minecraft tick delta
	 * @param limitTime
	 * @param matrix the matrix stack (used in {@link RenderTick#tick(MinecraftClient, MatrixStack, float)} to rotate the screen)
	 * @param info required by every mixin injection
	 */
	@Inject(at = @At("HEAD"), method = "renderWorld")
	public void renderWorld(float tickDelta, long limitTime, MatrixStack matrix, CallbackInfo info) {
		RenderTick.tick(client, matrix, tickDelta);
		InputTick.tick();
	}

	/**
	 * This mixin modifies the renderHand method so that when the player
	 * is flying a drone, the hand is no longer renderer.
	 * @param matrices the matrix stack
	 * @param camera the camera object
	 * @param tickDelta minecraft tick delta
	 * @param info required by every mixin injection
	 */
	@Inject(at = @At("HEAD"), method = "renderHand", cancellable = true)
	public void renderHand(MatrixStack matrices, Camera camera, float tickDelta, CallbackInfo info) {
		if (ClientInitializer.isInGoggles(client)) info.cancel();
	}

	/**
	 * This mixin redirects the {@link MatrixStack#multiply(Quaternion)} method called in
	 * {@link GameRenderer#renderWorld(float, long, MatrixStack)}. If the player is flying
	 * a drone, this method will lock the screens yaw to 0 degrees. Otherwise, it will pass
	 * through like normal.
	 * @param stack the matrix stack
	 * @param quat the quaternion to rotate by
	 */
	@Redirect(
			method = "renderWorld(FJLnet/minecraft/client/util/math/MatrixStack;)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/util/math/MatrixStack;multiply(Lnet/minecraft/util/math/Quaternion;)V",
					ordinal = 3
			)
	)
	public void yaw(MatrixStack stack, Quaternion quat) {
		if (ClientInitializer.isInGoggles(client)) {
			stack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(180));
		} else {
			stack.multiply(quat);
		}
	}

	/**
	 * This mixin redirects the {@link MatrixStack#multiply(Quaternion)} method called in
	 * {@link GameRenderer#renderWorld(float, long, MatrixStack)}. If the player is flying
	 * a drone, this method will lock the screens pitch to 0 degrees. Otherwise, it will pass
	 * through like normal.
	 * @param stack the matrix stack
	 * @param quat the quaternion to rotate by
	 */
	@Redirect(
			method = "renderWorld(FJLnet/minecraft/client/util/math/MatrixStack;)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/util/math/MatrixStack;multiply(Lnet/minecraft/util/math/Quaternion;)V",
					ordinal = 2
			)
	)
	public void pitch(MatrixStack stack, Quaternion quat) {
		if (ClientInitializer.isInGoggles(client)) {
			stack.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(0));
		} else {
			stack.multiply(quat);
		}
	}
}
