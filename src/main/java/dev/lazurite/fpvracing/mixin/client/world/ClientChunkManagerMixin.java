package dev.lazurite.fpvracing.mixin.client.world;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * This mixin ensures that the drone is able to tick when on the client.
 */
@Mixin(ClientChunkManager.class)
public class ClientChunkManagerMixin {
    @Inject(at = @At("HEAD"), method = "shouldTickEntity", cancellable = true)
    public void shouldTickEntity(Entity entity, CallbackInfoReturnable<Boolean> info) {
        if (entity == MinecraftClient.getInstance().getCameraEntity()) {
            info.setReturnValue(true);
        }
    }
}
