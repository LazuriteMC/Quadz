package bluevista.fpvracingmod.mixin;

import bluevista.fpvracingmod.server.entities.DroneEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BuiltChunkStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BuiltChunkStorage.class)
public abstract class BuiltChunkStorageMixin {
//    @Inject(at = @At("HEAD"), method = "updateCameraPosition", cancellable = true)
//    public void updateCameraPosition(double x, double z, CallbackInfo info) {
//        MinecraftClient client = MinecraftClient.getInstance();
//        if(client.getCameraEntity() instanceof DroneEntity && client.player.getX() == x && client.player.getZ() == z)
//            info.cancel();
//    }
}
