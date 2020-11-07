package dev.lazurite.fpvracing.mixin;

import dev.lazurite.fpvracing.client.RenderTick;
import dev.lazurite.fpvracing.client.input.InputTick;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This mixin class is responsible for changing several behaviors in the {@link GameRenderer}.
 * The render of the player's hand is modified, the rendering of the camera's pitch and yaw are modified,
 * and updating the screens rotation is handled here as well as updating the player's controller input.
 * @author Ethan Johnson
 */
@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Shadow @Final private MinecraftClient client;

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
}
