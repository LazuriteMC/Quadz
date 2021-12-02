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
    public Visibility render(PoseStack matrices, ToastComponent manager, long startTime) {
        manager.getMinecraft().getTextureManager().bindForSetup(TEXTURE);
        manager.blit(matrices, 0, 0, 0, 0, width(), height());
        manager.getMinecraft().font.draw(matrices, new TranslatableComponent("toast.quadz.controller.notfound"), 30, 12, -1);

        matrices.pushPose();
        matrices.scale(1.5f, 1.5f, 1.0f);
        manager.getMinecraft().getItemRenderer().renderAndDecorateFakeItem(new ItemStack(Quadz.TRANSMITTER_ITEM), 3, 3);
        matrices.popPose();

        this.visible = startTime < visibilityTime;
        return startTime >= visibilityTime ? Visibility.HIDE : Visibility.SHOW;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public static void add() {
        ToastComponent manager = Minecraft.getInstance().getToasts();

        if (toast == null || !toast.isVisible()) {
            toast = new ControllerNotFoundToast();
            manager.addToast(toast);
        }
    }
}
