package dev.lazurite.quadz.common.item.quads;

import com.jme3.math.Quaternion;
import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.common.entity.quads.VoxelRacerOneEntity;
import dev.lazurite.quadz.common.item.container.QuadcopterContainer;
import dev.lazurite.rayon.impl.util.math.QuaternionHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

public class VoxelRacerOneItem extends Item implements IAnimatable {
	public AnimationFactory factory = new AnimationFactory(this);

	public VoxelRacerOneItem(Settings settings) {
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
				VoxelRacerOneEntity entity = new VoxelRacerOneEntity(Quadz.VOXEL_RACER_ONE, world);
				entity.updatePosition(hitResult.getPos().x, hitResult.getPos().y, hitResult.getPos().z);
				entity.getRigidBody().setPhysicsRotation(QuaternionHelper.rotateY(new Quaternion(), -user.yaw));

				QuadcopterContainer item = Quadz.QUADCOPTER_CONTAINER.get(itemStack);
				item.setCameraAngle(50);

				CompoundTag tag = new CompoundTag();
				item.writeToNbt(tag);
				entity.readCustomDataFromTag(tag);
				entity.getRigidBody().prioritize(user);

				world.spawnEntity(entity);
			}

			itemStack.decrement(1);
			itemStack = new ItemStack(Items.AIR);
		}

		return TypedActionResult.success(itemStack);
	}

	@Override
	public void registerControllers(AnimationData animationData) { }

	@Override
	public AnimationFactory getFactory() {
		return this.factory;
	}
}
