package bluevista.fpvracingmod.mixin;

import bluevista.fpvracingmod.server.entities.DroneEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin {
    @Shadow @Final MinecraftClient client;

//    @Inject(at = @At("HEAD"), method = "doRandomBlockDisplayTicks", cancellable = true)
//    public void doRandomBlockDisplayTicks(int xCenter, int yCenter, int zCenter, CallbackInfo info) {
//        Camera cam = client.gameRenderer.getCamera();
//        int camX = MathHelper.floor(cam.getPos().getX());
//        int camY = MathHelper.floor(cam.getPos().getY());
//        int camZ = MathHelper.floor(cam.getPos().getZ());
//
//        int pX = MathHelper.floor(client.player.getX());
//        int pY = MathHelper.floor(client.player.getY());
//        int pZ = MathHelper.floor(client.player.getZ());
//        if(client.getCameraEntity() instanceof DroneEntity && xCenter == pX && yCenter == pY && zCenter == pZ) {
//            System.out.println("FINNA CANCEL");
//            client.world.doRandomBlockDisplayTicks(camX, camY, camZ);
//            info.cancel();
//        }
//    }
}
