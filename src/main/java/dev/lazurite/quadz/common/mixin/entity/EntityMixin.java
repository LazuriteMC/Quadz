package dev.lazurite.quadz.common.mixin.entity;

import dev.lazurite.quadz.common.data.template.TemplateLoader;
import dev.lazurite.quadz.common.data.template.model.Template;
import dev.lazurite.quadz.common.state.entity.QuadcopterEntity;
import dev.lazurite.rayon.impl.bullet.collision.body.shape.MinecraftShape;
import dev.lazurite.rayon.impl.bullet.thread.PhysicsThread;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Shadow public EntityDimensions dimensions;
    @Shadow public abstract void setBoundingBox(AABB aABB);
    @Shadow public abstract double getX();
    @Shadow public abstract double getY();
    @Shadow public abstract double getZ();
    @Shadow public abstract AABB getBoundingBox();

    @Shadow public Level level;

    @Inject(method = "refreshDimensions", at = @At("HEAD"), cancellable = true)
    public void refreshDimensions_HEAD(CallbackInfo info) {
        if ((Entity) (Object) this instanceof QuadcopterEntity) {
            QuadcopterEntity quadcopter = (QuadcopterEntity) (Object) this;
            Template template = TemplateLoader.getTemplate(quadcopter.getTemplate());

            if (template != null) {
                EntityDimensions dimensions1 = this.dimensions;
                this.dimensions = new EntityDimensions(quadcopter.getBbWidth(), quadcopter.getBbHeight(), true);

                if (dimensions.width < dimensions1.width) {
                    double d = (double)dimensions.width / 2.0D;
                    setBoundingBox(new AABB(getX() - d, getY(), getZ() - d, getX() + d, getY() + dimensions.height, getZ() + d));
                } else {
                    AABB box = this.getBoundingBox();
                    setBoundingBox(new AABB(box.minX, box.minY, box.minZ, box.minX + dimensions.width, box.minY + dimensions.height, box.minZ + dimensions.width));
                }

                PhysicsThread.get(level).execute(() ->
                        quadcopter.getRigidBody().setCollisionShape(MinecraftShape.convex(quadcopter.getBoundingBox())));
                info.cancel();
            }
        }
    }
}
