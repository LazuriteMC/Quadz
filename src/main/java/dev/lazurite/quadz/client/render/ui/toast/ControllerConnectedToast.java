package dev.lazurite.quadz.client.render.ui.toast;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.lazurite.quadz.Quadz;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
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
        toastComponent.getMinecraft().getTextureManager().bindForSetup(TEXTURE);
        toastComponent.blit(poseStack, 0, 0, 0, 0, width(), height());
        toastComponent.getMinecraft().font.draw(poseStack, message, 30, 7, -1);
        toastComponent.getMinecraft().font.draw(poseStack, new TextComponent(controllerName), 30, 18, -1);

        poseStack.pushPose();
        poseStack.scale(1.5f, 1.5f, 1.0f);
        toastComponent.getMinecraft().getItemRenderer().renderAndDecorateFakeItem(new ItemStack(Quadz.TRANSMITTER_ITEM), 3, 3);
        poseStack.popPose();

        return startTime >= 5000L ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
    }

    public static void add(Component message, String name) {
        ToastComponent manager = Minecraft.getInstance().getToasts();
        manager.addToast(new ControllerConnectedToast(message, name));
    }
}
