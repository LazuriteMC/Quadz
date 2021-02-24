package dev.lazurite.fpvracing.common.entity.quads;

import dev.lazurite.fpvracing.FPVRacing;
import dev.lazurite.fpvracing.common.entity.QuadcopterEntity;
import dev.lazurite.rayon.impl.bullet.body.ElementRigidBody;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

public class PixelEntity extends QuadcopterEntity implements IAnimatable {
    private final AnimationFactory factory = new AnimationFactory(this);
    private final ElementRigidBody rigidBody = new ElementRigidBody(this);

    public PixelEntity(EntityType<? extends LivingEntity> type, World world) {
        super(type, world);
        this.setCameraAngle(5);
        this.getRigidBody().setMass(0.050f); // 50 grams
        this.getRigidBody().setDragCoefficient(0.05f);
    }

    @Override
    public float getThrustForce() {
        return 10.0f;
    }

    @Override
    public float getThrustCurve() {
        return 1.0f;
    }

    @Override
    public void dropSpawner() {
        if (world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
            ItemStack stack = new ItemStack(FPVRacing.PIXEL_ITEM);
            CompoundTag tag = new CompoundTag();
            writeCustomDataToTag(tag);
            FPVRacing.QUADCOPTER_CONTAINER.get(stack).readFromNbt(tag);
            dropStack(stack);
        }
    }

    @Override
    public ElementRigidBody getRigidBody() {
        return this.rigidBody;
    }

    @Override
    public void kill() {
        this.dropSpawner();
        super.kill();
    }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.fpvracing.pixel.armed", true));
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData animationData) {
        animationData.addAnimationController(new AnimationController<>(this, "pixel_entity_controller", 0, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }
}
