package bluevista.fpvracing.server.entities;

import bluevista.fpvracing.client.ClientInitializer;
import bluevista.fpvracing.client.ClientTick;
import bluevista.fpvracing.client.input.InputTick;
import bluevista.fpvracing.client.physics.DronePhysics;
import bluevista.fpvracing.config.Config;
import bluevista.fpvracing.network.entity.DroneEntityS2C;
import bluevista.fpvracing.network.entity.DroneEntityC2S;
import bluevista.fpvracing.server.ServerInitializer;
import bluevista.fpvracing.server.items.DroneSpawnerItem;
import bluevista.fpvracing.server.items.TransmitterItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.ProjectileDamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

import javax.vecmath.Vector3f;
import java.util.*;

/**
 * This class does most of the work in this mod. Not only does it store all of the physics
 * information necessary, but it also handles the update logic for both physics and minecraft entities.
 * @author Ethan Johnson
 * @author Patrick Hofmann
 */
public class DroneEntity extends Entity {
	public static final int TRACKING_RANGE = 80;
	public static final int PSEUDO_TRACKING_RANGE = TRACKING_RANGE / 2;

	/* Misc */
	public DronePhysics physics;
	private float damageCoefficient;
	private boolean godMode;
	private int bindID;

	/* Camera Settings */
	private int cameraAngle;
	private float fieldOfView;
	private int band;
	private int channel;

	/* Controller Settings */
	private float rate;
	private float superRate;
	private float expo;

	/**
	 * The constructor called by the Fabric API in {@link ServerInitializer}. Invokes the main constructor.
	 * @param type the {@link EntityType}
	 * @param world the {@link World} that the {@link DroneEntity} will be spawned in
	 */
	public DroneEntity(EntityType<?> type, World world) {
		this(world, Vec3d.ZERO, 0);
	}

	public DroneEntity(World world, Vec3d pos, float yaw, int playerID) {
		this(world, pos, yaw);
		this.physics.setPlayerID(playerID);

		Entity entity = world.getEntityById(playerID);
		if (entity instanceof PlayerEntity) {
			this.setCustomName(new LiteralText(((PlayerEntity) entity).getGameProfile().getName()));
			this.setCustomNameVisible(true);
		}
	}

	/**
	 * The main constructor for {@link DroneEntity}.
	 * @param world the {@link World} the {@link DroneEntity} will be spawned in
	 * @param pos the position where the {@link DroneEntity} will be spawned
	 * @param yaw the yaw of the {@link DroneEntity}
	 */
	public DroneEntity(World world, Vec3d pos, float yaw) {
		super(ServerInitializer.DRONE_ENTITY, world);

		this.updatePositionAndAngles(pos.x, pos.y, pos.z, yaw, 0);
		this.yaw = yaw;
		this.prevYaw = yaw;
		this.physics = new DronePhysics(this);

		this.ignoreCameraFrustum = true;
		this.godMode = false;
		this.bindID = -1;
	}

	/**
	 * Called every tick, this method handles several pieces of logic.
	 * Packets are also sent from both the client and the server within this method.
	 */
	@Override
	public void tick() {
		super.tick();

		Vector3f pos = physics.getRigidBody().getCenterOfMassPosition(new Vector3f());
		this.updatePosition(pos.x, pos.y, pos.z);

		if (this.world.isClient()) {
			physics.tick();

			if (physics.isActive()) {
				DroneEntityC2S.send(this);
			}
		} else {
			DroneEntityS2C.send(this);

			this.world.getOtherEntities(this, getBoundingBox(), (entity -> true)).forEach((entity -> {
				if (entity instanceof LivingEntity) {
					Vector3f vec = physics.getRigidBody().getLinearVelocity(new Vector3f());
					vec.scale(physics.mass);
					entity.damage(DamageSource.GENERIC, vec.length() * damageCoefficient);
				}
			}));
		}
	}

	/**
	 * The main config value setter for the {@link DroneEntity}.
	 * @param key the {@link Config} key to set
	 * @param value the {@link Number} value to set
	 */
	public void setConfigValues(String key, Number value) {
		switch (key) {
			case Config.BAND:
				this.band = value.intValue();
				break;
			case Config.CHANNEL:
				this.channel = value.intValue();
				break;
			case Config.CAMERA_ANGLE:
				this.cameraAngle = value.intValue();
				break;
			case Config.FIELD_OF_VIEW:
				this.fieldOfView = value.floatValue();
				break;
			case Config.NO_CLIP:
				this.noClip = value.intValue() == 1;
				break;
			case Config.GOD_MODE:
				this.godMode = value.intValue() == 1;
				break;
			case Config.RATE:
				this.rate = value.floatValue();
				break;
			case Config.SUPER_RATE:
				this.superRate = value.floatValue();
				break;
			case Config.EXPO:
				this.expo = value.floatValue();
				break;
			case Config.BIND:
				this.bindID = value.intValue();
				break;
			case Config.DAMAGE_COEFFICIENT:
				this.damageCoefficient = value.floatValue();
				break;
			default:
				physics.setConfigValues(key, value);
				break;
		}
	}

	/**
	 * The main config value getter for the {@link DroneEntity}.
	 * @param key the {@link Config} key to get
	 * @return the {@link Number} value based off of the {@link Config} key
	 */
	public Number getConfigValues(String key) {
		switch (key) {
			case Config.BAND:
				return this.band;
			case Config.CHANNEL:
				return this.channel;
			case Config.CAMERA_ANGLE:
				return this.cameraAngle;
			case Config.FIELD_OF_VIEW:
				return this.fieldOfView;
			case Config.NO_CLIP:
				return this.noClip ? 1 : 0;
			case Config.GOD_MODE:
				return this.godMode ? 1 : 0;
			case Config.RATE:
				return this.rate;
			case Config.SUPER_RATE:
				return this.superRate;
			case Config.EXPO:
				return this.expo;
			case Config.BIND:
				return this.bindID;
			case Config.DAMAGE_COEFFICIENT:
				return this.damageCoefficient;
			default:
				return physics.getConfigValues(key);
		}
	}

	@Environment(EnvType.CLIENT)
	public void prepConfig() {
		String[] CLIENT_KEYS = {
				Config.CAMERA_ANGLE,
				Config.FIELD_OF_VIEW,
				Config.RATE,
				Config.SUPER_RATE,
				Config.EXPO,
				Config.THRUST,
				Config.THRUST_CURVE,
				Config.DAMAGE_COEFFICIENT,
				Config.MASS,
				Config.SIZE,
				Config.DRAG_COEFFICIENT
		};

		Config config = ClientInitializer.getConfig();

		for (String key : CLIENT_KEYS) {
			if (Config.FLOAT_KEYS.contains(key)) {
				this.setConfigValues(key, config.getFloatOption(key));
			} else if (Config.INT_KEYS.contains(key)){
				this.setConfigValues(key, config.getIntOption(key));
			}
		}
	}

	/**
	 * Finds all instances of {@link DroneEntity} within range of the given {@link Entity}.
	 * @param entity the {@link Entity} as the origin
	 * @return a {@link List} of type {@link DroneEntity}
	 */
	public static List<Entity> getDrones(Entity entity) {
		ServerWorld world = (ServerWorld) entity.getEntityWorld();
		List<Entity> drones = world.getEntitiesByType(ServerInitializer.DRONE_ENTITY, EntityPredicates.EXCEPT_SPECTATOR);
		return drones;
	}

	/**
	 * Called whenever a world is saved. Contains {@link CompoundTag} information
	 * for the {@link DroneEntity} which should persist across restarts of the game.
	 * @param tag the tag to save to
	 */
	@Override
	protected void writeCustomDataToTag(CompoundTag tag) {
		tag.putInt(Config.BAND, getConfigValues(Config.BAND).intValue());
		tag.putInt(Config.CHANNEL, getConfigValues(Config.CHANNEL).intValue());
		tag.putInt(Config.CAMERA_ANGLE, getConfigValues(Config.CAMERA_ANGLE).intValue());
		tag.putFloat(Config.FIELD_OF_VIEW, getConfigValues(Config.FIELD_OF_VIEW).floatValue());
		tag.putFloat(Config.RATE, getConfigValues(Config.RATE).floatValue());
		tag.putFloat(Config.SUPER_RATE, getConfigValues(Config.SUPER_RATE).floatValue());
		tag.putFloat(Config.EXPO, getConfigValues(Config.EXPO).floatValue());
		tag.putFloat(Config.THRUST, getConfigValues(Config.THRUST).floatValue());
		tag.putFloat(Config.THRUST_CURVE, getConfigValues(Config.THRUST_CURVE).floatValue());
		tag.putFloat(Config.DAMAGE_COEFFICIENT, getConfigValues(Config.DAMAGE_COEFFICIENT).floatValue());
//		tag.putInt(Config.CRASH_MOMENTUM_THRESHOLD, getConfigValues(Config.CRASH_MOMENTUM_THRESHOLD).intValue());

		tag.putInt(Config.PLAYER_ID, getConfigValues(Config.PLAYER_ID).intValue());
		tag.putInt(Config.BIND, getConfigValues(Config.BIND).intValue());
		tag.putFloat(Config.MASS, getConfigValues(Config.MASS).floatValue());
		tag.putInt(Config.SIZE, getConfigValues(Config.SIZE).intValue());
		tag.putFloat(Config.DRAG_COEFFICIENT, getConfigValues(Config.DRAG_COEFFICIENT).floatValue());

		// don't write noClip or prevGodMode because...
		// noClip shouldn't be preserved after a restart (your drone may fall through the world) and ...
		// prevGodMode is only used when noClip is set, keeping this value between restarts isn't required
		tag.putInt(Config.GOD_MODE, getConfigValues(Config.GOD_MODE).intValue());
	}

	/**
	 * Called whenever a world is loaded. Contains {@link CompoundTag} information
	 * for the {@link DroneEntity} which should persist across restarts of the game.
	 * @param tag the tag to load from
	 */
	@Override
	protected void readCustomDataFromTag(CompoundTag tag) {
		setConfigValues(Config.BAND, tag.getInt(Config.BAND));
		setConfigValues(Config.CHANNEL, tag.getInt(Config.CHANNEL));
		setConfigValues(Config.CAMERA_ANGLE, tag.getInt(Config.CAMERA_ANGLE));
		setConfigValues(Config.FIELD_OF_VIEW, tag.getFloat(Config.FIELD_OF_VIEW));
		setConfigValues(Config.RATE, tag.getFloat(Config.RATE));
		setConfigValues(Config.SUPER_RATE, tag.getFloat(Config.SUPER_RATE));
		setConfigValues(Config.EXPO, tag.getFloat(Config.EXPO));
		setConfigValues(Config.THRUST, tag.getFloat(Config.THRUST));
		setConfigValues(Config.THRUST_CURVE, tag.getFloat(Config.THRUST_CURVE));
		setConfigValues(Config.DAMAGE_COEFFICIENT, tag.getFloat(Config.DAMAGE_COEFFICIENT));
//		setConfigValues(Config.CRASH_MOMENTUM_THRESHOLD, tag.getInt(Config.CRASH_MOMENTUM_THRESHOLD));

		setConfigValues(Config.PLAYER_ID, tag.getInt(Config.PLAYER_ID));
		setConfigValues(Config.BIND, tag.getInt(Config.BIND));
		setConfigValues(Config.MASS, tag.getFloat(Config.MASS));
		setConfigValues(Config.SIZE, tag.getInt(Config.SIZE));
		setConfigValues(Config.DRAG_COEFFICIENT, tag.getFloat(Config.DRAG_COEFFICIENT));

		// don't retrieve noClip or prevGodMode because they weren't written (reason in writeCustomDataToTag)
		setConfigValues(Config.GOD_MODE, tag.getInt(Config.GOD_MODE));
	}

	/**
	 * Break the {@link DroneEntity} when it's shot or otherwise damaged in some way.
	 * @param source the source of the damage
	 * @param amount the amount of damage taken
	 * @return
	 */
	@Override
	public boolean damage(DamageSource source, float amount) {
		if (source.getAttacker() instanceof PlayerEntity || (isKillable() && source instanceof ProjectileDamageSource)) {
			this.kill();
			return true;
		}
		return false;
	}

	/**
	 * Called whenever the {@link DroneEntity} is killed. Drops {@link DroneSpawnerItem} containing tag info.
	 */
	@Override
	public void kill() {
		if (this.world.isClient()) {
			if (!ClientTick.isServerModded) {
				ClientTick.destroyDrone(ClientInitializer.client);
			} else {
				this.remove();
			}
		} else {
			if (this.world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
				ItemStack itemStack = new ItemStack(ServerInitializer.DRONE_SPAWNER_ITEM);
				DroneSpawnerItem.prepDestroyedDrone(this, itemStack);

				this.dropStack(itemStack);
			}

			this.remove();
		}
	}

	/**
	 * If the {@link PlayerEntity} is holding a {@link TransmitterItem} when they right
	 * click on the {@link DroneEntity}, bind it using a new random ID.
	 * @param player the {@link PlayerEntity} who is interacting
	 * @param hand the hand of the {@link PlayerEntity}
	 * @return
	 */
	@Override
	public ActionResult interact(PlayerEntity player, Hand hand) {
		if (!player.world.isClient()) {
			if (player.inventory.getMainHandStack().getItem() instanceof TransmitterItem) {
				Random rand = new Random();

				bindID = rand.nextInt();
				physics.setPlayerID(player.getEntityId());
				setCustomName(new LiteralText(player.getGameProfile().getName()));

				TransmitterItem.setTagValue(player.getMainHandStack(), Config.BIND, bindID);
				player.sendMessage(new LiteralText("Transmitter bound"), false);
			}
		} else if (!InputTick.controllerExists()) {
			player.sendMessage(new TranslatableText("Controller not found"), false);
		}

		return ActionResult.SUCCESS;
	}

	/**
	 * Whenever the {@link DroneEntity} is killed or
	 * otherwise not supposed to be there, this is called.
	 */
	@Override
	public void remove() {
		super.remove();

		if (world.isClient()) {
			if (physics.isActive() && ClientTick.isServerModded) {
				DroneEntityC2S.send(this);
			}
		}
	}

	/**
	 * Allows the drone to be seen from far away.
	 * @param distance the distance away from the drone
	 * @return whether or not the drone is outside of the view distance
	 */
	@Override
	@Environment(EnvType.CLIENT)
	public boolean shouldRender(double distance) {
//		MinecraftClient client = ClientInitializer.client;
//
//		if (client.getCameraEntity() == this) {
//			return !client.options.getPerspective().isFirstPerson();
//		}

		return distance < Math.pow(ClientInitializer.client.options.viewDistance * 16, 2);
	}

	@Override
	public Packet<?> createSpawnPacket() {
		return new EntitySpawnS2CPacket(this);
	}

	@Override
	protected void initDataTracker() {

	}

	@Override
	public boolean isGlowing() {
		return false;
	}

	@Override
	public boolean collides() {
		return true;
	}

	/**
	 * Gets whether the {@link DroneEntity} can be killed
	 * by conventional means (e.g. punched, rained on, set on fire, etc.)
	 * @return whether or not the {@link DroneEntity} is killable
	 */
	public boolean isKillable() {
		return !(this.godMode || this.noClip);
	}
}
