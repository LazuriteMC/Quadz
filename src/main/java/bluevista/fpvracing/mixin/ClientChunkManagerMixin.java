package bluevista.fpvracing.mixin;

import bluevista.fpvracing.client.ClientInitializer;
import bluevista.fpvracing.client.ClientTick;
import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientChunkManager.class)
public class ClientChunkManagerMixin {
    @Inject(at = @At("HEAD"), method = "shouldTickEntity", cancellable = true)
    public void shouldTickEntity(Entity entity, CallbackInfoReturnable<Boolean> info) {
        if (!ClientTick.isServerModded && entity == ClientInitializer.client.getCameraEntity()) {
            info.setReturnValue(true);
        }
    }
}
