package dev.lazurite.quadz.common.entity.quads;

import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.common.entity.QuadcopterEntity;
import dev.lazurite.quadz.common.item.container.QuadcopterContainer;
import dev.lazurite.rayon.core.impl.physics.space.body.ElementRigidBody;
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

import java.util.Optional;

public class VoxelRacerOneEntity extends QuadcopterEntity implements IAnimatable {
    private final AnimationFactory factory = new AnimationFactory(this);
    private final ElementRigidBody rigidBody = new ElementRigidBody(this);

    public VoxelRacerOneEntity(EntityType<? extends LivingEntity> type, World world) {
        super(type, world);
        this.getRigidBody().setMass(0.5f);
        this.getRigidBody().setDragCoefficient(0.04f);
    }

    @Override
    public float getThrustForce() {
        return 100.0f;
    }

    @Override
    public float getThrustCurve() {
        return 1.0f;
    }

    @Override
    public void dropSpawner() {
        if (world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
            ItemStack stack = new ItemStack(Quadz.VOXEL_RACER_ONE_ITEM);
            Optional<QuadcopterContainer> container = Quadz.QUADCOPTER_CONTAINER.maybeGet(stack);

            CompoundTag tag = new CompoundTag();
            writeCustomDataToTag(tag);
            container.ifPresent(quadcopterContainer -> quadcopterContainer.readFromNbt(tag));

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
        AnimationController<VoxelRacerOneEntity> controller = new AnimationController<>(this, "voxel_racer_one_entity_controller", 0, this::predicate);
        controller.setAnimation(new AnimationBuilder().addAnimation("animation.fpvracing.voxel_racer_one.armed", true));
        animationData.addAnimationController(controller);
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }
}
