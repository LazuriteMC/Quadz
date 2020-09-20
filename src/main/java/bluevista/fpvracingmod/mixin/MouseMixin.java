package bluevista.fpvracingmod.mixin;

import bluevista.fpvracingmod.client.ClientTick;
import net.minecraft.client.Mouse;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mouse.class)
public class MouseMixin {
    @Redirect(
            method = "updateMouse",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;changeLookDirection(DD)V"
            )
    )
    public void changeLookDirection(ClientPlayerEntity player, double cursorDeltaX, double cursorDeltaY) {
        if (!ClientTick.isInGoggles()) {
            player.changeLookDirection(cursorDeltaX, cursorDeltaY);
        }
    }

    @Redirect(
            method = "onMouseButton",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/options/KeyBinding;setKeyPressed(Lnet/minecraft/client/util/InputUtil$Key;Z)V"
            )
    )
    public void setKeyPressed(InputUtil.Key key, boolean pressed) {
        if (!ClientTick.isInGoggles()) {
            KeyBinding.setKeyPressed(key, pressed);
        }
    }

    @Redirect(
            method = "onMouseButton",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/options/KeyBinding;onKeyPressed(Lnet/minecraft/client/util/InputUtil$Key;)V"
            )
    )
    public void onKeyPressed(InputUtil.Key key) {
        if (!ClientTick.isInGoggles()) {
            KeyBinding.onKeyPressed(key);
        }
    }

    @Inject(at = @At("HEAD"), method = "wasLeftButtonClicked", cancellable = true)
    public void wasLeftButtonClicked(CallbackInfoReturnable<Boolean> info) {
        if (ClientTick.isInGoggles()) {
            info.setReturnValue(false);
        }
    }

    @Inject(at = @At("HEAD"), method = "wasRightButtonClicked", cancellable = true)
    public void wasRightButtonClicked(CallbackInfoReturnable<Boolean> info) {
        if (ClientTick.isInGoggles()) {
            info.setReturnValue(false);
        }
    }
}