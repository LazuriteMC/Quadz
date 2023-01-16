package dev.lazurite.quadz.client.mixin;

import dev.lazurite.quadz.client.render.screen.ScreenHooks;
import net.minecraft.client.gui.screens.PauseScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PauseScreen.class)
public class PauseScreenMixin {

    @Inject(method = "createPauseMenu", at = @At("TAIL"))
    public void createPauseScreen$TAIL(CallbackInfo ci) {
        ScreenHooks.addQuadzButton((PauseScreen) (Object) this);
    }

}
