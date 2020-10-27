package bluevista.fpvracing.server.items;

import bluevista.fpvracing.config.Config;
import bluevista.fpvracing.network.datatracker.FlyableTrackerRegistry;
import bluevista.fpvracing.server.ServerInitializer;
import bluevista.fpvracing.server.entities.FlyableEntity;
import bluevista.fpvracing.server.entities.QuadcopterEntity;
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
		FlyableTrackerRegistry.getAll(FlyableEntity.class).forEach(entry -> FlyableTrackerRegistry.writeToTag(tag, (FlyableTrackerRegistry.Entry) entry, entry.getDataType().fromConfig(config, entry.getName())));
		FlyableTrackerRegistry.getAll(QuadcopterEntity.class).forEach(entry -> FlyableTrackerRegistry.writeToTag(tag, (FlyableTrackerRegistry.Entry) entry, entry.getDataType().fromConfig(config, entry.getName())));
	}
}
