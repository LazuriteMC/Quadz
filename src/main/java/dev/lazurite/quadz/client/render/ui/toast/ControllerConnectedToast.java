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
    public Visibility render(PoseStack matrices, ToastComponent manager, long startTime) {
        manager.getMinecraft().getTextureManager().bindForSetup(TEXTURE);
        manager.blit(matrices, 0, 0, 0, 0, width(), height());
        manager.getMinecraft().font.draw(matrices, message, 30, 7, -1);
        manager.getMinecraft().font.draw(matrices, new TextComponent(controllerName), 30, 18, -1);

        matrices.pushPose();
        matrices.scale(1.5f, 1.5f, 1.0f);
        manager.getMinecraft().getItemRenderer().renderAndDecorateFakeItem(new ItemStack(Quadz.TRANSMITTER_ITEM), 3, 3);
        matrices.popPose();

        return startTime >= 5000L ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
    }

    public static void add(Component message, String name) {
        ToastComponent manager = Minecraft.getInstance().getToasts();
        manager.addToast(new ControllerConnectedToast(message, name));
    }
}
