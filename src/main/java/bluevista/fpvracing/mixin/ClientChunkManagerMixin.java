package bluevista.fpvracing.mixin;

import bluevista.fpvracing.client.ClientInitializer;
import bluevista.fpvracing.client.ClientTick;
import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * This mixin class mainly deals with entity ticking on the client.
 * @author Ethan Johnson
 */
@Mixin(ClientChunkManager.class)
public class ClientChunkManagerMixin {
    /**
     * This mixin is written to ensure that a client-sided drone is able to tick.
     * @param entity
     * @param info required by every mixin injection
     */
    @Inject(at = @At("HEAD"), method = "shouldTickEntity", cancellable = true)
    public void shouldTickEntity(Entity entity, CallbackInfoReturnable<Boolean> info) {
        if (!ClientTick.isServerModded && entity == ClientInitializer.client.getCameraEntity()) {
            info.setReturnValue(true);
        }
    }
}
