package bluevista.fpvracingmod.mixin;

import bluevista.fpvracingmod.client.ClientInitializer;
import bluevista.fpvracingmod.client.ClientTick;
import bluevista.fpvracingmod.client.RenderTick;
import bluevista.fpvracingmod.client.input.InputTick;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Shadow MinecraftClient client;

	/*
	 * Call the RenderTick#tick and InputTick#tick methods within renderWorld.
	 * The idea is that they will run every frame rather than every game tick.
	 */
	@Inject(at = @At("HEAD"), method = "renderWorld")
	public void renderWorld(MatrixStack matrix) {
		RenderTick.tick(client, matrix);
		InputTick.tick(ClientTick.boundDrone);
	}

	/*
	 * Cancel the method which renders the player's hand if the
	 * player is viewing a drone through goggles.
	 */
	@Inject(at = @At("HEAD"), method = "renderHand", cancellable = true)
	public void renderHand(CallbackInfo info) {
		if(ClientInitializer.isInGoggles(client)) info.cancel();
	}
}
