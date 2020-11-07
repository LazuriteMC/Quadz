package dev.lazurite.fpvracing.client;

import dev.lazurite.fpvracing.physics.PhysicsWorld;
import dev.lazurite.fpvracing.server.entity.FlyableEntity;
import dev.lazurite.fpvracing.util.math.QuaternionHelper;
import dev.lazurite.fpvracing.physics.entity.ClientPhysicsHandler;
import dev.lazurite.fpvracing.server.entity.flyable.QuadcopterEntity;
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

        if (entity instanceof FlyableEntity) {
            FlyableEntity flyable = (FlyableEntity) entity;

            Quat4f q;
            ClientPhysicsHandler physics = (ClientPhysicsHandler) flyable.getPhysics();
            if (physics.isActive()) {
                q = physics.getOrientation();
            } else {
                q = QuaternionHelper.slerp(physics.getPrevOrientation(), physics.getOrientation(), tickDelta);
            }

            q.set(q.x, -q.y, q.z, -q.w);
            if (flyable instanceof QuadcopterEntity) {
                QuaternionHelper.rotateX(q, flyable.getValue(QuadcopterEntity.CAMERA_ANGLE));
            }

            Matrix4f newMat = new Matrix4f(QuaternionHelper.quat4fToQuaternion(q));
            newMat.transpose();
            stack.peek().getModel().multiply(newMat);
        }
    }
}
