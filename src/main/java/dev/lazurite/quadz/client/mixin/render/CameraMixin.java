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
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow protected abstract void setPos(double x, double y, double z);

    @Inject(method = "update", at = @At("TAIL"))
    public void update(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo info) {
        if (focusedEntity instanceof QuadcopterEntity) {
            QuadcopterEntity quadcopter = (QuadcopterEntity) focusedEntity;
            Vector3f location = quadcopter.getPhysicsLocation(new Vector3f(), tickDelta);
            Quaternion rotation = quadcopter.getPhysicsRotation(new Quaternion(), tickDelta);
            Template template = DataDriver.getTemplate(quadcopter.getTemplate());
            Vector3f point = new Vector3f(0.0f, template.getSettings().getCameraY(), template.getSettings().getCameraX());

            net.minecraft.client.util.math.Vector3f vec = VectorHelper.bulletToMinecraft(point);
            vec.rotate(QuaternionHelper.bulletToMinecraft(rotation));
            vec.add(VectorHelper.bulletToMinecraft(location));
            setPos(vec.getX(),vec.getY(), vec.getZ());
        }
    }
}
