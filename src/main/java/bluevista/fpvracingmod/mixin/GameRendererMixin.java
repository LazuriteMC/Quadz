package bluevista.fpvracingmod.mixin;

import bluevista.fpvracingmod.client.ClientTick;
import bluevista.fpvracingmod.client.RenderTick;
import bluevista.fpvracingmod.client.input.InputTick;
import bluevista.fpvracingmod.server.items.TransmitterItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
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
	 * Call the RenderTick.tick method within renderWorld. The idea is that
	 * RenderTick.tick will run every frame rather than every game tick.
	 */
	@Inject(at = @At("HEAD"), method = "renderWorld")
	public void renderWorld(float tickDelta, long limitTime, MatrixStack matrix, CallbackInfo info) {
		RenderTick.tick(client, matrix, tickDelta);
		InputTick.tick(TransmitterItem.droneFromTransmitter(client.player.getMainHandStack(), client.player));
	}

	/*
	 * Cancel the method which renders the player's hand if the
	 * player is viewing a drone through goggles.
	 */
	@Inject(at = @At("HEAD"), method = "renderHand", cancellable = true)
	public void renderHand(MatrixStack matrices, Camera camera, float tickDelta, CallbackInfo info) {
		if(ClientTick.isInView(client)) info.cancel();
	}
}
