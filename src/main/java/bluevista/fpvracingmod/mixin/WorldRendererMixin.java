package bluevista.fpvracingmod.mixin;

import bluevista.fpvracingmod.client.ClientTick;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
	@Shadow
	MinecraftClient client;

//	@Inject(at = @At("HEAD"), method = "render")
//	public void render(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo info) {
//		if (!ClientTick.shouldRender() && client.player != null) {
//			client.player.setPos(ClientTick.view.getX(), ClientTick.view.getY(), ClientTick.view.getZ());
//			System.out.println(client.player.getPos());
//		}
//	}

	@Inject(at = @At("HEAD"), method = "render", cancellable = true)
	public void render(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo info) {
		if (!ClientTick.shouldRender() && client.player != null) {
//			info.cancel();
//			client.player.setPos(ClientTick.view.getX(), ClientTick.view.getY(), ClientTick.view.getZ());
//			System.out.println(client.player.getPos());
		}
	}
}
