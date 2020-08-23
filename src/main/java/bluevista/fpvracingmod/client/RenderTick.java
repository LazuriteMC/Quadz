package bluevista.fpvracingmod.client;

import bluevista.fpvracingmod.client.math.QuaternionHelper;
import bluevista.fpvracingmod.server.entities.DroneEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Matrix4f;

import javax.vecmath.Quat4f;

@Environment(EnvType.CLIENT)
public class RenderTick {
    public static void tick(MinecraftClient client, MatrixStack stack) {
        Entity entity = client.getCameraEntity();

        if(ClientInitializer.physicsWorld != null && !client.isPaused())
            ClientInitializer.physicsWorld.stepWorld();

        if(entity instanceof DroneEntity) {
            DroneEntity drone = (DroneEntity) entity;

            Quat4f q = drone.getOrientation();
            QuaternionHelper.rotateX(q, drone.getCameraAngle());

            Matrix4f newMat = new Matrix4f(QuaternionHelper.quat4fToQuaternion(q));
            Matrix4f screenMat = stack.peek().getModel();
            newMat.transpose();
            screenMat.multiply(newMat);
        }
    }
}
