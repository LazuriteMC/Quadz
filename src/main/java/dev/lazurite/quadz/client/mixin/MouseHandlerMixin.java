package dev.lazurite.quadz.client.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import dev.lazurite.quadz.client.hooks.MouseHooks;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {

    @Inject(method = "onMove", at = @At(value = "HEAD"), cancellable = true)
    public void turnPlayer(long l, double d, double e, CallbackInfo info) {
        if (MouseHooks.cancelTurnPlayer()) {
            info.cancel();
        }
    }

    @Redirect(
            method = "onPress",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/KeyMapping;set(Lcom/mojang/blaze3d/platform/InputConstants$Key;Z)V"
            )
    )
    public void onPress$set(InputConstants.Key key, boolean pressed) {
        MouseHooks.onSet(key, pressed);
    }

    @Redirect(
            method = "onPress",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/KeyMapping;click(Lcom/mojang/blaze3d/platform/InputConstants$Key;)V"
            )
    )
    public void onPress$click(InputConstants.Key key) {
        MouseHooks.onClick(key);
    }

}
