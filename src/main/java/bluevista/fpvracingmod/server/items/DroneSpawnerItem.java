package bluevista.fpvracingmod.server.items;

import bluevista.fpvracingmod.client.ClientInitializer;
import bluevista.fpvracingmod.client.math.QuaternionHelper;
import bluevista.fpvracingmod.server.entities.DroneEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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

				if (itemStack.getSubTag("frequency") != null) {
					drone.setBand(itemStack.getSubTag("frequency").getInt("band"));
				} else {
					drone.setBand(0);
				}

				if (itemStack.getSubTag("frequency") != null) {
					drone.setChannel(itemStack.getSubTag("frequency").getInt("channel"));
				} else {
					drone.setChannel(0);
				}

				if(itemStack.getSubTag("misc") != null) {
					drone.setCameraAngle(itemStack.getSubTag("misc").getInt("cameraAngle"));
				} else {
					drone.setCameraAngle(20);
				}

				if (!user.abilities.creativeMode) {
					itemStack.decrement(1);
				}
			}
		}

		return TypedActionResult.success(itemStack);
	}

	public static void setBand(ItemStack itemStack, int band) {
		itemStack.getOrCreateSubTag("frequency").putInt("band", band);
	}

	public static void setChannel(ItemStack itemStack, int channel) {
		itemStack.getOrCreateSubTag("frequency").putInt("channel", channel);
	}

	public static void setCameraAngle(ItemStack itemStack, int angle) {
		itemStack.getOrCreateSubTag("misc").putInt("cameraAngle", angle);
	}
}

