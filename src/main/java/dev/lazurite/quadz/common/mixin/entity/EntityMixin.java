package dev.lazurite.quadz.common.mixin.entity;

import dev.lazurite.quadz.common.data.DataDriver;
import dev.lazurite.quadz.common.data.model.Template;
import dev.lazurite.quadz.common.state.entity.QuadcopterEntity;
import dev.lazurite.rayon.core.impl.bullet.collision.body.shape.MinecraftShape;
import dev.lazurite.rayon.core.impl.bullet.thread.PhysicsThread;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow public World world;
    @Shadow public EntityDimensions dimensions;
    @Shadow public abstract Box getBoundingBox();
    @Shadow public abstract void setBoundingBox(Box boundingBox);
    @Shadow public abstract double getX();
    @Shadow public abstract double getY();
    @Shadow public abstract double getZ();

    @Inject(method = "calculateDimensions", at = @At("HEAD"), cancellable = true)
    public void calculateEntityDimensions(CallbackInfo info) {
        if ((Entity) (Object) this instanceof QuadcopterEntity) {
            QuadcopterEntity quadcopter = (QuadcopterEntity) (Object) this;
            Template template = DataDriver.getTemplate(quadcopter.getTemplate());

            if (template != null) {
                EntityDimensions dimensions1 = this.dimensions;
                this.dimensions = new EntityDimensions(quadcopter.getWidth(), quadcopter.getHeight(), true);

                if (dimensions.width < dimensions1.width) {
                    double d = (double)dimensions.width / 2.0D;
                    setBoundingBox(new Box(getX() - d, getY(), getZ() - d, getX() + d, getY() + dimensions.height, getZ() + d));
                } else {
                    Box box = this.getBoundingBox();
                    setBoundingBox(new Box(box.minX, box.minY, box.minZ, box.minX + dimensions.width, box.minY + dimensions.height, box.minZ + dimensions.width));
                }

                PhysicsThread.get(world).execute(() ->
                        quadcopter.getRigidBody().setCollisionShape(MinecraftShape.of(quadcopter.getBoundingBox())));
                info.cancel();
            }
        }
    }
}
