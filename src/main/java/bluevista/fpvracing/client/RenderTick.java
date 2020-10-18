package bluevista.fpvracing.client;

import bluevista.fpvracing.physics.PhysicsWorld;
import bluevista.fpvracing.config.Config;
import bluevista.fpvracing.physics.entity.ClientEntityPhysics;
import bluevista.fpvracing.server.entities.QuadcopterEntity;
import bluevista.fpvracing.util.math.QuaternionHelper;
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

        if (entity instanceof QuadcopterEntity) {
            QuadcopterEntity drone = (QuadcopterEntity) entity;

            Quat4f q;
            ClientEntityPhysics physics = (ClientEntityPhysics) drone.getPhysics();
            if (physics.isActive()) {
                q = physics.getOrientation();
            } else {
                q = QuaternionHelper.slerp(physics.getPrevOrientation(), physics.getOrientation(), tickDelta);
            }

            q.set(q.x, -q.y, q.z, -q.w);
            QuaternionHelper.rotateX(q, drone.getConfigValues(Config.CAMERA_ANGLE).intValue());

            Matrix4f newMat = new Matrix4f(QuaternionHelper.quat4fToQuaternion(q));
            newMat.transpose();
            stack.peek().getModel().multiply(newMat);
        }
    }
}
