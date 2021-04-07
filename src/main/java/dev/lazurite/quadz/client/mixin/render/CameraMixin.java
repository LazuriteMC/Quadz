package dev.lazurite.quadz.client.mixin.render;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import dev.lazurite.quadz.client.Config;
import dev.lazurite.quadz.common.data.DataDriver;
import dev.lazurite.quadz.common.data.model.Template;
import dev.lazurite.quadz.common.state.entity.QuadcopterEntity;
import dev.lazurite.rayon.core.impl.util.math.QuaternionHelper;
import dev.lazurite.rayon.core.impl.util.math.VectorHelper;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.RaycastContext;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This mixin allows quadcopter {@link Template}s to apply
 * their own camera position transformation as well as allows
 * drones to enter third person mode using that same transformation.
 */
@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow private BlockView area;
    @Shadow private Entity focusedEntity;
    @Shadow protected abstract void setPos(Vec3d pos);

    @Shadow private Vec3d pos;

    @Shadow @Final private net.minecraft.client.util.math.Vector3f horizontalPlane;

    @Inject(method = "update", at = @At("TAIL"))
    public void update(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo info) {
        if (focusedEntity instanceof QuadcopterEntity) {
            QuadcopterEntity quadcopter = (QuadcopterEntity) focusedEntity;
            Vector3f location = quadcopter.getPhysicsLocation(new Vector3f(), tickDelta);
            Quaternion rotation = quadcopter.getPhysicsRotation(new Quaternion(), tickDelta);
            Template template = DataDriver.getTemplate(quadcopter.getTemplate());
            Vector3f point = new Vector3f(0.0f, template.getSettings().getCameraY(), template.getSettings().getCameraX());

            net.minecraft.client.util.math.Vector3f vec = VectorHelper.bulletToMinecraft(point);

            if (thirdPerson) {
                QuaternionHelper.rotateX(rotation, -quadcopter.getCameraAngle());

                if (inverseView) {
                    QuaternionHelper.rotateX(rotation, -Config.getInstance().thirdPersonAngle);
                    vec.add(VectorHelper.bulletToMinecraft(new Vector3f(0.0f,
                            Config.getInstance().thirdPersonOffsetY,
                            Config.getInstance().thirdPersonOffsetX)));
                } else {
                    QuaternionHelper.rotateX(rotation, Config.getInstance().thirdPersonAngle);
                    vec.add(VectorHelper.bulletToMinecraft(new Vector3f(0.0f,
                            Config.getInstance().thirdPersonOffsetY,
                            -Config.getInstance().thirdPersonOffsetX)));
                }
            }

            vec.rotate(QuaternionHelper.bulletToMinecraft(rotation));
            vec.add(VectorHelper.bulletToMinecraft(location));
            setPos(clip(VectorHelper.vector3fToVec3d(location), VectorHelper.vector3fToVec3d(VectorHelper.minecraftToBullet(vec))));
        }
    }

    public Vec3d clip(Vec3d quadcopter, Vec3d target) {
        HitResult hitResult = this.area.raycast(new RaycastContext(quadcopter, target, RaycastContext.ShapeType.VISUAL, RaycastContext.FluidHandling.NONE, this.focusedEntity));

        if (hitResult.getType() != HitResult.Type.MISS) {
            target = hitResult.getPos().subtract(target.subtract(quadcopter).normalize());
        }

        return target;
    }
}
