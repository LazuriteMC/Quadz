package dev.lazurite.quadz.client.mixin.render;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.lazurite.quadz.client.Config;
import dev.lazurite.quadz.client.render.ui.osd.OnScreenDisplay;
import dev.lazurite.quadz.common.state.entity.QuadcopterEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class InGameHudMixin {
    @Shadow @Final private Minecraft minecraft;
    @Shadow public abstract Font getFont();

    /**
     * Render the OSD whenever the player views a{@link QuadcopterEntity}
     * through their goggles.
     * @param matrices
     * @param tickDelta
     * @param info
     */
    @Inject(method = "render", at = @At("HEAD"))
    public void render(PoseStack poseStack, float f, CallbackInfo ci) {
        if (Config.getInstance().osdEnabled && minecraft.getCameraEntity() instanceof QuadcopterEntity) {
            OnScreenDisplay.render((QuadcopterEntity) minecraft.getCameraEntity(), getFont(), poseStack, f);
        }
    }

    /**
     * This mixin method removes the crosshair from the
     * player's screen while they are using goggles.
     * @param matrices
     * @param info required by every mixin injection
     */
    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void renderCrosshair(PoseStack poseStack, CallbackInfo info) {
        if (minecraft.getCameraEntity() instanceof QuadcopterEntity) {
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
    public void renderExperienceBar(PoseStack poseStack, int i, CallbackInfo info) {
        if (minecraft.getCameraEntity() instanceof QuadcopterEntity) {
            info.cancel();
        }
    }
}
