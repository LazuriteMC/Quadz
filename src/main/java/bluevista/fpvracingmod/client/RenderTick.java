package bluevista.fpvracingmod.client;

import bluevista.fpvracingmod.client.input.InputTick;
import bluevista.fpvracingmod.client.math.QuaternionHelper;
import bluevista.fpvracingmod.server.entities.DroneEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Quaternion;

@Environment(EnvType.CLIENT)

public class RenderTick {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static void tick(MatrixStack stack, float delta) {
        Entity currentViewEntity = mc.getCameraEntity();
        if(currentViewEntity instanceof DroneEntity && !mc.gameRenderer.getCamera().isThirdPerson()) {
            DroneEntity drone = (DroneEntity) currentViewEntity;
            Quaternion q = new Quaternion(drone.getOrientation());
            QuaternionHelper.rotateX(q, drone.getCameraAngle());

            Matrix4f newMat = new Matrix4f(q);
            Matrix4f screenMat = stack.peek().getModel();
            newMat.transpose();

            screenMat.multiply(newMat);
        }

        if(ClientTick.currentDrone != null)
            InputTick.tick(ClientTick.currentDrone);
    }
}
