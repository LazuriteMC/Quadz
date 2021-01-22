package dev.lazurite.fpvracing.common.item.quadcopter;

import dev.lazurite.fpvracing.FPVRacing;
import dev.lazurite.fpvracing.common.entity.VoxelRacerOne;
import dev.lazurite.fpvracing.common.item.container.QuadcopterContainer;
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

public class VoxelRacerOneItem extends Item {
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
				VoxelRacerOne entity = new VoxelRacerOne(FPVRacing.VOXEL_RACER_ONE, world);
				entity.updatePositionAndAngles(hitResult.getPos().x, hitResult.getPos().y, hitResult.getPos().z, user.yaw, 0);

				QuadcopterContainer item = QuadcopterContainer.get(itemStack);
				CompoundTag tag = new CompoundTag();
				item.writeToNbt(tag);
				entity.readCustomDataFromTag(tag);

				world.spawnEntity(entity);
			}

			itemStack.decrement(1);
			itemStack = new ItemStack(Items.AIR);
		}

		return TypedActionResult.success(itemStack);
	}
}
