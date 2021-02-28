package dev.lazurite.quadz.client.mixin;

import dev.lazurite.quadz.client.input.InputTick;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Shadow @Final public GameRenderer gameRenderer;
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

    @ModifyArgs(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;doRandomBlockDisplayTicks(III)V"))
    public void doRandomBlockDisplayTicks(Args args) {
        Camera cam = gameRenderer.getCamera();
        args.set(0, (int) cam.getPos().getX());
        args.set(0, (int) cam.getPos().getY());
        args.set(0, (int) cam.getPos().getZ());
    }
}
