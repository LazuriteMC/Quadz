package dev.lazurite.quadz.client.mixin.render;

import com.jme3.math.Vector3f;
import dev.lazurite.quadz.common.entity.QuadcopterEntity;
import dev.lazurite.rayon.core.impl.util.math.VectorHelper;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow private Vec3d pos;
    @Shadow @Final private BlockPos.Mutable blockPos;
    @Shadow protected abstract void setPos(Vec3d pos);

//    @Redirect(
//            method = "update",
//            at = @At(
//                    value = "INVOKE",
//                    target = "Lnet/minecraft/client/render/Camera;setPos(DDD)V"
//            )
//    )
//    public void setPos(Camera camera, double x, double y, double z, BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta) {
//        if (focusedEntity instanceof QuadcopterEntity) {
//            this.pos = VectorHelper.vector3fToVec3d(((QuadcopterEntity) focusedEntity).getPhysicsLocation(new Vector3f(), tickDelta));
//            this.blockPos.set(pos.x, pos.y, pos.z);
//        } else {
//            this.setPos(new Vec3d(x, y, z));
//        }
//    }
}
