package bluevista.fpvracingmod.mixin;

import bluevista.fpvracingmod.client.ClientTick;
import bluevista.fpvracingmod.client.RenderTick;
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

	@Inject(at = @At("HEAD"), method = "renderWorld")
	public void renderWorld(float tickDelta, long limitTime, MatrixStack matrix, CallbackInfo info) {
		RenderTick.tick(matrix, tickDelta);
		if (!ClientTick.shouldRender() && client.player != null) {
			client.player.setPos(ClientTick.view.getX(), ClientTick.view.getY(), ClientTick.view.getZ());
		}
	}
}
