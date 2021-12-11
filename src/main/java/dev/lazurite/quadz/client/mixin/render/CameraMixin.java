package dev.lazurite.quadz.client.mixin.render;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import dev.lazurite.quadz.client.Config;
import dev.lazurite.quadz.common.data.DataDriver;
import dev.lazurite.quadz.common.data.model.Template;
import dev.lazurite.quadz.common.state.entity.QuadcopterEntity;
import dev.lazurite.rayon.impl.bullet.math.Convert;
import dev.lazurite.toolbox.api.math.QuaternionHelper;
import dev.lazurite.toolbox.api.math.VectorHelper;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
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
    @Shadow @Final private com.mojang.math.Vector3f forwards;
    @Shadow @Final private com.mojang.math.Vector3f up;
    @Shadow @Final private com.mojang.math.Vector3f left;
    @Shadow @Final private com.mojang.math.Quaternion rotation;
    @Shadow private Entity entity;
    @Shadow private float xRot;
    @Shadow private float yRot;

    @Shadow protected abstract void setPosition(Vec3 pos);
    @Shadow protected abstract void move(double x, double y, double z);

    @Inject(
            method = "setup",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Camera;setRotation(FF)V"
            )
    )
    public void setup_setRotation(BlockGetter blockGetter, Entity entity, boolean bl, boolean bl2, float f, CallbackInfo ci) {
        if (entity instanceof QuadcopterEntity quadcopter) {
            if (quadcopter.getRigidBody() != null && quadcopter.getRigidBody().getFrame() != null) {
                var template = DataDriver.getTemplate(quadcopter.getTemplate());
                var location = quadcopter.getPhysicsLocation(new Vector3f(), f);
                setPosition(VectorHelper.toVec3(Convert.toMinecraft(location)));

                var quaternion = ((QuadcopterEntity) entity).getPhysicsRotation(new Quaternion(), f);
                var cameraAngle = ((QuadcopterEntity) entity).getCameraAngle();
                this.xRot = QuaternionHelper.getPitch(Convert.toMinecraft(quaternion));
                this.yRot = QuaternionHelper.getYaw(Convert.toMinecraft(quaternion));

                this.rotation.set(0.0F, 0.0F, 0.0F, 1.0F);
                this.rotation.mul(Convert.toMinecraft(quaternion));
                this.rotation.mul(com.mojang.math.Vector3f.XN.rotationDegrees(cameraAngle));

                this.forwards.set(0.0F, 0.0F, 1.0F);
                this.forwards.transform(rotation);
                this.up.set(0.0F, 1.0F, 0.0F);
                this.up.transform(rotation);
                this.left.set(1.0F, 0.0F, 0.0F);
                this.left.transform(rotation);

                if (!bl && !Config.getInstance().renderCameraInCenter) {
                    double cameraX = template.getSettings().getCameraX();
                    double cameraY = template.getSettings().getCameraY();
                    move(cameraX, cameraY, 0);
                }
            }
        }
    }

    @Inject(method = "setPosition(DDD)V", at = @At("HEAD"), cancellable = true)
    public void setPosition_HEAD(double x, double y, double z, CallbackInfo info) {
        if (entity instanceof QuadcopterEntity) {
            info.cancel();
        }
    }

    @Inject(method = "setRotation", at = @At("HEAD"), cancellable = true)
    public void setRotation_HEAD(float yaw, float pitch, CallbackInfo info) {
        if (entity instanceof QuadcopterEntity) {
            info.cancel();
        }
    }
}