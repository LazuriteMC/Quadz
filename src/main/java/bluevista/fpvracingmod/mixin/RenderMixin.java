package bluevista.fpvracingmod.mixin;

import bluevista.fpvracingmod.client.handler.RenderHandler;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class RenderMixin {
	@Inject(at = @At("HEAD"), method = "renderWorld")
	public void renderWorld(float tickDelta, long limitTime, MatrixStack matrix, CallbackInfo info) {
		RenderHandler.tick(matrix, tickDelta);
	}
}
