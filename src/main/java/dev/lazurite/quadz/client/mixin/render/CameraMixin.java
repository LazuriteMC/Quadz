package dev.lazurite.quadz.client.mixin.render;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import dev.lazurite.quadz.client.Config;
import dev.lazurite.quadz.common.data.DataDriver;
import dev.lazurite.quadz.common.data.model.Template;
import dev.lazurite.quadz.common.state.entity.QuadcopterEntity;
import dev.lazurite.rayon.core.impl.bullet.math.Converter;
import dev.lazurite.toolbox.api.math.QuaternionHelper;
import dev.lazurite.toolbox.api.math.VectorHelper;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
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
    @Shadow @Final private Vec3f horizontalPlane;
    @Shadow @Final private Vec3f verticalPlane;
    @Shadow @Final private Vec3f diagonalPlane;
    @Shadow @Final private net.minecraft.util.math.Quaternion rotation;
    @Shadow private Entity focusedEntity;
    @Shadow private float pitch;
    @Shadow private float yaw;

    @Shadow protected abstract void setPos(Vec3d pos);
    @Shadow protected abstract void moveBy(double x, double y, double z);

    @Inject(
            method = "update",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/Camera;setRotation(FF)V"
            )
    )
    public void update(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo info) {
        if (focusedEntity instanceof QuadcopterEntity quadcopter) {
            if (quadcopter.getRigidBody() != null && quadcopter.getRigidBody().getFrame() != null) {
                var template = DataDriver.getTemplate(quadcopter.getTemplate());
                var location = quadcopter.getPhysicsLocation(new Vector3f(), tickDelta);
                setPos(VectorHelper.toVec3d(Converter.toMinecraft(location)));

                var quaternion = ((QuadcopterEntity) focusedEntity).getPhysicsRotation(new Quaternion(), tickDelta);
                var cameraAngle = ((QuadcopterEntity) focusedEntity).getCameraAngle();
                this.pitch = QuaternionHelper.getPitch(Converter.toMinecraft(quaternion));
                this.yaw = QuaternionHelper.getYaw(Converter.toMinecraft(quaternion));

                this.rotation.set(0.0F, 0.0F, 0.0F, 1.0F);
                this.rotation.hamiltonProduct(Converter.toMinecraft(quaternion));
                this.rotation.hamiltonProduct(Vec3f.NEGATIVE_X.getDegreesQuaternion(cameraAngle));

                this.horizontalPlane.set(0.0F, 0.0F, 1.0F);
                this.horizontalPlane.rotate(rotation);
                this.verticalPlane.set(0.0F, 1.0F, 0.0F);
                this.verticalPlane.rotate(rotation);
                this.diagonalPlane.set(1.0F, 0.0F, 0.0F);
                this.diagonalPlane.rotate(rotation);

                if (!thirdPerson && !Config.getInstance().renderCameraInCenter) {
                    double cameraX = template.getSettings().getCameraX();
                    double cameraY = template.getSettings().getCameraY();
                    moveBy(cameraX, cameraY, 0);
                }
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