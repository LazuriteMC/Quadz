package dev.lazurite.quadz.common.item;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.common.state.Quadcopter;
import dev.lazurite.quadz.common.state.entity.QuadcopterEntity;
import dev.lazurite.rayon.impl.bullet.math.Convert;
import dev.lazurite.toolbox.api.math.QuaternionHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import java.util.Random;

public class QuadcopterItem extends Item implements IAnimatable {
    private final AnimationFactory factory = new AnimationFactory(this);

    public QuadcopterItem(Item.Properties properties) {
        super(properties);
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        HitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);

        if (level.isClientSide()) {
            return InteractionResultHolder.success(itemStack);
        } else {
            QuadcopterEntity entity = new QuadcopterEntity(Quadz.QUADCOPTER_ENTITY, level);
            Quadcopter.fromStack(itemStack).ifPresent(entity::copyFrom);

            if (hitResult.getType() == HitResult.Type.BLOCK) {
                entity.absMoveTo(hitResult.getLocation().x, hitResult.getLocation().y, hitResult.getLocation().z);
                entity.getRigidBody().setPhysicsRotation(Convert.toBullet(QuaternionHelper.rotateY(Convert.toMinecraft(new Quaternion()), -player.getYRot())));
            } else {
                Random random = new Random();
                Vec3 direction = hitResult.getLocation().subtract(player.position()).add(0, player.getEyeHeight(), 0).normalize();
                Vec3 pos = player.position().add(direction);

                entity.absMoveTo(pos.x, pos.y, pos.z);
                entity.getRigidBody().setLinearVelocity(Convert.toBullet(direction).multLocal(4).multLocal(new Vector3f(1, 3, 1)));
                entity.getRigidBody().setAngularVelocity(new Vector3f(random.nextFloat() * 2, random.nextFloat() * 2, random.nextFloat() * 2));
            }

            level.addFreshEntity(entity);
            itemStack.shrink(1);
            itemStack = new ItemStack(Items.AIR);
        }

        return InteractionResultHolder.success(itemStack);
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
