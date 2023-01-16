package dev.lazurite.quadz.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.lazurite.quadz.client.QuadzClient;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {

    @Inject(method = "render", at = @At("TAIL"))
    public void render$TAIL(PoseStack poseStack, float tickDelta, CallbackInfo ci) {
        QuadzClient.getQuadcopterFromCamera().ifPresent(quadcopter ->
            quadcopter.getView().onGuiRender(poseStack, tickDelta)
        );
    }

    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void renderCrosshair$HEAD(PoseStack poseStack, CallbackInfo ci) {
        QuadzClient.getQuadcopterFromCamera().ifPresent(quadcopter -> ci.cancel());
    }

    @Inject(method = "renderExperienceBar", at = @At("HEAD"), cancellable = true)
    public void renderExperienceBar$HEAD(PoseStack poseStack, int i, CallbackInfo ci) {
        QuadzClient.getQuadcopterFromCamera().ifPresent(quadcopter -> ci.cancel());
    }

}
