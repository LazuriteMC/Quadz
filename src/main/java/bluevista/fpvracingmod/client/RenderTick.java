package bluevista.fpvracingmod.client;

import bluevista.fpvracingmod.server.entities.DroneEntity;
import bluevista.fpvracingmod.server.entities.ViewHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Matrix4f;

public class RenderTick {

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static void tick(MatrixStack stack, float delta) {
        Entity currentViewEntity = mc.getCameraEntity();
        if(currentViewEntity instanceof ViewHandler && !mc.gameRenderer.getCamera().isThirdPerson()) {
            if (((ViewHandler) currentViewEntity).getTarget() instanceof DroneEntity) {
                DroneEntity drone = (DroneEntity) ((ViewHandler) currentViewEntity).getTarget();
                Matrix4f newMat = new Matrix4f(drone.getOrientation());
                Matrix4f screenMat = stack.peek().getModel();
                newMat.transpose();
                screenMat.multiply(newMat);
            }
        }

        if(ClientTick.currentDrone != null)
            InputTick.tick(ClientTick.currentDrone, delta);
    }
}
