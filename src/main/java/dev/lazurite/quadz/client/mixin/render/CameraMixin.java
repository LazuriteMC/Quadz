package dev.lazurite.quadz.client.mixin.render;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import dev.lazurite.quadz.common.data.DataDriver;
import dev.lazurite.quadz.common.data.model.Template;
import dev.lazurite.quadz.common.state.entity.QuadcopterEntity;
import dev.lazurite.rayon.core.impl.util.math.QuaternionHelper;
import dev.lazurite.rayon.core.impl.util.math.VectorHelper;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This mixin allows quadcopter {@link Template}s to apply
 * their own camera position transformation as well as allows
 * drones to enter third person mode using that same transformation.
 */
@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow @Final private net.minecraft.client.util.math.Vector3f horizontalPlane;
    @Shadow @Final private net.minecraft.client.util.math.Vector3f verticalPlane;
    @Shadow @Final private net.minecraft.client.util.math.Vector3f diagonalPlane;
    @Shadow @Final private net.minecraft.util.math.Quaternion rotation;
    @Shadow @Final private BlockPos.Mutable blockPos;
    @Shadow private Entity focusedEntity;
    @Shadow private Vec3d pos;
    @Shadow private float pitch;
    @Shadow private float yaw;
    @Shadow protected abstract void moveBy(double x, double y, double z);

    @Inject(
            method = "update",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/Camera;setRotation(FF)V"
            )
    )
    public void update(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo info) {
        if (focusedEntity instanceof QuadcopterEntity) {
            QuadcopterEntity quadcopter = (QuadcopterEntity) focusedEntity;

            if (quadcopter.getRigidBody() != null && quadcopter.getRigidBody().getFrame() != null) {
                Template template = DataDriver.getTemplate(quadcopter.getTemplate());
                Vector3f location = quadcopter.getPhysicsLocation(new Vector3f(), tickDelta);

                pos = VectorHelper.vector3fToVec3d(location);
                blockPos.set(pos.x, pos.y, pos.z);

                Quaternion quaternion = ((QuadcopterEntity) focusedEntity).getPhysicsRotation(new Quaternion(), tickDelta);
                int cameraAngle = ((QuadcopterEntity) focusedEntity).getCameraAngle();

                if (!thirdPerson) {
                    double cameraX = template.getSettings().getCameraX();
                    double cameraY = template.getSettings().getCameraY();
                    moveBy(cameraX, cameraY, 0);
                }

                this.pitch = QuaternionHelper.getPitch(quaternion);
                this.yaw = QuaternionHelper.getYaw(quaternion);

                this.rotation.set(0.0F, 0.0F, 0.0F, 1.0F);
                this.rotation.hamiltonProduct(QuaternionHelper.bulletToMinecraft(quaternion));
                this.rotation.hamiltonProduct(net.minecraft.client.util.math.Vector3f.NEGATIVE_X.getDegreesQuaternion(cameraAngle));

                this.horizontalPlane.set(0.0F, 0.0F, 1.0F);
                this.horizontalPlane.rotate(rotation);
                this.verticalPlane.set(0.0F, 1.0F, 0.0F);
                this.verticalPlane.rotate(rotation);
                this.diagonalPlane.set(1.0F, 0.0F, 0.0F);
                this.diagonalPlane.rotate(rotation);
            }
        }
    }

    @Inject(method = "setPos(DDD)V", at = @At("HEAD"), cancellable = true)
    public void setPos(double x, double y, double z, CallbackInfo info) {
        if (focusedEntity instanceof QuadcopterEntity) {
            info.cancel();
        }
    }

    @Inject(method = "setRotation", at = @At("HEAD"), cancellable = true)
    public void setRotation(float yaw, float pitch, CallbackInfo info) {
        if (focusedEntity instanceof QuadcopterEntity) {
            info.cancel();
        }
    }
}