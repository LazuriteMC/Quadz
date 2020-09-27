package bluevista.fpvracing.mixin;

import bluevista.fpvracing.client.ClientTick;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {
    @Inject(at = @At("HEAD"), method = "move", cancellable = true)
    public void move(MovementType type, Vec3d movement, CallbackInfo info) {
        if (ClientTick.isInGoggles()) {
            info.cancel();
        }
    }
}
