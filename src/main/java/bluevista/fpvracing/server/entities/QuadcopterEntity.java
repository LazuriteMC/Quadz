package bluevista.fpvracing.server.entities;

import bluevista.fpvracing.client.ClientInitializer;
import bluevista.fpvracing.client.ClientTick;
import bluevista.fpvracing.client.input.InputTick;
import bluevista.fpvracing.network.entity.DroneEntityS2C;
import bluevista.fpvracing.physics.thrust.QuadcopterThrust;
import bluevista.fpvracing.physics.entity.ClientEntityPhysics;
import bluevista.fpvracing.config.Config;
import bluevista.fpvracing.server.ServerInitializer;
import bluevista.fpvracing.server.items.QuadcopterItem;
import bluevista.fpvracing.server.items.TransmitterItem;
import bluevista.fpvracing.util.math.BetaflightHelper;
import bluevista.fpvracing.util.math.QuaternionHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.ProjectileDamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

import javax.vecmath.Quat4f;

/**
 * @author Ethan Johnson
 * @author Patrick Hofmann
 */
public class QuadcopterEntity extends FlyableEntity {

	/* Controller Settings */
	protected float rate;
	protected float superRate;
	protected float expo;

	/* Misc */
	protected float damageCoefficient;
	protected int cameraAngle;

	/**
	 * The constructor called by the Fabric API in {@link ServerInitializer}. Invokes the main constructor.
	 * @param type the {@link EntityType}
	 * @param world the {@link World} that the {@link QuadcopterEntity} will be spawned in
	 */
	public QuadcopterEntity(EntityType<?> type, World world) {
		super(type, world);
	}

	@Override
	public void tick() {
		super.tick();

		if (!world.isClient()) {
			DroneEntityS2C.send(this);
		}
	}

	@Environment(EnvType.CLIENT)
	public void step(float delta) {
		super.step(delta);

		ClientEntityPhysics physics = (ClientEntityPhysics) this.physics;
		QuadcopterThrust thrust = new QuadcopterThrust(this);

		physics.decreaseAngularVelocity();
		physics.calculateBlockDamage();

		if (!ClientTick.isServerModded || TransmitterItem.isBoundTransmitter(ClientInitializer.client.player.getMainHandStack(), this)) {
			float deltaX = (float) BetaflightHelper.calculateRates(InputTick.axisValues.currX, getConfigValues(Config.RATE).floatValue(), getConfigValues(Config.EXPO).floatValue(), getConfigValues(Config.SUPER_RATE).floatValue(), delta);
			float deltaY = (float) BetaflightHelper.calculateRates(InputTick.axisValues.currY, getConfigValues(Config.RATE).floatValue(), getConfigValues(Config.EXPO).floatValue(), getConfigValues(Config.SUPER_RATE).floatValue(), delta);
			float deltaZ = (float) BetaflightHelper.calculateRates(InputTick.axisValues.currZ, getConfigValues(Config.RATE).floatValue(), getConfigValues(Config.EXPO).floatValue(), getConfigValues(Config.SUPER_RATE).floatValue(), delta);

			physics.rotateX(deltaX);
			physics.rotateY(deltaY);
			physics.rotateZ(deltaZ);

			physics.applyForce(thrust.getForce());
		}
	}

	/**
	 * The main config value setter for the {@link QuadcopterEntity}.
	 * @param key the {@link Config} key to set
	 * @param value the {@link Number} value to set
	 */
	@Override
	public void setConfigValues(String key, Number value) {
		switch (key) {
			case Config.CAMERA_ANGLE:
				this.cameraAngle = value.intValue();
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
			case Config.DAMAGE_COEFFICIENT:
				this.damageCoefficient = value.floatValue();
				break;
			default:
				super.setConfigValues(key, value);
		}
	}

	/**
	 * The main config value getter for the {@link QuadcopterEntity}.
	 * @param key the {@link Config} key to get
	 * @return the {@link Number} value based off of the {@link Config} key
	 */
	@Override
	public Number getConfigValues(String key) {
		switch (key) {
			case Config.CAMERA_ANGLE:
				return this.cameraAngle;
			case Config.RATE:
				return this.rate;
			case Config.SUPER_RATE:
				return this.superRate;
			case Config.EXPO:
				return this.expo;
			case Config.DAMAGE_COEFFICIENT:
				return this.damageCoefficient;
			default:
				return super.getConfigValues(key);
		}
	}

//	@Environment(EnvType.CLIENT)
//	public void prepConfig() {
//		String[] CLIENT_KEYS = {
//				Config.CAMERA_ANGLE,
//				Config.FIELD_OF_VIEW,
//				Config.RATE,
//				Config.SUPER_RATE,
//				Config.EXPO,
//				Config.THRUST,
//				Config.THRUST_CURVE,
//				Config.DAMAGE_COEFFICIENT,
//				Config.MASS,
//				Config.SIZE,
//				Config.DRAG_COEFFICIENT
//		};
//
//		Config config = ClientInitializer.getConfig();
//
//		for (String key : CLIENT_KEYS) {
//			if (Config.FLOAT_KEYS.contains(key)) {
//				this.setConfigValues(key, config.getFloatOption(key));
//			} else if (Config.INT_KEYS.contains(key)){
//				this.setConfigValues(key, config.getIntOption(key));
//			}
//		}
//	}

	@Override
	public void updateEulerRotations() {
		Quat4f cameraPitch = physics.getOrientation();
		QuaternionHelper.rotateX(cameraPitch, -getConfigValues(Config.CAMERA_ANGLE).intValue());
		pitch = QuaternionHelper.getPitch(cameraPitch);

		super.updateEulerRotations();
	}

	/**
	 * Gets whether the {@link QuadcopterEntity} can be killed
	 * by conventional means (e.g. punched, rained on, set on fire, etc.)
	 * @return whether or not the {@link QuadcopterEntity} is killable
	 */
	@Override
	public boolean isKillable() {
		return !(getConfigValues(Config.GOD_MODE).intValue() == 1 || getConfigValues(Config.NO_CLIP).intValue() == 1);
	}

	/**
	 * Called whenever a world is saved. Contains {@link CompoundTag} information
	 * for the {@link QuadcopterEntity} which should persist across restarts of the game.
	 * @param tag the tag to save to
	 */
	@Override
	protected void writeCustomDataToTag(CompoundTag tag) {
		super.writeCustomDataToTag(tag);

		tag.putFloat(Config.RATE, getConfigValues(Config.RATE).floatValue());
		tag.putFloat(Config.SUPER_RATE, getConfigValues(Config.SUPER_RATE).floatValue());
		tag.putFloat(Config.EXPO, getConfigValues(Config.EXPO).floatValue());

		tag.putFloat(Config.DAMAGE_COEFFICIENT, getConfigValues(Config.DAMAGE_COEFFICIENT).floatValue());
		tag.putInt(Config.CAMERA_ANGLE, getConfigValues(Config.CAMERA_ANGLE).intValue());
	}

	/**
	 * Called whenever a world is loaded. Contains {@link CompoundTag} information
	 * for the {@link QuadcopterEntity} which should persist across restarts of the game.
	 * @param tag the tag to load from
	 */
	@Override
	protected void readCustomDataFromTag(CompoundTag tag) {
		super.readCustomDataFromTag(tag);

		setConfigValues(Config.RATE, tag.getFloat(Config.RATE));
		setConfigValues(Config.SUPER_RATE, tag.getFloat(Config.SUPER_RATE));
		setConfigValues(Config.EXPO, tag.getFloat(Config.EXPO));

		setConfigValues(Config.DAMAGE_COEFFICIENT, tag.getFloat(Config.DAMAGE_COEFFICIENT));
		setConfigValues(Config.CAMERA_ANGLE, tag.getInt(Config.CAMERA_ANGLE));
	}

	/**
	 * Break the {@link QuadcopterEntity} when it's shot or otherwise damaged in some way.
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
	 * Called whenever the {@link QuadcopterEntity} is killed. Drops {@link QuadcopterItem} containing tag info.
	 */
	@Override
	public void kill() {
		if (this.world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
			ItemStack itemStack = new ItemStack(ServerInitializer.DRONE_SPAWNER_ITEM);
			QuadcopterItem.prepDestroyedDrone(this, itemStack);

			this.dropStack(itemStack);
		}

		this.remove();
	}

//	/**
//	 * Whenever the {@link DroneEntity} is killed or
//	 * otherwise not supposed to be there, this is called.
//	 */
//	@Override
//	public void remove() {
//		super.remove();
//
//		if (world.isClient()) {
//			if (physics.isActive() && ClientTick.isServerModded) {
//				EntityPhysicsC2S.send(this);
//			}
//		}
//	}

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
}
