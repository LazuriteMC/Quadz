package bluevista.fpvracing.server.items;

import bluevista.fpvracing.config.Config;
import bluevista.fpvracing.server.ServerInitializer;
import bluevista.fpvracing.server.entities.DroneEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
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

		if (!(world instanceof ServerWorld)) {
			return TypedActionResult.success(itemStack);
		} else if (hitResult.getType() == HitResult.Type.MISS) {
			return TypedActionResult.pass(itemStack);
		} else {
			if (hitResult.getType() == HitResult.Type.BLOCK) {
				Vec3d pos = new Vec3d(hitResult.getPos().x, hitResult.getPos().y, hitResult.getPos().z);
				DroneEntity drone = new DroneEntity(world, user.getEntityId(), pos, user.getHeadYaw());
				prepSpawnedDrone(user, drone);
				world.spawnEntity(drone);
			}

			itemStack.decrement(1);
			itemStack = new ItemStack(Items.AIR);
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
				case Config.MASS:
					itemStack.getOrCreateSubTag(ServerInitializer.MODID).putFloat(Config.MASS, value.floatValue());
					break;
				case Config.SIZE:
					itemStack.getOrCreateSubTag(ServerInitializer.MODID).putInt(Config.SIZE, value.intValue());
					break;
				case Config.DRAG_COEFFICIENT:
					itemStack.getOrCreateSubTag(ServerInitializer.MODID).putFloat(Config.DRAG_COEFFICIENT, value.floatValue());
					break;
				case Config.THRUST:
					itemStack.getOrCreateSubTag(ServerInitializer.MODID).putFloat(Config.THRUST, value.floatValue());
					break;
				case Config.THRUST_CURVE:
					itemStack.getOrCreateSubTag(ServerInitializer.MODID).putFloat(Config.THRUST_CURVE, value.floatValue());
					break;
				case Config.DAMAGE_COEFFICIENT:
					itemStack.getOrCreateSubTag(ServerInitializer.MODID).putFloat(Config.DAMAGE_COEFFICIENT, value.floatValue());
					break;
//				case Config.CRASH_MOMENTUM_THRESHOLD:
//					itemStack.getOrCreateSubTag(ServerInitializer.MODID).putInt(Config.CRASH_MOMENTUM_THRESHOLD, value.intValue());
//					break;
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
				case Config.GOD_MODE:
					return tag.getInt(Config.GOD_MODE);
				case Config.RATE:
					return tag.getFloat(Config.RATE);
				case Config.SUPER_RATE:
					return tag.getFloat(Config.SUPER_RATE);
				case Config.EXPO:
					return tag.getFloat(Config.EXPO);
				case Config.MASS:
					return tag.getFloat(Config.MASS);
				case Config.DRAG_COEFFICIENT:
					return tag.getFloat(Config.DRAG_COEFFICIENT);
				case Config.SIZE:
					return tag.getInt(Config.SIZE);
				case Config.THRUST:
					return tag.getFloat(Config.THRUST);
				case Config.THRUST_CURVE:
					return tag.getFloat(Config.THRUST_CURVE);
				case Config.DAMAGE_COEFFICIENT:
					return tag.getFloat(Config.DAMAGE_COEFFICIENT);
//				case Config.CRASH_MOMENTUM_THRESHOLD:
//					return tag.getInt(Config.CRASH_MOMENTUM_THRESHOLD);
				default:
					return null;
			}
		}

		return null;
	}

	public static void prepSpawnedDrone(PlayerEntity user, DroneEntity drone) {
		Config config = ServerInitializer.SERVER_PLAYER_CONFIGS.get(user.getUuid());
		ItemStack itemStack = user.getMainHandStack();

		drone.setConfigValues(Config.BAND,						DroneSpawnerItem.getTagValue(itemStack, Config.BAND)						!= null ? DroneSpawnerItem.getTagValue(itemStack, Config.BAND)						: config.getOption(Config.BAND));
		drone.setConfigValues(Config.CHANNEL,					DroneSpawnerItem.getTagValue(itemStack, Config.CHANNEL)						!= null ? DroneSpawnerItem.getTagValue(itemStack, Config.CHANNEL)					: config.getOption(Config.CHANNEL));
		drone.setConfigValues(Config.CAMERA_ANGLE,				DroneSpawnerItem.getTagValue(itemStack, Config.CAMERA_ANGLE)				!= null ? DroneSpawnerItem.getTagValue(itemStack, Config.CAMERA_ANGLE)				: config.getOption(Config.CAMERA_ANGLE));
		drone.setConfigValues(Config.FIELD_OF_VIEW,				DroneSpawnerItem.getTagValue(itemStack, Config.FIELD_OF_VIEW)				!= null ? DroneSpawnerItem.getTagValue(itemStack, Config.FIELD_OF_VIEW)				: config.getOption(Config.FIELD_OF_VIEW));
		drone.setConfigValues(Config.RATE,						DroneSpawnerItem.getTagValue(itemStack, Config.RATE)						!= null ? DroneSpawnerItem.getTagValue(itemStack, Config.RATE)						: config.getOption(Config.RATE));
		drone.setConfigValues(Config.SUPER_RATE,				DroneSpawnerItem.getTagValue(itemStack, Config.SUPER_RATE)					!= null ? DroneSpawnerItem.getTagValue(itemStack, Config.SUPER_RATE)				: config.getOption(Config.SUPER_RATE));
		drone.setConfigValues(Config.EXPO,						DroneSpawnerItem.getTagValue(itemStack, Config.EXPO)						!= null ? DroneSpawnerItem.getTagValue(itemStack, Config.EXPO)						: config.getOption(Config.EXPO));
		drone.setConfigValues(Config.THRUST,					DroneSpawnerItem.getTagValue(itemStack, Config.THRUST)						!= null ? DroneSpawnerItem.getTagValue(itemStack, Config.THRUST)					: config.getOption(Config.THRUST));
		drone.setConfigValues(Config.THRUST_CURVE,				DroneSpawnerItem.getTagValue(itemStack, Config.THRUST_CURVE)				!= null ? DroneSpawnerItem.getTagValue(itemStack, Config.THRUST_CURVE)				: config.getOption(Config.THRUST_CURVE));
		drone.setConfigValues(Config.DAMAGE_COEFFICIENT,		DroneSpawnerItem.getTagValue(itemStack, Config.DAMAGE_COEFFICIENT)			!= null ? DroneSpawnerItem.getTagValue(itemStack, Config.DAMAGE_COEFFICIENT)		: config.getOption(Config.DAMAGE_COEFFICIENT));
//		drone.setConfigValues(Config.CRASH_MOMENTUM_THRESHOLD,	DroneSpawnerItem.getTagValue(itemStack, Config.CRASH_MOMENTUM_THRESHOLD)	!= null ? DroneSpawnerItem.getTagValue(itemStack, Config.CRASH_MOMENTUM_THRESHOLD)	: config.getOption(Config.CRASH_MOMENTUM_THRESHOLD));

		drone.setConfigValues(Config.MASS,				DroneSpawnerItem.getTagValue(itemStack, Config.MASS)				!= null ? DroneSpawnerItem.getTagValue(itemStack, Config.MASS)				: config.getOption(Config.MASS));
		drone.setConfigValues(Config.SIZE,				DroneSpawnerItem.getTagValue(itemStack, Config.SIZE)				!= null ? DroneSpawnerItem.getTagValue(itemStack, Config.SIZE)				: config.getOption(Config.SIZE));
		drone.setConfigValues(Config.DRAG_COEFFICIENT,	DroneSpawnerItem.getTagValue(itemStack, Config.DRAG_COEFFICIENT)	!= null ? DroneSpawnerItem.getTagValue(itemStack, Config.DRAG_COEFFICIENT)	: config.getOption(Config.DRAG_COEFFICIENT));

		// config doesn't contain values for these, setting to default values if itemStack doesn't contain the value
		drone.setConfigValues(Config.NO_CLIP,	DroneSpawnerItem.getTagValue(itemStack, Config.NO_CLIP)		!= null ? DroneSpawnerItem.getTagValue(itemStack, Config.NO_CLIP)	: 0);
		drone.setConfigValues(Config.GOD_MODE,	DroneSpawnerItem.getTagValue(itemStack, Config.GOD_MODE)	!= null ? DroneSpawnerItem.getTagValue(itemStack, Config.GOD_MODE)	: 0);
	}

	public static void prepDestroyedDrone(DroneEntity drone, ItemStack itemStack) {
		DroneSpawnerItem.setTagValue(itemStack, Config.BAND,						drone.getConfigValues(Config.BAND));
		DroneSpawnerItem.setTagValue(itemStack, Config.CHANNEL,						drone.getConfigValues(Config.CHANNEL));
		DroneSpawnerItem.setTagValue(itemStack, Config.CAMERA_ANGLE,				drone.getConfigValues(Config.CAMERA_ANGLE));
		DroneSpawnerItem.setTagValue(itemStack, Config.FIELD_OF_VIEW,				drone.getConfigValues(Config.FIELD_OF_VIEW));
		DroneSpawnerItem.setTagValue(itemStack, Config.NO_CLIP,						drone.getConfigValues(Config.NO_CLIP));
		DroneSpawnerItem.setTagValue(itemStack, Config.GOD_MODE,					drone.getConfigValues(Config.GOD_MODE));
		DroneSpawnerItem.setTagValue(itemStack, Config.RATE,						drone.getConfigValues(Config.RATE));
		DroneSpawnerItem.setTagValue(itemStack, Config.SUPER_RATE,					drone.getConfigValues(Config.SUPER_RATE));
		DroneSpawnerItem.setTagValue(itemStack, Config.EXPO,						drone.getConfigValues(Config.EXPO));
		DroneSpawnerItem.setTagValue(itemStack, Config.THRUST,						drone.getConfigValues(Config.THRUST));
		DroneSpawnerItem.setTagValue(itemStack, Config.THRUST_CURVE,				drone.getConfigValues(Config.THRUST_CURVE));
		DroneSpawnerItem.setTagValue(itemStack, Config.DAMAGE_COEFFICIENT,			drone.getConfigValues(Config.DAMAGE_COEFFICIENT));
//		DroneSpawnerItem.setTagValue(itemStack, Config.CRASH_MOMENTUM_THRESHOLD,	drone.getConfigValues(Config.CRASH_MOMENTUM_THRESHOLD));

		DroneSpawnerItem.setTagValue(itemStack, Config.MASS,				drone.getConfigValues(Config.MASS));
		DroneSpawnerItem.setTagValue(itemStack, Config.SIZE,				drone.getConfigValues(Config.SIZE));
		DroneSpawnerItem.setTagValue(itemStack, Config.DRAG_COEFFICIENT,	drone.getConfigValues(Config.DRAG_COEFFICIENT));
	}

	public static void prepDroneSpawnerItem(PlayerEntity user, ItemStack itemStack) {
		Config config = ServerInitializer.SERVER_PLAYER_CONFIGS.get(user.getUuid());

		DroneSpawnerItem.setTagValue(itemStack, Config.BAND,						DroneSpawnerItem.getTagValue(itemStack, Config.BAND)						!= null ? DroneSpawnerItem.getTagValue(itemStack, Config.BAND)						: config.getOption(Config.BAND));
		DroneSpawnerItem.setTagValue(itemStack, Config.CHANNEL,						DroneSpawnerItem.getTagValue(itemStack, Config.CHANNEL)						!= null ? DroneSpawnerItem.getTagValue(itemStack, Config.CHANNEL)					: config.getOption(Config.CHANNEL));
		DroneSpawnerItem.setTagValue(itemStack, Config.CAMERA_ANGLE,				DroneSpawnerItem.getTagValue(itemStack, Config.CAMERA_ANGLE)				!= null ? DroneSpawnerItem.getTagValue(itemStack, Config.CAMERA_ANGLE)				: config.getOption(Config.CAMERA_ANGLE));
		DroneSpawnerItem.setTagValue(itemStack, Config.FIELD_OF_VIEW,				DroneSpawnerItem.getTagValue(itemStack, Config.FIELD_OF_VIEW)				!= null ? DroneSpawnerItem.getTagValue(itemStack, Config.FIELD_OF_VIEW)				: config.getOption(Config.FIELD_OF_VIEW));
		DroneSpawnerItem.setTagValue(itemStack, Config.RATE,						DroneSpawnerItem.getTagValue(itemStack, Config.RATE)						!= null ? DroneSpawnerItem.getTagValue(itemStack, Config.RATE)						: config.getOption(Config.RATE));
		DroneSpawnerItem.setTagValue(itemStack, Config.SUPER_RATE,					DroneSpawnerItem.getTagValue(itemStack, Config.SUPER_RATE)					!= null ? DroneSpawnerItem.getTagValue(itemStack, Config.SUPER_RATE)				: config.getOption(Config.SUPER_RATE));
		DroneSpawnerItem.setTagValue(itemStack, Config.EXPO,						DroneSpawnerItem.getTagValue(itemStack, Config.EXPO)						!= null ? DroneSpawnerItem.getTagValue(itemStack, Config.EXPO)						: config.getOption(Config.EXPO));
		DroneSpawnerItem.setTagValue(itemStack, Config.THRUST,						DroneSpawnerItem.getTagValue(itemStack, Config.THRUST)						!= null ? DroneSpawnerItem.getTagValue(itemStack, Config.THRUST)					: config.getOption(Config.THRUST));
		DroneSpawnerItem.setTagValue(itemStack, Config.THRUST_CURVE,				DroneSpawnerItem.getTagValue(itemStack, Config.THRUST_CURVE)				!= null ? DroneSpawnerItem.getTagValue(itemStack, Config.THRUST_CURVE)				: config.getOption(Config.THRUST_CURVE));
		DroneSpawnerItem.setTagValue(itemStack, Config.DAMAGE_COEFFICIENT,			DroneSpawnerItem.getTagValue(itemStack, Config.DAMAGE_COEFFICIENT)			!= null ? DroneSpawnerItem.getTagValue(itemStack, Config.DAMAGE_COEFFICIENT)		: config.getOption(Config.DAMAGE_COEFFICIENT));
//		DroneSpawnerItem.setTagValue(itemStack, Config.CRASH_MOMENTUM_THRESHOLD,	DroneSpawnerItem.getTagValue(itemStack, Config.CRASH_MOMENTUM_THRESHOLD)	!= null ? DroneSpawnerItem.getTagValue(itemStack, Config.CRASH_MOMENTUM_THRESHOLD)	: config.getOption(Config.CRASH_MOMENTUM_THRESHOLD));

		DroneSpawnerItem.setTagValue(itemStack, Config.MASS,				DroneSpawnerItem.getTagValue(itemStack, Config.MASS)				!= null ? DroneSpawnerItem.getTagValue(itemStack, Config.MASS)				: config.getOption(Config.MASS));
		DroneSpawnerItem.setTagValue(itemStack, Config.SIZE,				DroneSpawnerItem.getTagValue(itemStack, Config.SIZE)				!= null ? DroneSpawnerItem.getTagValue(itemStack, Config.SIZE)				: config.getOption(Config.SIZE));
		DroneSpawnerItem.setTagValue(itemStack, Config.DRAG_COEFFICIENT,	DroneSpawnerItem.getTagValue(itemStack, Config.DRAG_COEFFICIENT)	!= null ? DroneSpawnerItem.getTagValue(itemStack, Config.DRAG_COEFFICIENT)	: config.getOption(Config.DRAG_COEFFICIENT));

		// config doesn't contain values for these, setting to default values if itemStack doesn't contain the value
		DroneSpawnerItem.setTagValue(itemStack, Config.NO_CLIP,		DroneSpawnerItem.getTagValue(itemStack, Config.NO_CLIP)		!= null ? DroneSpawnerItem.getTagValue(itemStack, Config.NO_CLIP)	: 0);
		DroneSpawnerItem.setTagValue(itemStack, Config.GOD_MODE,	DroneSpawnerItem.getTagValue(itemStack, Config.GOD_MODE)	!= null ? DroneSpawnerItem.getTagValue(itemStack, Config.GOD_MODE)	: 0);
	}
}
