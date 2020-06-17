package bluevista.fpvracingmod.mixin;

import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public class StupidMixin {
    @Inject(at = @At("HEAD"), method = "getLight", cancellable = true)
    public int getLight(Entity entity, float tickDelta, CallbackInfoReturnable info) {
        if(entity.world == null) {
            info.setReturnValue(100);
        }
        return 100;
    }
}
