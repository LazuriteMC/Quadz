package bluevista.fpvracingmod.server.items;

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
			if (hitResult.getType() == HitResult.Type.MISS)
				return TypedActionResult.pass(itemStack);

			if (hitResult.getType() == HitResult.Type.BLOCK) {
				DroneEntity drone = DroneEntity.create(world, hitResult.getPos(), 180f - user.yaw);

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
					drone.setCameraAngle(0); // default to 0
				}

//				if (!user.abilities.creativeMode) {
					itemStack.decrement(1);
//				}
			}
		}

		return TypedActionResult.success(itemStack);
	}

	public static void setValue(ItemStack itemStack, String key, Number value) {
		switch (key) {
			case "band":
				setBand(itemStack, value.intValue());
				break;
			case "channel":
				setChannel(itemStack, value.intValue());
				break;
			case "cameraAngle":
				setCameraAngle(itemStack, value.intValue());
				break;
			default:
				break;
		}
	}

	public static int getValue(ItemStack itemStack, String key) {
		switch (key) {
			case "band":
				return getBand(itemStack);
			case "channel":
				return getChannel(itemStack);
			case "cameraAngle":
				return getCameraAngle(itemStack);
			default:
				return 0; // unknown key, default value
		}
	}

	public static void setBand(ItemStack itemStack, int band) {
		itemStack.getOrCreateSubTag("frequency").putInt("band", band);
	}

	public static int getBand(ItemStack itemStack) {
		if (itemStack.getSubTag("frequency") != null) {
			return itemStack.getSubTag("frequency").getInt("band");
		}
		return 0;
	}

	public static void setChannel(ItemStack itemStack, int channel) {
		itemStack.getOrCreateSubTag("frequency").putInt("channel", channel);
	}

	public static int getChannel(ItemStack itemStack) {
		if (itemStack.getSubTag("frequency") != null) {
			return itemStack.getSubTag("frequency").getInt("channel");
		}
		return 0;
	}

	public static void setCameraAngle(ItemStack itemStack, int angle) {
		itemStack.getOrCreateSubTag("misc").putInt("cameraAngle", angle);
	}

	public static int getCameraAngle(ItemStack itemStack) {
		if (itemStack.getSubTag("misc") != null) {
			return itemStack.getSubTag("misc").getInt("cameraAngle");
		}
		return 0;
	}
}

