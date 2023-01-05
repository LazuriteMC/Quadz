package dev.lazurite.quadz.client.render.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.lazurite.quadz.Quadz;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class ControllerConnectedToast implements Toast {

    private final Component message;
    private final String controllerName;

    public ControllerConnectedToast(Component message, String controllerName) {
        this.message = message;

        if (controllerName.length() > 25) {
            this.controllerName = controllerName.substring(0, 25) + "...";
        } else {
            this.controllerName = controllerName;
        }
    }

    @Override
    public Visibility render(PoseStack poseStack, ToastComponent toastComponent, long startTime) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        toastComponent.blit(poseStack, 0, 0, 0, 0, width(), height());
        toastComponent.getMinecraft().font.draw(poseStack, message, 30, 7, -1);
        toastComponent.getMinecraft().font.draw(poseStack, Component.literal(controllerName), 30, 18, -1);
        toastComponent.getMinecraft().getItemRenderer().renderAndDecorateFakeItem(new ItemStack(Quadz.REMOTE_ITEM), 8, 8);

        return startTime >= 5000L ? Visibility.HIDE : Visibility.SHOW;
    }

    public static void add(Component message, String name) {
        var manager = Minecraft.getInstance().getToasts();
        manager.addToast(new ControllerConnectedToast(message, name));
    }

}
