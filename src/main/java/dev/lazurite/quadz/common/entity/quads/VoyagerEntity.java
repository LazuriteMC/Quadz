package dev.lazurite.quadz.common.entity.quads;

import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.common.entity.QuadcopterEntity;
import dev.lazurite.rayon.core.impl.physics.space.body.ElementRigidBody;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

@SuppressWarnings("EntityConstructor")
public class VoyagerEntity extends QuadcopterEntity implements IAnimatable {
    private final AnimationFactory factory = new AnimationFactory(this);
    private final ElementRigidBody rigidBody = new ElementRigidBody(this);

    public VoyagerEntity(EntityType<? extends LivingEntity> type, World world) {
        super(type, world);
        this.getRigidBody().setMass(1.25f);
        this.getRigidBody().setDragCoefficient(0.005f);
    }

    @Override
    public float getThrustForce() {
        return 90.0f;
    }

    @Override
    public float getThrustCurve() {
        return 0.9f;
    }

    @Override
    public void dropSpawner() {
        if (world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
            ItemStack stack = new ItemStack(Quadz.VOYAGER_ITEM);
            Quadz.QUADCOPTER_CONTAINER.maybeGet(stack).ifPresent(quadcopter -> quadcopter.copyFrom(this));
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

    /* Called each frame */
    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        return isActive() ? PlayState.CONTINUE : PlayState.STOP;
    }

    @Override
    public void registerControllers(AnimationData animationData) {
        AnimationController<VoyagerEntity> controller = new AnimationController<>(this, "voyager_entity_controller", 0, this::predicate);
        controller.setAnimation(new AnimationBuilder().addAnimation("animation.fpvracing.voyager.armed", true));
        animationData.addAnimationController(controller);
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }
}
