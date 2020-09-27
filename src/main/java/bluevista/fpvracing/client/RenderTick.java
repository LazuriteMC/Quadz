package bluevista.fpvracing.client;

import bluevista.fpvracing.client.physics.PhysicsWorld;
import bluevista.fpvracing.config.Config;
import bluevista.fpvracing.client.math.QuaternionHelper;
import bluevista.fpvracing.server.entities.DroneEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Matrix4f;

import javax.vecmath.Quat4f;

@Environment(EnvType.CLIENT)
public class RenderTick {
    public static void tick(MinecraftClient client, MatrixStack stack, float tickDelta) {
        Entity entity = client.getCameraEntity();

        PhysicsWorld w = ClientInitializer.physicsWorld;
        if (w != null) {
            if (!client.isPaused())
                w.stepWorld();
            else w.clock.reset();
        }

        if (entity instanceof DroneEntity) {
            DroneEntity drone = (DroneEntity) entity;

            Quat4f q;
            if (drone.isActive()) {
                q = drone.getOrientation();
            } else {
                q = QuaternionHelper.slerp(drone.getPrevOrientation(), drone.getOrientation(), tickDelta);
            }

            q.set(q.x, -q.y, q.z, -q.w);
            QuaternionHelper.rotateX(q, drone.getConfigValues(Config.CAMERA_ANGLE).intValue());

            Matrix4f newMat = new Matrix4f(QuaternionHelper.quat4fToQuaternion(q));
            newMat.transpose();
            stack.peek().getModel().multiply(newMat);
        }
    }
}
