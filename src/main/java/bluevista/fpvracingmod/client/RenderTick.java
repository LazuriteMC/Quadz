package bluevista.fpvracingmod.client;

import bluevista.fpvracingmod.client.physics.PhysicsWorld;
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

        PhysicsWorld w = ClientInitializer.physicsWorld;
        if(w != null) {
            if (!client.isPaused())
                w.stepWorld();
            else w.clock.reset();
        }

        if (entity instanceof DroneEntity) {
            DroneEntity drone = (DroneEntity) entity;

            Quat4f q = drone.getOrientation();
            Quat4f newQ = new Quat4f();
            newQ.set(q.x, -q.y, q.z, -q.w);
            QuaternionHelper.rotateX(newQ, drone.getCameraAngle());

            Matrix4f newMat = new Matrix4f(QuaternionHelper.quat4fToQuaternion(newQ));
            newMat.transpose();
            stack.peek().getModel().multiply(newMat);
        }
    }
}
