package dev.lazurite.quadz.client.mixin.input;

import dev.lazurite.quadz.client.util.ClientTick;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin extends Input {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void tick(CallbackInfo info) {
        if (ClientTick.isUsingKeyboard) {
            movementForward = 0.0f;
            movementSideways = 0.0f;
            jumping = false;
            sneaking = false;
            pressingForward = false;
            pressingBack = false;
            pressingLeft = false;
            pressingRight = false;
            info.cancel();
        }
    }
}
