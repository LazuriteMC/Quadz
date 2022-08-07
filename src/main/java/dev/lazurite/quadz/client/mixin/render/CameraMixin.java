package dev.lazurite.quadz.client.mixin.render;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import dev.lazurite.form.api.loader.TemplateLoader;
import dev.lazurite.form.impl.common.template.model.Template;
import dev.lazurite.remote.impl.client.util.Config;
import dev.lazurite.quadz.common.entity.QuadcopterEntity;
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
    @Shadow private float xRot;
    @Shadow private float yRot;

    @Shadow protected abstract void setPosition(Vec3 pos);
    @Shadow protected abstract void move(double x, double y, double z);

    /**
     * This mixin transforms the camera's rotation and position based on the quadcopter rotation and template.
     */
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
                TemplateLoader.getTemplateById(quadcopter.getTemplate()).ifPresent(template -> {
                    final var location = quadcopter.getPhysicsLocation(new Vector3f(), f);
                    setPosition(VectorHelper.toVec3(Convert.toMinecraft(location)));

                    final var quaternion = ((QuadcopterEntity) entity).getPhysicsRotation(new Quaternion(), f);
                    final var cameraAngle = ((QuadcopterEntity) entity).getCameraAngle();
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

                    if (!bl && !Config.renderCameraInCenter) {
                        double cameraX = template.metadata().get("cameraX").getAsDouble();
                        double cameraY = template.metadata().get("cameraY").getAsDouble();
                        move(cameraX, cameraY, 0);
                    }
                });
            }
        }
    }
}