package dev.lazurite.quadz.client.render.ui.toast;

import dev.lazurite.quadz.Quadz;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public class ControllerConnectedToast implements Toast {
    private final Text message;
    private final String controllerName;

    public ControllerConnectedToast(Text message, String controllerName) {
        this.message = message;

        if (controllerName.length() > 25) {
            this.controllerName = controllerName.substring(0, 25) + "...";
        } else {
            this.controllerName = controllerName;
        }
    }

    @Override
    public Visibility draw(MatrixStack matrices, ToastManager manager, long startTime) {
        manager.getGame().getTextureManager().bindTexture(TEXTURE);
        manager.drawTexture(matrices, 0, 0, 0, 0, getWidth(), getHeight());
        manager.getGame().textRenderer.draw(matrices, message, 30, 7, -1);
        manager.getGame().textRenderer.draw(matrices, new LiteralText(controllerName), 30, 18, -1);

        matrices.push();
        matrices.scale(1.5f, 1.5f, 1.0f);
        manager.getGame().getItemRenderer().renderInGui(new ItemStack(Quadz.TRANSMITTER_ITEM), 3, 3);
        matrices.pop();

        return startTime >= 5000L ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
    }

    public static void add(Text message, String name) {
        ToastManager manager = MinecraftClient.getInstance().getToastManager();
        manager.add(new ControllerConnectedToast(message, name));
    }
}
