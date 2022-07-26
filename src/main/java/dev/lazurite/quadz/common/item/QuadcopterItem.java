package dev.lazurite.quadz.common.item;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.common.data.model.Bindable;
import dev.lazurite.quadz.common.data.model.Templated;
import dev.lazurite.quadz.common.entity.QuadcopterEntity;
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
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import java.util.Random;

// Quadz specific
public class QuadcopterItem extends Item implements IAnimatable {
    private final AnimationFactory factory = new AnimationFactory(this);

    public QuadcopterItem(Item.Properties properties) {
        super(properties);
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        var itemStack = player.getItemInHand(interactionHand);
        final var hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);

        if (level.isClientSide()) {
            return InteractionResultHolder.success(itemStack);
        } else {
            final var entity = new QuadcopterEntity(Quadz.QUADCOPTER_ENTITY, level);
            Templated.get(itemStack).ifPresent(entity::copyFrom);
            Bindable.get(itemStack).ifPresent(entity::copyFrom);

            if (hitResult.getType() == HitResult.Type.BLOCK) {
                entity.absMoveTo(hitResult.getLocation().x, hitResult.getLocation().y, hitResult.getLocation().z);
                entity.getRigidBody().setPhysicsRotation(Convert.toBullet(QuaternionHelper.rotateY(Convert.toMinecraft(new Quaternion()), -player.getYRot())));
            } else {
                final var random = new Random();
                final var direction = hitResult.getLocation().subtract(player.position()).add(0, player.getEyeHeight(), 0).normalize();
                final var pos = player.position().add(direction);

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
        animationData.addAnimationController(new AnimationController<>(this, "quadcopter_item_controller", 0, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }
}
