package dev.lazurite.quadz.client.mixin;

import com.jme3.math.Vector3f;
import dev.lazurite.quadz.common.entity.QuadcopterEntity;
import dev.lazurite.rayon.impl.bullet.math.Convert;
import dev.lazurite.toolbox.api.math.VectorHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {
    @Inject(method = "getPosition", at = @At("HEAD"), cancellable = true)
    public void getPosition(float delta, CallbackInfoReturnable<Vec3> info) {
        if ((Entity) (Object) this instanceof QuadcopterEntity quadcopter) {
            info.setReturnValue(VectorHelper.toVec3(Convert.toMinecraft(quadcopter.getPhysicsLocation(new Vector3f(), delta))));
        }
    }
}
