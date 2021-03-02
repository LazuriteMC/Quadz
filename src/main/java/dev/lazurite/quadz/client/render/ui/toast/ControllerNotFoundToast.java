package dev.lazurite.quadz.client.render.ui.toast;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.lazurite.quadz.Quadz;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.TranslatableText;

public class ControllerNotFoundToast implements Toast {
    public static long visibilityTime = 1000L;
    private static ControllerNotFoundToast toast;
    private boolean visible;

    @Override
    public Visibility draw(MatrixStack matrices, ToastManager manager, long startTime) {
        manager.getGame().getTextureManager().bindTexture(TEXTURE);
        RenderSystem.color3f(1.0f, 1.0f, 1.0f);
        manager.drawTexture(matrices, 0, 0, 0, 0, getWidth(), getHeight());
        manager.getGame().textRenderer.draw(matrices, new TranslatableText("toast.quadz.controller.notfound"), 30, 12, -1);

        RenderSystem.pushMatrix();
        RenderSystem.scalef(1.5f, 1.5f, 1.0f);
        manager.getGame().getItemRenderer().renderInGui(new ItemStack(Quadz.TRANSMITTER_ITEM), 3, 3);
        RenderSystem.popMatrix();

        this.visible = startTime < visibilityTime;
        return startTime >= visibilityTime ? Visibility.HIDE : Visibility.SHOW;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public static void add() {
        ToastManager manager = MinecraftClient.getInstance().getToastManager();

        if (toast == null || !toast.isVisible()) {
            toast = new ControllerNotFoundToast();
            manager.add(toast);
        }
    }
}
