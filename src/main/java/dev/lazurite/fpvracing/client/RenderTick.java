package dev.lazurite.fpvracing.client;

import dev.lazurite.fpvracing.server.entity.FlyableEntity;
import dev.lazurite.fpvracing.server.entity.flyable.QuadcopterEntity;
import dev.lazurite.rayon.physics.body.EntityRigidBody;
import dev.lazurite.rayon.physics.helper.math.QuaternionHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Matrix4f;
import physics.javax.vecmath.Quat4f;

@Environment(EnvType.CLIENT)
public class RenderTick {
    public static void tick(MinecraftClient client, MatrixStack stack, float tickDelta) {
        Entity entity = client.getCameraEntity();

        if (entity instanceof FlyableEntity) {
            EntityRigidBody body = EntityRigidBody.get(entity);

            Quat4f q = QuaternionHelper.slerp(body.getPrevOrientation(new Quat4f()), body.getTickOrientation(new Quat4f()), tickDelta);
            q.set(q.x, -q.y, q.z, -q.w);

            /* Camera Angle */
            if (entity instanceof QuadcopterEntity) {
//                QuaternionHelper.rotateX(q, flyable.getValue(QuadcopterEntity.CAMERA_ANGLE));
            }

            Matrix4f newMat = new Matrix4f(QuaternionHelper.quat4fToQuaternion(q));
            newMat.transpose();
            stack.peek().getModel().multiply(newMat);
        }
    }
}
