package bluevista.fpvracingmod.mixin;

import bluevista.fpvracingmod.client.ClientInitializer;
import bluevista.fpvracingmod.client.RenderTick;
import bluevista.fpvracingmod.client.input.InputTick;
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

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Shadow MinecraftClient client;

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

	/*
	 * Call the RenderTick#tick and InputTick#tick methods within renderWorld.
	 * The idea is that they will run every frame rather than every game tick.
	 */
	@Inject(at = @At("HEAD"), method = "renderWorld")
	public void renderWorld(float tickDelta, long limitTime, MatrixStack matrix, CallbackInfo info) {
		RenderTick.tick(client, matrix, tickDelta);
		InputTick.tick();
	}

	/*
	 * Cancel the method which renders the player's hand if the
	 * player is viewing a drone through goggles.
	 */
	@Inject(at = @At("HEAD"), method = "renderHand", cancellable = true)
	public void renderHand(MatrixStack matrices, Camera camera, float tickDelta, CallbackInfo info) {
		if (ClientInitializer.isInGoggles(client)) info.cancel();
	}
}
