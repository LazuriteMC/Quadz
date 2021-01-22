package dev.lazurite.fpvracing.mixin.client;

import dev.lazurite.fpvracing.client.input.InputTick;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Shadow private Profiler profiler;

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/toast/ToastManager;draw(Lnet/minecraft/client/util/math/MatrixStack;)V",
                    shift = At.Shift.AFTER
            )
    )
    public void render(boolean tick, CallbackInfo info) {
        this.profiler.swap("gamepadInput");
        InputTick.getInstance().tick();
    }
}
