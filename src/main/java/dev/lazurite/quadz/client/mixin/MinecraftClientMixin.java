package dev.lazurite.quadz.client.mixin;

import dev.lazurite.quadz.client.input.InputTick;
import dev.lazurite.quadz.common.state.entity.QuadcopterEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.GameOptions;
import net.minecraft.entity.Entity;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Shadow @Final public GameOptions options;
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

    @Inject(method = "setCameraEntity", at = @At("HEAD"))
    public void setCameraEntity(Entity entity, CallbackInfo info) {
        if (entity instanceof QuadcopterEntity && options.getPerspective().isFrontView()) {
            options.setPerspective(options.getPerspective().next());
        }
    }
}
