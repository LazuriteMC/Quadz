package bluevista.fpvracingmod.mixin;

import bluevista.fpvracingmod.client.ClientTick;
import bluevista.fpvracingmod.server.entities.PhysicsEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.vecmath.Quat4f;

@Mixin(Camera.class)
public class CameraMixin {

    @Inject(at = @At("HEAD"), method = "update")
    public void update(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo info) {
        if(focusedEntity instanceof PhysicsEntity) {
            PhysicsEntity physics = (PhysicsEntity) focusedEntity;

            if(!physics.isActive()) {
                Quat4f slerp = new Quat4f();
                slerp.interpolate(physics.prevQuat, physics.remoteQuat, tickDelta);
                physics.setOrientation(slerp);
            }
        }
    }
}
