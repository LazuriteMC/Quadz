package dev.lazurite.quadz.client.mixin;

import dev.lazurite.quadz.client.render.screen.ScreenHooks;
import net.minecraft.client.gui.components.GridWidget;
import net.minecraft.client.gui.screens.PauseScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(PauseScreen.class)
public class PauseScreenMixin {

    @Inject(method = "createPauseMenu", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
    public void createPauseScreen$TAIL(CallbackInfo ci, GridWidget gridWidget, GridWidget.RowHelper rowHelper) {
        ScreenHooks.addQuadzButton((PauseScreen) (Object) this, gridWidget, rowHelper);
    }

}
