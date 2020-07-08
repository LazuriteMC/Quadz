package bluevista.fpvracingmod.mixin;

import bluevista.fpvracingmod.server.entities.DroneEntity;
import bluevista.fpvracingmod.server.entities.ViewHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public abstract class IsMainPlayerMixin {

    @Shadow @Final protected MinecraftClient client;

    @Inject(method = "isMainPlayer()Z", at = @At("HEAD"), cancellable = true)
    public void isMainPlayer(CallbackInfoReturnable<Boolean> info) {
        if (client.getCameraEntity() instanceof ViewHandler) {
            info.setReturnValue(false);
            info.cancel();
        }
    }

}

