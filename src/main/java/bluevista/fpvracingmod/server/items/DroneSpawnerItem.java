package bluevista.fpvracingmod.server.items;

import bluevista.fpvracingmod.config.Config;
import bluevista.fpvracingmod.server.ServerInitializer;
import bluevista.fpvracingmod.server.entities.DroneEntity;
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

public class DroneSpawnerItem extends Item {

	public DroneSpawnerItem(Settings settings) {
		super(settings);
	}

	/**
	 * Called when this item is used while targeting a Block
	 */
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack itemStack = user.getStackInHand(hand);
		HitResult hitResult = raycast(world, user, RaycastContext.FluidHandling.NONE);

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

	public static void setTagValue(ItemStack itemStack, String key, Number value) {
		if (value != null) {
			switch (key) {
				case Config.BAND:
					itemStack.getOrCreateSubTag(ServerInitializer.MODID).putInt(Config.BAND, value.intValue());
					break;
				case Config.CHANNEL:
					itemStack.getOrCreateSubTag(ServerInitializer.MODID).putInt(Config.CHANNEL, value.intValue());
					break;
				case Config.CAMERA_ANGLE:
					itemStack.getOrCreateSubTag(ServerInitializer.MODID).putInt(Config.CAMERA_ANGLE, value.intValue());
					break;
				case Config.FIELD_OF_VIEW:
					itemStack.getOrCreateSubTag(ServerInitializer.MODID).putFloat(Config.FIELD_OF_VIEW, value.floatValue());
					break;
				case Config.NO_CLIP:
					itemStack.getOrCreateSubTag(ServerInitializer.MODID).putInt(Config.NO_CLIP, value.intValue());

					if (getTagValue(itemStack, Config.NO_CLIP).intValue() == 1) {
						setTagValue(itemStack, Config.PREV_GOD_MODE, getTagValue(itemStack, Config.GOD_MODE));
						setTagValue(itemStack, Config.GOD_MODE, 1);
					} else {
						setTagValue(itemStack, Config.GOD_MODE, getTagValue(itemStack, Config.PREV_GOD_MODE));
					}
					break;
				case Config.PREV_GOD_MODE:
					itemStack.getOrCreateSubTag(ServerInitializer.MODID).putInt(Config.PREV_GOD_MODE, value.intValue());
					break;
				case Config.GOD_MODE:
					itemStack.getOrCreateSubTag(ServerInitializer.MODID).putInt(Config.GOD_MODE, value.intValue());
					break;
				case Config.RATE:
					itemStack.getOrCreateSubTag(ServerInitializer.MODID).putFloat(Config.RATE, value.floatValue());
					break;
				case Config.SUPER_RATE:
					itemStack.getOrCreateSubTag(ServerInitializer.MODID).putFloat(Config.SUPER_RATE, value.floatValue());
					break;
				case Config.EXPO:
					itemStack.getOrCreateSubTag(ServerInitializer.MODID).putFloat(Config.EXPO, value.floatValue());
					break;
				default:
					break;
			}

		} else {
			setTagValue(itemStack, key, 0);
		}
	}

	public static Number getTagValue(ItemStack itemStack, String key) {
		if (itemStack.getSubTag(ServerInitializer.MODID) != null && itemStack.getSubTag(ServerInitializer.MODID).contains(key)) {
			CompoundTag tag = itemStack.getSubTag(ServerInitializer.MODID);
			switch (key) {
				case Config.BAND:
					return tag.getInt(Config.BAND);
				case Config.CHANNEL:
					return tag.getInt(Config.CHANNEL);
				case Config.CAMERA_ANGLE:
					return tag.getInt(Config.CAMERA_ANGLE);
				case Config.FIELD_OF_VIEW:
					return tag.getFloat(Config.FIELD_OF_VIEW);
				case Config.NO_CLIP:
					return tag.getInt(Config.NO_CLIP);
				case Config.PREV_GOD_MODE:
					return tag.getInt(Config.PREV_GOD_MODE);
				case Config.GOD_MODE:
					return tag.getInt(Config.GOD_MODE);
				case Config.RATE:
					return tag.getFloat(Config.RATE);
				case Config.SUPER_RATE:
					return tag.getFloat(Config.SUPER_RATE);
				case Config.EXPO:
					return tag.getFloat(Config.EXPO);
				default:
					return null;
			}
		}

		return null;
	}

	public static void prepSpawnedDrone(PlayerEntity user, DroneEntity drone) {
		ItemStack itemStack = user.getMainHandStack();
		Config config = ServerInitializer.SERVER_PLAYER_CONFIGS.get(user.getUuid());

		drone.setConfigValues(Config.BAND,			DroneSpawnerItem.getTagValue(itemStack, Config.BAND)			!= null ? DroneSpawnerItem.getTagValue(itemStack, Config.BAND)			: config.getOption(Config.BAND));
		drone.setConfigValues(Config.CHANNEL,		DroneSpawnerItem.getTagValue(itemStack, Config.CHANNEL)			!= null ? DroneSpawnerItem.getTagValue(itemStack, Config.CHANNEL)		: config.getOption(Config.CHANNEL));
		drone.setConfigValues(Config.CAMERA_ANGLE,	DroneSpawnerItem.getTagValue(itemStack, Config.CAMERA_ANGLE)	!= null ? DroneSpawnerItem.getTagValue(itemStack, Config.CAMERA_ANGLE)	: config.getOption(Config.CAMERA_ANGLE));
		drone.setConfigValues(Config.FIELD_OF_VIEW,	DroneSpawnerItem.getTagValue(itemStack, Config.FIELD_OF_VIEW)	!= null ? DroneSpawnerItem.getTagValue(itemStack, Config.FIELD_OF_VIEW)	: config.getOption(Config.FIELD_OF_VIEW));
		drone.setConfigValues(Config.RATE,			DroneSpawnerItem.getTagValue(itemStack, Config.RATE)			!= null ? DroneSpawnerItem.getTagValue(itemStack, Config.RATE)			: config.getOption(Config.RATE));
		drone.setConfigValues(Config.SUPER_RATE,	DroneSpawnerItem.getTagValue(itemStack, Config.SUPER_RATE)		!= null ? DroneSpawnerItem.getTagValue(itemStack, Config.SUPER_RATE)	: config.getOption(Config.SUPER_RATE));
		drone.setConfigValues(Config.EXPO,			DroneSpawnerItem.getTagValue(itemStack, Config.EXPO)			!= null ? DroneSpawnerItem.getTagValue(itemStack, Config.EXPO)			: config.getOption(Config.EXPO));

		// config doesn't contain values for these, setting to default values if itemStack doesn't contain the value
		drone.setConfigValues(Config.NO_CLIP,		DroneSpawnerItem.getTagValue(itemStack, Config.NO_CLIP)			!= null ? DroneSpawnerItem.getTagValue(itemStack, Config.NO_CLIP)		: 0);
		drone.setConfigValues(Config.PREV_GOD_MODE,	DroneSpawnerItem.getTagValue(itemStack, Config.PREV_GOD_MODE)	!= null ? DroneSpawnerItem.getTagValue(itemStack, Config.PREV_GOD_MODE)	: 0);
		drone.setConfigValues(Config.GOD_MODE,		DroneSpawnerItem.getTagValue(itemStack, Config.GOD_MODE)		!= null ? DroneSpawnerItem.getTagValue(itemStack, Config.GOD_MODE)		: 0);
	}

	public static void prepDestroyedDrone(DroneEntity drone, ItemStack itemStack) {
		DroneSpawnerItem.setTagValue(itemStack, Config.BAND,			drone.getConfigValues(Config.BAND));
		DroneSpawnerItem.setTagValue(itemStack, Config.CHANNEL,			drone.getConfigValues(Config.CHANNEL));
		DroneSpawnerItem.setTagValue(itemStack, Config.CAMERA_ANGLE,	drone.getConfigValues(Config.CAMERA_ANGLE));
		DroneSpawnerItem.setTagValue(itemStack, Config.FIELD_OF_VIEW,	drone.getConfigValues(Config.FIELD_OF_VIEW));
		DroneSpawnerItem.setTagValue(itemStack, Config.NO_CLIP,			drone.getConfigValues(Config.NO_CLIP));
		DroneSpawnerItem.setTagValue(itemStack, Config.PREV_GOD_MODE,	drone.getConfigValues(Config.PREV_GOD_MODE));
		DroneSpawnerItem.setTagValue(itemStack, Config.GOD_MODE,		drone.getConfigValues(Config.GOD_MODE));
		DroneSpawnerItem.setTagValue(itemStack, Config.RATE,			drone.getConfigValues(Config.RATE));
		DroneSpawnerItem.setTagValue(itemStack, Config.SUPER_RATE,		drone.getConfigValues(Config.SUPER_RATE));
		DroneSpawnerItem.setTagValue(itemStack, Config.EXPO,			drone.getConfigValues(Config.EXPO));
	}

	public static void prepDroneSpawnerItem(PlayerEntity user, ItemStack itemStack) {
		Config config = ServerInitializer.SERVER_PLAYER_CONFIGS.get(user.getUuid());

		String[] keys = {Config.BAND, Config.CHANNEL, Config.CAMERA_ANGLE, Config.FIELD_OF_VIEW, Config.RATE, Config.SUPER_RATE, Config.EXPO};

		for (String key : keys) {
			if (DroneSpawnerItem.getTagValue(itemStack, key) == null) {
				DroneSpawnerItem.setTagValue(itemStack, key, config.getOption(key));
			}
		}

		// config doesn't contain values for these, setting to default values
		keys = new String[] {Config.NO_CLIP, Config.PREV_GOD_MODE, Config.GOD_MODE};

		for (String key : keys) {
			if (DroneSpawnerItem.getTagValue(itemStack, key) == null) {
				DroneSpawnerItem.setTagValue(itemStack, key, 0);
			}
		}
	}
}
