package dev.lazurite.quadz.client.render.ui.toast;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.lazurite.quadz.Quadz;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class ControllerNotFoundToast implements Toast {
    public static long visibilityTime = 1000L;
    private static ControllerNotFoundToast toast;
    private boolean visible;

    @Override
    public Visibility render(PoseStack poseStack, ToastComponent toastComponent, long startTime) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        toastComponent.blit(poseStack, 0, 0, 0, 0, width(), height());

        toastComponent.getMinecraft().font.draw(poseStack, Component.translatable("toast.quadz.controller.notfound"), 30, 12, -1);
        toastComponent.getMinecraft().getItemRenderer().renderAndDecorateFakeItem(new ItemStack(Quadz.TRANSMITTER_ITEM), 8, 8);

        this.visible = startTime < visibilityTime;
        return startTime >= visibilityTime ? Visibility.HIDE : Visibility.SHOW;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public static void add() {
        ToastComponent toastComponent = Minecraft.getInstance().getToasts();

        if (toast == null || !toast.isVisible()) {
            toast = new ControllerNotFoundToast();
            toastComponent.addToast(toast);
        }
    }
}
