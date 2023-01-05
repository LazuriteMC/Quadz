package dev.lazurite.quadz.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.lazurite.quadz.client.hooks.RenderHooks;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {

    @Shadow public abstract Font getFont();

    @Inject(method = "render", at = @At("HEAD"))
    public void render$HEAD(PoseStack poseStack, float delta, CallbackInfo info) {
        RenderHooks.onRenderGui(getFont(), poseStack, delta);
    }

    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void renderCrosshair_HEAD(PoseStack poseStack, CallbackInfo info) {
        if (RenderHooks.isCameraQuadcopter()) {
            info.cancel();
        }
    }

    @Inject(method = "renderExperienceBar", at = @At("HEAD"), cancellable = true)
    public void renderExperienceBar_HEAD(PoseStack poseStack, int i, CallbackInfo info) {
        if (RenderHooks.isCameraQuadcopter()) {
            info.cancel();
        }
    }

}
