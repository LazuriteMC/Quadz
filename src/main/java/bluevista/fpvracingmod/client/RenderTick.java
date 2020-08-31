package bluevista.fpvracingmod.client;

import bluevista.fpvracingmod.math.QuaternionHelper;
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

        if (!client.isPaused() && ClientInitializer.physicsWorld != null)
            ClientInitializer.physicsWorld.stepWorld();

        if (entity instanceof DroneEntity) {
            DroneEntity drone = (DroneEntity) entity;

            Quat4f q = drone.getOrientation();
            Quat4f newQ = new Quat4f();
            newQ.set(q.x, -q.y, q.z, -q.w);

            Matrix4f newMat = new Matrix4f(QuaternionHelper.quat4fToQuaternion(newQ));
            newMat.transpose();
            stack.peek().getModel().multiply(newMat);
        }
    }
}
