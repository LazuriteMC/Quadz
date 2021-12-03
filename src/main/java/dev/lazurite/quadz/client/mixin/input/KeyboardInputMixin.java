package dev.lazurite.quadz.client.mixin.input;

import dev.lazurite.quadz.client.util.ClientTick;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin extends Input {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void tick_HEAD(CallbackInfo info) {
        if (ClientTick.isUsingKeyboard) {
            forwardImpulse = 0.0f;
            leftImpulse = 0.0f;
            jumping = false;
            shiftKeyDown = false;
            up = false;
            down = false;
            left = false;
            right = false;
            info.cancel();
        }
    }
}
