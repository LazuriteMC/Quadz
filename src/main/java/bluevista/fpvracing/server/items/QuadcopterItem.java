package bluevista.fpvracing.server.items;

import bluevista.fpvracing.config.Config;
import bluevista.fpvracing.server.ServerInitializer;
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
				default:
					return null;
			}
		}

		return null;
	}

	public static void prepSpawnedDrone(PlayerEntity user, QuadcopterEntity drone) {
		Config config = ServerInitializer.SERVER_PLAYER_CONFIGS.get(user.getUuid());
		ItemStack itemStack = user.getMainHandStack();

		drone.setConfigValues(Config.BAND,						QuadcopterItem.getTagValue(itemStack, Config.BAND)						!= null ? QuadcopterItem.getTagValue(itemStack, Config.BAND)						: config.getOption(Config.BAND));
		drone.setConfigValues(Config.CHANNEL,					QuadcopterItem.getTagValue(itemStack, Config.CHANNEL)						!= null ? QuadcopterItem.getTagValue(itemStack, Config.CHANNEL)					: config.getOption(Config.CHANNEL));
		drone.setConfigValues(Config.CAMERA_ANGLE,				QuadcopterItem.getTagValue(itemStack, Config.CAMERA_ANGLE)				!= null ? QuadcopterItem.getTagValue(itemStack, Config.CAMERA_ANGLE)				: config.getOption(Config.CAMERA_ANGLE));
		drone.setConfigValues(Config.FIELD_OF_VIEW,				QuadcopterItem.getTagValue(itemStack, Config.FIELD_OF_VIEW)				!= null ? QuadcopterItem.getTagValue(itemStack, Config.FIELD_OF_VIEW)				: config.getOption(Config.FIELD_OF_VIEW));

		drone.setConfigValues(Config.RATE,						QuadcopterItem.getTagValue(itemStack, Config.RATE)						!= null ? QuadcopterItem.getTagValue(itemStack, Config.RATE)						: config.getOption(Config.RATE));
		drone.setConfigValues(Config.SUPER_RATE,				QuadcopterItem.getTagValue(itemStack, Config.SUPER_RATE)					!= null ? QuadcopterItem.getTagValue(itemStack, Config.SUPER_RATE)				: config.getOption(Config.SUPER_RATE));
		drone.setConfigValues(Config.EXPO,						QuadcopterItem.getTagValue(itemStack, Config.EXPO)						!= null ? QuadcopterItem.getTagValue(itemStack, Config.EXPO)						: config.getOption(Config.EXPO));

		drone.setConfigValues(Config.THRUST,					QuadcopterItem.getTagValue(itemStack, Config.THRUST)						!= null ? QuadcopterItem.getTagValue(itemStack, Config.THRUST)					: config.getOption(Config.THRUST));
		drone.setConfigValues(Config.THRUST_CURVE,				QuadcopterItem.getTagValue(itemStack, Config.THRUST_CURVE)				!= null ? QuadcopterItem.getTagValue(itemStack, Config.THRUST_CURVE)				: config.getOption(Config.THRUST_CURVE));
		drone.setConfigValues(Config.DAMAGE_COEFFICIENT,		QuadcopterItem.getTagValue(itemStack, Config.DAMAGE_COEFFICIENT)			!= null ? QuadcopterItem.getTagValue(itemStack, Config.DAMAGE_COEFFICIENT)		: config.getOption(Config.DAMAGE_COEFFICIENT));

		drone.setConfigValues(Config.MASS,				QuadcopterItem.getTagValue(itemStack, Config.MASS)				!= null ? QuadcopterItem.getTagValue(itemStack, Config.MASS)				: config.getOption(Config.MASS));
		drone.setConfigValues(Config.SIZE,				QuadcopterItem.getTagValue(itemStack, Config.SIZE)				!= null ? QuadcopterItem.getTagValue(itemStack, Config.SIZE)				: config.getOption(Config.SIZE));
		drone.setConfigValues(Config.DRAG_COEFFICIENT,	QuadcopterItem.getTagValue(itemStack, Config.DRAG_COEFFICIENT)	!= null ? QuadcopterItem.getTagValue(itemStack, Config.DRAG_COEFFICIENT)	: config.getOption(Config.DRAG_COEFFICIENT));

		// config doesn't contain values for these, setting to default values if itemStack doesn't contain the value
		drone.setConfigValues(Config.NO_CLIP,	QuadcopterItem.getTagValue(itemStack, Config.NO_CLIP)		!= null ? QuadcopterItem.getTagValue(itemStack, Config.NO_CLIP)	: 0);
		drone.setConfigValues(Config.GOD_MODE,	QuadcopterItem.getTagValue(itemStack, Config.GOD_MODE)	!= null ? QuadcopterItem.getTagValue(itemStack, Config.GOD_MODE)	: 0);

		drone.setConfigValues(Config.PLAYER_ID, user.getEntityId());
	}

	public static void prepDestroyedDrone(QuadcopterEntity drone, ItemStack itemStack) {
		QuadcopterItem.setTagValue(itemStack, Config.BAND,						drone.getConfigValues(Config.BAND));
		QuadcopterItem.setTagValue(itemStack, Config.CHANNEL,						drone.getConfigValues(Config.CHANNEL));
		QuadcopterItem.setTagValue(itemStack, Config.CAMERA_ANGLE,				drone.getConfigValues(Config.CAMERA_ANGLE));
		QuadcopterItem.setTagValue(itemStack, Config.FIELD_OF_VIEW,				drone.getConfigValues(Config.FIELD_OF_VIEW));

		QuadcopterItem.setTagValue(itemStack, Config.NO_CLIP,						drone.getConfigValues(Config.NO_CLIP));
		QuadcopterItem.setTagValue(itemStack, Config.GOD_MODE,					drone.getConfigValues(Config.GOD_MODE));

		QuadcopterItem.setTagValue(itemStack, Config.RATE,						drone.getConfigValues(Config.RATE));
		QuadcopterItem.setTagValue(itemStack, Config.SUPER_RATE,					drone.getConfigValues(Config.SUPER_RATE));
		QuadcopterItem.setTagValue(itemStack, Config.EXPO,						drone.getConfigValues(Config.EXPO));

		QuadcopterItem.setTagValue(itemStack, Config.THRUST,						drone.getConfigValues(Config.THRUST));
		QuadcopterItem.setTagValue(itemStack, Config.THRUST_CURVE,				drone.getConfigValues(Config.THRUST_CURVE));
		QuadcopterItem.setTagValue(itemStack, Config.DAMAGE_COEFFICIENT,			drone.getConfigValues(Config.DAMAGE_COEFFICIENT));

		QuadcopterItem.setTagValue(itemStack, Config.MASS,				drone.getConfigValues(Config.MASS));
		QuadcopterItem.setTagValue(itemStack, Config.SIZE,				drone.getConfigValues(Config.SIZE));
		QuadcopterItem.setTagValue(itemStack, Config.DRAG_COEFFICIENT,	drone.getConfigValues(Config.DRAG_COEFFICIENT));
	}

	public static void prepDroneSpawnerItem(PlayerEntity user, ItemStack itemStack) {
		Config config = ServerInitializer.SERVER_PLAYER_CONFIGS.get(user.getUuid());

		QuadcopterItem.setTagValue(itemStack, Config.BAND,						QuadcopterItem.getTagValue(itemStack, Config.BAND)						!= null ? QuadcopterItem.getTagValue(itemStack, Config.BAND)						: config.getOption(Config.BAND));
		QuadcopterItem.setTagValue(itemStack, Config.CHANNEL,						QuadcopterItem.getTagValue(itemStack, Config.CHANNEL)						!= null ? QuadcopterItem.getTagValue(itemStack, Config.CHANNEL)					: config.getOption(Config.CHANNEL));
		QuadcopterItem.setTagValue(itemStack, Config.CAMERA_ANGLE,				QuadcopterItem.getTagValue(itemStack, Config.CAMERA_ANGLE)				!= null ? QuadcopterItem.getTagValue(itemStack, Config.CAMERA_ANGLE)				: config.getOption(Config.CAMERA_ANGLE));
		QuadcopterItem.setTagValue(itemStack, Config.FIELD_OF_VIEW,				QuadcopterItem.getTagValue(itemStack, Config.FIELD_OF_VIEW)				!= null ? QuadcopterItem.getTagValue(itemStack, Config.FIELD_OF_VIEW)				: config.getOption(Config.FIELD_OF_VIEW));

		QuadcopterItem.setTagValue(itemStack, Config.RATE,						QuadcopterItem.getTagValue(itemStack, Config.RATE)						!= null ? QuadcopterItem.getTagValue(itemStack, Config.RATE)						: config.getOption(Config.RATE));
		QuadcopterItem.setTagValue(itemStack, Config.SUPER_RATE,					QuadcopterItem.getTagValue(itemStack, Config.SUPER_RATE)					!= null ? QuadcopterItem.getTagValue(itemStack, Config.SUPER_RATE)				: config.getOption(Config.SUPER_RATE));
		QuadcopterItem.setTagValue(itemStack, Config.EXPO,						QuadcopterItem.getTagValue(itemStack, Config.EXPO)						!= null ? QuadcopterItem.getTagValue(itemStack, Config.EXPO)						: config.getOption(Config.EXPO));

		QuadcopterItem.setTagValue(itemStack, Config.THRUST,						QuadcopterItem.getTagValue(itemStack, Config.THRUST)						!= null ? QuadcopterItem.getTagValue(itemStack, Config.THRUST)					: config.getOption(Config.THRUST));
		QuadcopterItem.setTagValue(itemStack, Config.THRUST_CURVE,				QuadcopterItem.getTagValue(itemStack, Config.THRUST_CURVE)				!= null ? QuadcopterItem.getTagValue(itemStack, Config.THRUST_CURVE)				: config.getOption(Config.THRUST_CURVE));
		QuadcopterItem.setTagValue(itemStack, Config.DAMAGE_COEFFICIENT,			QuadcopterItem.getTagValue(itemStack, Config.DAMAGE_COEFFICIENT)			!= null ? QuadcopterItem.getTagValue(itemStack, Config.DAMAGE_COEFFICIENT)		: config.getOption(Config.DAMAGE_COEFFICIENT));

		QuadcopterItem.setTagValue(itemStack, Config.MASS,				QuadcopterItem.getTagValue(itemStack, Config.MASS)				!= null ? QuadcopterItem.getTagValue(itemStack, Config.MASS)				: config.getOption(Config.MASS));
		QuadcopterItem.setTagValue(itemStack, Config.SIZE,				QuadcopterItem.getTagValue(itemStack, Config.SIZE)				!= null ? QuadcopterItem.getTagValue(itemStack, Config.SIZE)				: config.getOption(Config.SIZE));
		QuadcopterItem.setTagValue(itemStack, Config.DRAG_COEFFICIENT,	QuadcopterItem.getTagValue(itemStack, Config.DRAG_COEFFICIENT)	!= null ? QuadcopterItem.getTagValue(itemStack, Config.DRAG_COEFFICIENT)	: config.getOption(Config.DRAG_COEFFICIENT));

		// config doesn't contain values for these, setting to default values if itemStack doesn't contain the value
		QuadcopterItem.setTagValue(itemStack, Config.NO_CLIP,		QuadcopterItem.getTagValue(itemStack, Config.NO_CLIP)		!= null ? QuadcopterItem.getTagValue(itemStack, Config.NO_CLIP)	: 0);
		QuadcopterItem.setTagValue(itemStack, Config.GOD_MODE,	QuadcopterItem.getTagValue(itemStack, Config.GOD_MODE)	!= null ? QuadcopterItem.getTagValue(itemStack, Config.GOD_MODE)	: 0);
	}
}
