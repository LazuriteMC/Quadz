package dev.lazurite.fpvracing.common.entity.quads;

import dev.lazurite.fpvracing.FPVRacing;
import dev.lazurite.fpvracing.common.entity.QuadcopterEntity;
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

public class VoyagerEntity extends QuadcopterEntity implements IAnimatable {
    private static final float thrustForce = 75.0f;
    private static final float thrustCurve = 1.0f;

    private final AnimationFactory factory = new AnimationFactory(this);

    public VoyagerEntity(EntityType<? extends LivingEntity> type, World world) {
        super(type, world);
        this.getRigidBody().setDragCoefficient(0.015f);
        this.getRigidBody().setMass(1.5f);
        this.setCameraAngle(30);
    }

    @Override
    public float getThrustForce() {
        return thrustForce;
    }

    @Override
    public float getThrustCurve() {
        return thrustCurve;
    }

    @Override
    public void kill() {
        if (world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
            ItemStack stack = new ItemStack(FPVRacing.VOYAGER_ITEM);
            CompoundTag tag = new CompoundTag();
            writeCustomDataToTag(tag);
            FPVRacing.QUADCOPTER_CONTAINER.get(stack).readFromNbt(tag);
            dropStack(stack);
        }

        super.kill();
    }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.fpvracing.voyager.armed", true));
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData animationData) {
        animationData.addAnimationController(new AnimationController<>(this, "voyager_entity_controller", 0, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }
}
