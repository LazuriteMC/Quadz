package dev.lazurite.quadz.client.mixin.render;

import dev.lazurite.quadz.common.state.entity.QuadcopterEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow protected abstract void moveBy(double x, double y, double z);
    @Shadow protected abstract double clipToSpace(double desiredCameraDistance);

    @Inject(method = "update", at = @At("TAIL"))
    public void update(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo info) {
        if (focusedEntity instanceof QuadcopterEntity) {
            // TODO rotate about quaternion
            moveBy(clipToSpace(0.2), 0, 0);
        }
    }
}
