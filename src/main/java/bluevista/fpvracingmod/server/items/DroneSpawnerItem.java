package bluevista.fpvracingmod.server.items;

import bluevista.fpvracingmod.config.Config;
import bluevista.fpvracingmod.server.ServerInitializer;
import bluevista.fpvracingmod.server.entities.DroneEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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
		HitResult hitResult = rayTrace(world, user, RayTraceContext.FluidHandling.NONE);

		if (!world.isClient()) {
			if (hitResult.getType() == HitResult.Type.MISS)
				return TypedActionResult.pass(itemStack);

			if (hitResult.getType() == HitResult.Type.BLOCK) {
				DroneEntity drone = DroneEntity.create(user.getUuid(), world, hitResult.getPos(), 180f - user.yaw);
				prepSpawnedDrone(user, drone);

				itemStack.decrement(1);
				itemStack = new ItemStack(Items.AIR);
			}
		}

		return TypedActionResult.success(itemStack);
	}

	public static void setValue(ItemStack itemStack, String key, Number value) {
		switch (key) {
			case Config.BAND:
				setBand(itemStack, value.intValue());
				break;
			case Config.CHANNEL:
				setChannel(itemStack, value.intValue());
				break;
			case Config.CAMERA_ANGLE:
				setCameraAngle(itemStack, value.intValue());
				break;
			case Config.FIELD_OF_VIEW:
				setFieldOfView(itemStack, value.floatValue());
			case Config.NO_CLIP:
				setNoClip(itemStack, value.intValue());
				break;
			case Config.PREV_GOD_MODE:
				setPrevGodMode(itemStack, value.intValue());
				break;
			case Config.GOD_MODE:
				setGodMode(itemStack, value.intValue());
				break;
			default:
				break;
		}
	}

	public static Number getValue(ItemStack itemStack, String key) {
		switch (key) {
			case Config.BAND:
				return getBand(itemStack);
			case Config.CHANNEL:
				return getChannel(itemStack);
			case Config.CAMERA_ANGLE:
				return getCameraAngle(itemStack);
			case Config.FIELD_OF_VIEW:
				return getFieldOfView(itemStack);
			case Config.NO_CLIP:
				return getNoClip(itemStack);
			case Config.PREV_GOD_MODE:
				return getPrevGodMode(itemStack);
			case Config.GOD_MODE:
				return getGodMode(itemStack);
			default:
				return null; // 0?
		}
	}

	public static void setBand(ItemStack itemStack, int band) {
		itemStack.getOrCreateSubTag(Config.FREQUENCY).putInt(Config.BAND, band);
	}

	public static int getBand(ItemStack itemStack) {
		if (itemStack.getSubTag(Config.FREQUENCY) != null && itemStack.getSubTag(Config.FREQUENCY).contains(Config.BAND)) {
			return itemStack.getSubTag(Config.FREQUENCY).getInt(Config.BAND);
		}
		return 0;
	}

	public static void setChannel(ItemStack itemStack, int channel) {
		itemStack.getOrCreateSubTag(Config.FREQUENCY).putInt(Config.CHANNEL, channel);
	}

	public static int getChannel(ItemStack itemStack) {
		if (itemStack.getSubTag(Config.FREQUENCY) != null && itemStack.getSubTag(Config.FREQUENCY).contains(Config.CHANNEL)) {
			return itemStack.getSubTag(Config.FREQUENCY).getInt(Config.CHANNEL);
		}
		return 0;
	}

	public static void setCameraAngle(ItemStack itemStack, int angle) {
		itemStack.getOrCreateSubTag(Config.MISC).putInt(Config.CAMERA_ANGLE, angle);
	}

	public static int getCameraAngle(ItemStack itemStack) {
		if (itemStack.getSubTag(Config.MISC) != null && itemStack.getSubTag(Config.MISC).contains(Config.CAMERA_ANGLE)) {
			return itemStack.getSubTag(Config.MISC).getInt(Config.CAMERA_ANGLE);
		}
		return 0;
	}

	public static void setFieldOfView(ItemStack itemStack, float fieldOfView) {
		itemStack.getOrCreateSubTag(Config.MISC).putFloat(Config.FIELD_OF_VIEW, fieldOfView);
	}

	public static float getFieldOfView(ItemStack itemStack) {
		if (itemStack.getSubTag(Config.MISC) != null && itemStack.getSubTag(Config.MISC).contains(Config.FIELD_OF_VIEW)) {
			return itemStack.getSubTag(Config.MISC).getFloat(Config.FIELD_OF_VIEW);
		}
		return 0;
	}

	public static void setNoClip(ItemStack itemStack, int noClip) {
		itemStack.getOrCreateSubTag(Config.MISC).putInt(Config.NO_CLIP, noClip);
		if (getNoClip(itemStack) == 1) {
			setPrevGodMode(itemStack, getGodMode(itemStack));
			setGodMode(itemStack, getNoClip(itemStack));
		} else {
			setGodMode(itemStack, getPrevGodMode(itemStack));
		}
	}

	public static int getNoClip(ItemStack itemStack) {
		if (itemStack.getSubTag(Config.MISC) != null && itemStack.getSubTag(Config.MISC).contains(Config.NO_CLIP)) {
			return itemStack.getSubTag(Config.MISC).getInt(Config.NO_CLIP);
		}
		return 0;
	}

	public static void setPrevGodMode(ItemStack itemStack, int godMode) {
		itemStack.getOrCreateSubTag(Config.MISC).putInt(Config.PREV_GOD_MODE, godMode);
	}

	public static int getPrevGodMode(ItemStack itemStack) {
		if (itemStack.getSubTag(Config.MISC) != null && itemStack.getSubTag(Config.MISC).contains(Config.PREV_GOD_MODE)) {
			return itemStack.getSubTag(Config.MISC).getInt(Config.PREV_GOD_MODE);
		}
		return 0;
	}

	public static void setGodMode(ItemStack itemStack, int godMode) {
		itemStack.getOrCreateSubTag(Config.MISC).putInt(Config.GOD_MODE, godMode);
	}

	public static int getGodMode(ItemStack itemStack) {
		if (itemStack.getSubTag(Config.MISC) != null && itemStack.getSubTag(Config.MISC).contains(Config.GOD_MODE)) {
			return itemStack.getSubTag(Config.MISC).getInt(Config.GOD_MODE);
		}
		return 0;
	}

	private void prepSpawnedDrone(PlayerEntity user, DroneEntity drone) {
		ItemStack itemStack = user.getMainHandStack();
		Config config = ServerInitializer.SERVER_PLAYER_CONFIGS.get(user.getUuid());

		if (itemStack.getSubTag(Config.FREQUENCY) != null && itemStack.getSubTag(Config.FREQUENCY).contains(Config.BAND)) {
			drone.setBand(itemStack.getSubTag(Config.FREQUENCY).getInt(Config.BAND));
		} else {
			drone.setBand(config.getIntOption(Config.BAND));
		}

		if (itemStack.getSubTag(Config.FREQUENCY) != null && itemStack.getSubTag(Config.FREQUENCY).contains(Config.CHANNEL)) {
			drone.setChannel(itemStack.getSubTag(Config.FREQUENCY).getInt(Config.CHANNEL));
		} else {
			drone.setChannel(config.getIntOption(Config.CHANNEL));
		}

		if (itemStack.getSubTag(Config.MISC) != null && itemStack.getSubTag(Config.MISC).contains(Config.CAMERA_ANGLE)) {
			drone.setCameraAngle(itemStack.getSubTag(Config.MISC).getInt(Config.CAMERA_ANGLE));
		} else {
			drone.setCameraAngle(config.getIntOption(Config.CAMERA_ANGLE));
		}

		if (itemStack.getSubTag(Config.MISC) != null && itemStack.getSubTag(Config.MISC).contains(Config.FIELD_OF_VIEW)) {
			drone.setFieldOfView(itemStack.getSubTag(Config.MISC).getInt(Config.FIELD_OF_VIEW));
		} else {
			drone.setFieldOfView(config.getFloatOption(Config.FIELD_OF_VIEW));
		}

		if (itemStack.getSubTag(Config.MISC) != null && itemStack.getSubTag(Config.MISC).contains(Config.NO_CLIP)) {
			drone.setNoClip(itemStack.getSubTag(Config.MISC).getInt(Config.NO_CLIP));
		}

		if (itemStack.getSubTag(Config.MISC) != null && itemStack.getSubTag(Config.MISC).contains(Config.PREV_GOD_MODE)) {
			drone.setPrevGodMode(itemStack.getSubTag(Config.MISC).getInt(Config.PREV_GOD_MODE));
		}

		if (itemStack.getSubTag(Config.MISC) != null && itemStack.getSubTag(Config.MISC).contains(Config.GOD_MODE)) {
			drone.setGodMode(itemStack.getSubTag(Config.MISC).getInt(Config.GOD_MODE));
		}
	}

	public static void prepDestroyedDrone(DroneEntity drone, ItemStack itemStack) {
		DroneSpawnerItem.setBand(itemStack, drone.getBand());
		DroneSpawnerItem.setChannel(itemStack, drone.getChannel());
		DroneSpawnerItem.setCameraAngle(itemStack, drone.getCameraAngle());
		DroneSpawnerItem.setFieldOfView(itemStack, drone.getFieldOfView());
		DroneSpawnerItem.setNoClip(itemStack, drone.getNoClip());
		DroneSpawnerItem.setPrevGodMode(itemStack, drone.getPrevGodMode());
		DroneSpawnerItem.setGodMode(itemStack, drone.getGodMode());
	}
}

