package dev.lazurite.quadz.client.mixin.render;

import dev.lazurite.quadz.client.Config;
import dev.lazurite.quadz.client.render.ui.osd.OnScreenDisplay;
import dev.lazurite.quadz.common.state.entity.QuadcopterEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Shadow @Final private MinecraftClient client;
    @Shadow public abstract TextRenderer getFontRenderer();

    /**
     * Render the OSD whenever the player views a{@link QuadcopterEntity}
     * through their goggles.
     * @param matrices
     * @param tickDelta
     * @param info
     */
    @Inject(method = "render", at = @At("HEAD"))
    public void render(MatrixStack matrices, float tickDelta, CallbackInfo info) {
        if (Config.getInstance().osdEnabled && client.getCameraEntity() instanceof QuadcopterEntity) {
            OnScreenDisplay.render((QuadcopterEntity) client.getCameraEntity(), getFontRenderer(), matrices, tickDelta);
        }
    }

    /**
     * This mixin method removes the crosshair from the
     * player's screen while they are using goggles.
     * @param matrices
     * @param info required by every mixin injection
     */
    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void renderCrosshair(MatrixStack matrices, CallbackInfo info) {
        if (client.getCameraEntity() instanceof QuadcopterEntity) {
            info.cancel();
        }
    }

    /**
     * This mixin method removes the experience bar from the player's screen
     * when they're in adventure or survival mode whilst using goggles.
     * @param matrices
     * @param x
     * @param info required by every mixin injection
     */
    @Inject(method = "renderExperienceBar", at = @At("HEAD"), cancellable = true)
    public void renderExperienceBar(MatrixStack matrices, int x, CallbackInfo info) {
        if (client.getCameraEntity() instanceof QuadcopterEntity) {
            info.cancel();
        }
    }
}
