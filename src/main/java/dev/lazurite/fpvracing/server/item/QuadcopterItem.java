package dev.lazurite.fpvracing.server.item;

import dev.lazurite.fpvracing.network.tracker.Config;
import dev.lazurite.fpvracing.network.tracker.GenericDataTrackerRegistry;
import dev.lazurite.fpvracing.server.ServerInitializer;
import dev.lazurite.fpvracing.server.entity.flyable.QuadcopterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import javax.vecmath.Vector3f;

public class QuadcopterItem extends Item {

	public QuadcopterItem(Settings settings) {
		super(settings);
	}

	/**
	 * Called when this item is used while targeting a Block
	 */
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack itemStack = user.getStackInHand(hand);
		HitResult hitResult = raycast(world, user, RaycastContext.FluidHandling.NONE);

		if (!(world instanceof ServerWorld)) {
			return TypedActionResult.success(itemStack);
		} else if (hitResult.getType() == HitResult.Type.MISS) {
			return TypedActionResult.pass(itemStack);
		} else {
			if (hitResult.getType() == HitResult.Type.BLOCK) {
				QuadcopterEntity drone = new QuadcopterEntity(ServerInitializer.QUADCOPTER_ENTITY, world);
				drone.updatePositionAndAngles(new Vector3f((float) hitResult.getPos().x, (float) hitResult.getPos().y + 1.0f, (float) hitResult.getPos().z), user.yaw, 0);
				drone.readTagFromSpawner(user.getMainHandStack(), user);
				world.spawnEntity(drone);
			}

			itemStack.decrement(1);
			itemStack = new ItemStack(Items.AIR);
		}

		return TypedActionResult.success(itemStack);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public static void writeToTag(ItemStack itemStack, PlayerEntity user) {
		Config config = ServerInitializer.SERVER_PLAYER_CONFIGS.get(user.getUuid());

		CompoundTag tag = itemStack.getOrCreateSubTag(ServerInitializer.MODID);
		GenericDataTrackerRegistry.getAll(QuadcopterEntity.class).forEach(entry -> {
			if (!tag.contains(entry.getKey().getName())) {
				GenericDataTrackerRegistry.writeToTag(tag, (GenericDataTrackerRegistry.Entry) entry, entry.getKey().getType().fromConfig(config, entry.getKey().getName()));
			}
		});
	}
}
