package dev.lazurite.quadz.common.item;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import dev.lazurite.quadz.common.state.QuadcopterState;
import dev.lazurite.quadz.common.state.entity.QuadcopterEntity;
import dev.lazurite.rayon.core.impl.util.math.QuaternionHelper;
import dev.lazurite.rayon.core.impl.util.math.VectorHelper;
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

import java.util.Random;

public class QuadcopterItem extends Item {
    public QuadcopterItem(Settings settings) {
        super(settings);
    }

    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        HitResult hitResult = raycast(world, user, RaycastContext.FluidHandling.NONE);

        if (world.isClient()) {
            return TypedActionResult.success(itemStack);
        } else {
            QuadcopterEntity entity = new QuadcopterEntity(world);
            QuadcopterState.fromStack(itemStack).ifPresent(entity::copyFrom);

            if (hitResult.getType() == HitResult.Type.BLOCK) {
                entity.updatePosition(hitResult.getPos().x, hitResult.getPos().y, hitResult.getPos().z);
                entity.getRigidBody().setPhysicsRotation(QuaternionHelper.rotateY(new Quaternion(), -user.yaw));
            } else {
                Random random = new Random();
                Vec3d direction = hitResult.getPos().subtract(user.getPos()).add(0, user.getStandingEyeHeight(), 0).normalize();
                Vec3d pos = user.getPos().add(direction);

                entity.updatePosition(pos.x, pos.y, pos.z);
                entity.getRigidBody().setLinearVelocity(VectorHelper.vec3dToVector3f(direction).multLocal(4).multLocal(new Vector3f(1, 3, 1)));
                entity.getRigidBody().setAngularVelocity(new Vector3f(random.nextFloat() * 2, random.nextFloat() * 2, random.nextFloat() * 2));
            }

            world.spawnEntity(entity);
            itemStack.decrement(1);
            itemStack = new ItemStack(Items.AIR);
        }

        return TypedActionResult.success(itemStack);
    }
}
