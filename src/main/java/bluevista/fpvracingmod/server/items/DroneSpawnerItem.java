package bluevista.fpvracingmod.server.items;

import bluevista.fpvracingmod.client.math.QuaternionHelper;
import bluevista.fpvracingmod.server.ServerTick;
import bluevista.fpvracingmod.server.entities.DroneEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.RayTraceContext;
import net.minecraft.world.World;

public class DroneSpawnerItem extends Item {

	public DroneSpawnerItem(Settings settings) {
		super(settings);
	}

	/**
	 * Called when this item is used while targeting a Block
	 */
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack itemStack = user.getStackInHand(hand);
		HitResult hitResult = rayTrace(world, user, RayTraceContext.FluidHandling.ANY);

		if (!world.isClient()) {
			if (hitResult.getType() == net.minecraft.util.hit.HitResult.Type.MISS)
				return TypedActionResult.pass(itemStack);

			if (hitResult.getType() == net.minecraft.util.hit.HitResult.Type.BLOCK) {
				DroneEntity drone = DroneEntity.create(world, hitResult.getPos());
				QuaternionHelper.rotateY(drone.getOrientation(), 180f - user.yaw);

				if (!user.abilities.creativeMode) {
					itemStack.decrement(1);
				}
			}
		}

		return TypedActionResult.success(itemStack);
	}
	
}

