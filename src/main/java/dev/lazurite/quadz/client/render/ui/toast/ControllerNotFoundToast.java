package dev.lazurite.quadz.client.render.ui.toast;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.lazurite.quadz.Quadz;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;

public class ControllerNotFoundToast implements Toast {
    public static long visibilityTime = 1000L;
    private static ControllerNotFoundToast toast;
    private boolean visible;

    @Override
    public Visibility render(PoseStack poseStack, ToastComponent toastComponent, long startTime) {
        toastComponent.getMinecraft().getTextureManager().bindForSetup(TEXTURE);
        toastComponent.blit(poseStack, 0, 0, 0, 0, width(), height());
        toastComponent.getMinecraft().font.draw(poseStack, new TranslatableComponent("toast.quadz.controller.notfound"), 30, 12, -1);

        poseStack.pushPose();
        poseStack.scale(1.5f, 1.5f, 1.0f);
        toastComponent.getMinecraft().getItemRenderer().renderAndDecorateFakeItem(new ItemStack(Quadz.TRANSMITTER_ITEM), 3, 3);
        poseStack.popPose();

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
