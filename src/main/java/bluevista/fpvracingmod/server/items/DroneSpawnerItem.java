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
				default:
					return null;
			}
		}

		return null;
	}

	public static void prepSpawnedDrone(PlayerEntity user, DroneEntity drone) {
		ItemStack itemStack = user.getMainHandStack();
		Config config = ServerInitializer.SERVER_PLAYER_CONFIGS.get(user.getUuid());

		drone.setBand(			DroneSpawnerItem.getTagValue(itemStack, Config.BAND)			!= null ? DroneSpawnerItem.getTagValue(itemStack, Config.BAND).intValue()				: config.getIntOption(Config.BAND));
		drone.setChannel(		DroneSpawnerItem.getTagValue(itemStack, Config.CHANNEL)		!= null ? DroneSpawnerItem.getTagValue(itemStack, Config.CHANNEL).intValue()			: config.getIntOption(Config.CHANNEL));
		drone.setCameraAngle(	DroneSpawnerItem.getTagValue(itemStack, Config.CAMERA_ANGLE)	!= null ? DroneSpawnerItem.getTagValue(itemStack, Config.CAMERA_ANGLE).intValue()		: config.getIntOption(Config.CAMERA_ANGLE));
		drone.setFieldOfView(	DroneSpawnerItem.getTagValue(itemStack, Config.FIELD_OF_VIEW)	!= null ? DroneSpawnerItem.getTagValue(itemStack, Config.FIELD_OF_VIEW).floatValue()	: config.getFloatOption(Config.FIELD_OF_VIEW));

		// config doesn't contain values for these, setting to default values if itemStack doesn't contain the value
		drone.setNoClip(		DroneSpawnerItem.getTagValue(itemStack, Config.NO_CLIP)		!= null ? DroneSpawnerItem.getTagValue(itemStack, Config.NO_CLIP).intValue()			: 0);
		drone.setPrevGodMode(	DroneSpawnerItem.getTagValue(itemStack, Config.PREV_GOD_MODE)	!= null ? DroneSpawnerItem.getTagValue(itemStack, Config.PREV_GOD_MODE).intValue()		: 0);
		drone.setGodMode(		DroneSpawnerItem.getTagValue(itemStack, Config.GOD_MODE)		!= null ? DroneSpawnerItem.getTagValue(itemStack, Config.GOD_MODE).intValue()			: 0);
	}

	public static void prepDestroyedDrone(DroneEntity drone, ItemStack itemStack) {
		DroneSpawnerItem.setTagValue(itemStack, Config.BAND,			drone.getBand());
		DroneSpawnerItem.setTagValue(itemStack, Config.CHANNEL,		drone.getChannel());
		DroneSpawnerItem.setTagValue(itemStack, Config.CAMERA_ANGLE,	drone.getCameraAngle());
		DroneSpawnerItem.setTagValue(itemStack, Config.FIELD_OF_VIEW,	drone.getFieldOfView());
		DroneSpawnerItem.setTagValue(itemStack, Config.NO_CLIP,		drone.getNoClip());
		DroneSpawnerItem.setTagValue(itemStack, Config.PREV_GOD_MODE,	drone.getPrevGodMode());
		DroneSpawnerItem.setTagValue(itemStack, Config.GOD_MODE,		drone.getGodMode());
	}

	public static void prepDroneSpawnerItem(PlayerEntity user, ItemStack itemStack) {
		Config config = ServerInitializer.SERVER_PLAYER_CONFIGS.get(user.getUuid());

		String[] keys = {Config.BAND, Config.CHANNEL, Config.CAMERA_ANGLE, Config.FIELD_OF_VIEW};

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
