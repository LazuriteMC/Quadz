package bluevista.fpvracingmod.mixin;

import bluevista.fpvracingmod.server.entities.DroneEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {
	@Shadow BuiltChunkStorage chunks;
	@Shadow MinecraftClient client;
	@Shadow ClientWorld world;

	int cameraChunkX;
	int cameraChunkY;
	int cameraChunkZ;
	double lastCameraChunkUpdateX;
	double lastCameraChunkUpdateY;
	double lastCameraChunkUpdateZ;

	@Inject(
			at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/render/chunk/ChunkBuilder;setCameraPosition(Lnet/minecraft/util/math/Vec3d;)V",
			ordinal = 0
			),
			method = "setupTerrain"
	)
	public void setupTerrain(Camera camera, Frustum frustum, boolean hasForcedFrustum, int frame, boolean spectator, CallbackInfo info) {
		if(client.getCameraEntity() instanceof DroneEntity) {
			Entity cam = client.getCameraEntity();
			double d = camera.getPos().getX() - lastCameraChunkUpdateX;
			double e = camera.getPos().getY() - lastCameraChunkUpdateY;
			double f = camera.getPos().getZ() - lastCameraChunkUpdateZ;
			if (cameraChunkX != cam.chunkX || cameraChunkY != cam.chunkY || cameraChunkZ != cam.chunkZ || d * d + e * e + f * f > 16.0D) {
				this.world.getProfiler().swap("camera");

				lastCameraChunkUpdateX = camera.getPos().x;
				lastCameraChunkUpdateY = camera.getPos().y;
				lastCameraChunkUpdateZ = camera.getPos().z;

				cameraChunkX = cam.chunkX;
				cameraChunkY = cam.chunkY;
				cameraChunkZ = cam.chunkZ;

				this.chunks.updateCameraPosition(camera.getPos().x, camera.getPos().z);
			}
		}
	}
}
