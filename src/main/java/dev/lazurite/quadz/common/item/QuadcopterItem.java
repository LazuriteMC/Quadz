package dev.lazurite.quadz.common.item;

import com.jme3.math.Quaternion;
import dev.lazurite.quadz.common.state.QuadcopterState;
import dev.lazurite.quadz.common.state.entity.QuadcopterEntity;
import dev.lazurite.rayon.core.impl.util.math.QuaternionHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

public class QuadcopterItem extends Item {
    public QuadcopterItem(Settings settings) {
        super(settings);
    }

    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        HitResult hitResult = raycast(world, user, RaycastContext.FluidHandling.NONE);

        if (world.isClient()) {
            return TypedActionResult.success(itemStack);
        } else if (hitResult.getType() == HitResult.Type.MISS) {
            return TypedActionResult.pass(itemStack);
        } else {
            if (hitResult.getType() == HitResult.Type.BLOCK) {
                QuadcopterEntity entity = new QuadcopterEntity(world);

                entity.updatePosition(hitResult.getPos().x, hitResult.getPos().y, hitResult.getPos().z);
                entity.getRigidBody().setPhysicsRotation(QuaternionHelper.rotateY(new Quaternion(), -user.yaw));

                QuadcopterState.get(itemStack).ifPresent(entity::copyFrom);
                world.spawnEntity(entity);
            }

            itemStack.decrement(1);
            itemStack = new ItemStack(Items.AIR);
        }

        return TypedActionResult.success(itemStack);
    }
}
