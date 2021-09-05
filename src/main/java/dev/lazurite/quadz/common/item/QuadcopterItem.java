package dev.lazurite.quadz.common.item;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.common.state.QuadcopterState;
import dev.lazurite.quadz.common.state.entity.QuadcopterEntity;
import dev.lazurite.rayon.core.impl.bullet.math.Converter;
import dev.lazurite.toolbox.api.math.QuaternionHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import java.util.Random;

public class QuadcopterItem extends Item implements IAnimatable {
    private final AnimationFactory factory = new AnimationFactory(this);

    public QuadcopterItem(Settings settings) {
        super(settings);
    }

    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        HitResult hitResult = raycast(world, user, RaycastContext.FluidHandling.NONE);

        if (world.isClient()) {
            return TypedActionResult.success(itemStack);
        } else {
            QuadcopterEntity entity = new QuadcopterEntity(Quadz.QUADCOPTER_ENTITY, world);
            QuadcopterState.fromStack(itemStack).ifPresent(entity::copyFrom);

            if (hitResult.getType() == HitResult.Type.BLOCK) {
                entity.updatePosition(hitResult.getPos().x, hitResult.getPos().y, hitResult.getPos().z);
                entity.getRigidBody().setPhysicsRotation(Converter.toBullet(QuaternionHelper.rotateY(Converter.toMinecraft(new Quaternion()), -user.getYaw())));
            } else {
                Random random = new Random();
                Vec3d direction = hitResult.getPos().subtract(user.getPos()).add(0, user.getStandingEyeHeight(), 0).normalize();
                Vec3d pos = user.getPos().add(direction);

                entity.updatePosition(pos.x, pos.y, pos.z);
                entity.getRigidBody().setLinearVelocity(Converter.toBullet(direction).multLocal(4).multLocal(new Vector3f(1, 3, 1)));
                entity.getRigidBody().setAngularVelocity(new Vector3f(random.nextFloat() * 2, random.nextFloat() * 2, random.nextFloat() * 2));
            }

            world.spawnEntity(entity);
            itemStack.decrement(1);
            itemStack = new ItemStack(Items.AIR);
        }

        return TypedActionResult.success(itemStack);
    }

    private <P extends IAnimatable> PlayState predicate(AnimationEvent<P> event) {
        return PlayState.STOP;
    }

    @Override
    public void registerControllers(AnimationData animationData) {
        animationData.addAnimationController(new AnimationController<>(this, "quadcopter_controller", 0, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }
}
