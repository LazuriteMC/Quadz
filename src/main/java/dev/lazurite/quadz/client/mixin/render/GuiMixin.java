package dev.lazurite.quadz.client.mixin.render;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.lazurite.quadz.client.render.ui.osd.OnScreenDisplay;
import dev.lazurite.quadz.common.data.Config;
import dev.lazurite.quadz.common.entity.QuadcopterEntity;
import dev.lazurite.quadz.common.entity.RemoteControllableEntity;
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
public abstract class GuiMixin {
    @Shadow @Final private Minecraft minecraft;
    @Shadow public abstract Font getFont();

    /**
     * Render the OSD whenever the player views a {@link QuadcopterEntity}
     * through their goggles.
     * @param poseStack
     * @param delta
     * @param info
     */
    @Inject(method = "render", at = @At("HEAD"))
    public void render_HEAD(PoseStack poseStack, float delta, CallbackInfo info) {
        // Quadz specific
        if (Config.osdEnabled && minecraft.getCameraEntity() instanceof QuadcopterEntity quadcopter) {
            OnScreenDisplay.render(quadcopter, getFont(), poseStack, delta);
        }
    }

    /**
     * This mixin method removes the crosshair from the
     * player's screen while they are using goggles.
     * @param poseStack
     * @param info required by every mixin injection
     */
    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void renderCrosshair_HEAD(PoseStack poseStack, CallbackInfo info) {
        if (minecraft.getCameraEntity() instanceof RemoteControllableEntity) {
            info.cancel();
        }
    }

    /**
     * This mixin method removes the experience bar from the player's screen
     * when they're in adventure or survival mode whilst using goggles.
     * @param poseStack
     * @param i
     * @param info required by every mixin injection
     */
    @Inject(method = "renderExperienceBar", at = @At("HEAD"), cancellable = true)
    public void renderExperienceBar_HEAD(PoseStack poseStack, int i, CallbackInfo info) {
        if (minecraft.getCameraEntity() instanceof RemoteControllableEntity) {
            info.cancel();
        }
    }
}
