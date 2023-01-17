package dev.lazurite.quadz.client.mixin;

import dev.lazurite.quadz.client.render.screen.ScreenHooks;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {

    @Inject(method = "init", at = @At("TAIL"))
    public void init(CallbackInfo ci) {
        ScreenHooks.addQuadzButtonToTitleScreen((TitleScreen) (Object) this);
    }

}
