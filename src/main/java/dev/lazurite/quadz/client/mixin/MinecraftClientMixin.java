package dev.lazurite.quadz.client.mixin;

import dev.lazurite.quadz.client.input.InputTick;
import dev.lazurite.quadz.common.state.entity.QuadcopterEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftClientMixin {

    @Shadow private ProfilerFiller profiler;

    @Shadow @Final public Options options;

    @Inject(
            method = "runTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/toasts/ToastComponent;render(Lcom/mojang/blaze3d/vertex/PoseStack;)V",
                    shift = At.Shift.AFTER
            )
    )
    public void render(boolean tick, CallbackInfo info) {
        this.profiler.popPush("gamepadInput");
        InputTick.getInstance().tick();
    }

    @Inject(method = "setCameraEntity", at = @At("HEAD"))
    public void setCameraEntity(Entity entity, CallbackInfo ci) {
        if (entity instanceof QuadcopterEntity && options.getCameraType().isMirrored()) {
            options.setCameraType(options.getCameraType().cycle());
        }
    }
}
