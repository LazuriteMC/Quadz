package bluevista.fpvracing.server.entities;

import bluevista.fpvracing.client.ClientInitializer;
import bluevista.fpvracing.client.ClientTick;
import bluevista.fpvracing.client.input.InputTick;
import bluevista.fpvracing.physics.thrust.QuadcopterThrust;
import bluevista.fpvracing.physics.entity.ClientEntityPhysics;
import bluevista.fpvracing.config.Config;
import bluevista.fpvracing.server.ServerInitializer;
import bluevista.fpvracing.server.items.QuadcopterItem;
import bluevista.fpvracing.server.items.TransmitterItem;
import bluevista.fpvracing.util.math.BetaflightHelper;
import bluevista.fpvracing.util.math.QuaternionHelper;
import com.bulletphysics.dynamics.RigidBody;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.ProjectileDamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
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
	public static final TrackedData<Float> RATE = DataTracker.registerData(QuadcopterEntity.class, TrackedDataHandlerRegistry.FLOAT);
	public static final TrackedData<Float> SUPER_RATE = DataTracker.registerData(QuadcopterEntity.class, TrackedDataHandlerRegistry.FLOAT);
	public static final TrackedData<Float> EXPO = DataTracker.registerData(QuadcopterEntity.class, TrackedDataHandlerRegistry.FLOAT);

	public static final TrackedData<Integer> CAMERA_ANGLE = DataTracker.registerData(QuadcopterEntity.class, TrackedDataHandlerRegistry.INTEGER);
	public static final TrackedData<Float> DAMAGE_COEFFICIENT = DataTracker.registerData(QuadcopterEntity.class, TrackedDataHandlerRegistry.FLOAT);

	public static final TrackedData<Float> THRUST = DataTracker.registerData(QuadcopterEntity.class, TrackedDataHandlerRegistry.FLOAT);
	public static final TrackedData<Float> THRUST_CURVE = DataTracker.registerData(QuadcopterEntity.class, TrackedDataHandlerRegistry.FLOAT);

	/**
	 * The constructor called by the Fabric API in {@link ServerInitializer}. Invokes the main constructor.
	 * @param type the {@link EntityType}
	 * @param world the {@link World} that the {@link QuadcopterEntity} will be spawned in
	 */
	public QuadcopterEntity(EntityType<?> type, World world) {
		super(type, world);
	}

	@Environment(EnvType.CLIENT)
	public void step(float delta) {
		super.step(delta);

		ClientEntityPhysics physics = (ClientEntityPhysics) this.physics;
		QuadcopterThrust thrust = new QuadcopterThrust(this);

		physics.decreaseAngularVelocity();
		physics.calculateBlockDamage();

		if (!ClientTick.isServerModded || TransmitterItem.isBoundTransmitter(ClientInitializer.client.player.getMainHandStack(), this)) {
			float deltaX = (float) BetaflightHelper.calculateRates(InputTick.axisValues.currX, dataTracker.get(RATE), dataTracker.get(EXPO), dataTracker.get(SUPER_RATE), delta);
			float deltaY = (float) BetaflightHelper.calculateRates(InputTick.axisValues.currY, dataTracker.get(RATE), dataTracker.get(EXPO), dataTracker.get(SUPER_RATE), delta);
			float deltaZ = (float) BetaflightHelper.calculateRates(InputTick.axisValues.currZ, dataTracker.get(RATE), dataTracker.get(EXPO), dataTracker.get(SUPER_RATE), delta);

			physics.rotateX(deltaX);
			physics.rotateY(deltaY);
			physics.rotateZ(deltaZ);

			physics.applyForce(thrust.getForce());
		}
	}

	@Override
	public void updateEulerRotations() {
		Quat4f cameraPitch = physics.getOrientation();
		QuaternionHelper.rotateX(cameraPitch, -dataTracker.get(CAMERA_ANGLE));
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
		return !(dataTracker.get(GOD_MODE) || noClip);
	}

	/**
	 * Called whenever a world is saved. Contains {@link CompoundTag} information
	 * for the {@link QuadcopterEntity} which should persist across restarts of the game.
	 * @param tag the tag to save to
	 */
	@Override
	protected void writeCustomDataToTag(CompoundTag tag) {
		super.writeCustomDataToTag(tag);

		tag.putFloat("rate", this.dataTracker.get(RATE));
		tag.putFloat("super_rate", this.dataTracker.get(SUPER_RATE));
		tag.putFloat("expo", this.dataTracker.get(EXPO));

		tag.putInt("camera_angle", this.dataTracker.get(CAMERA_ANGLE));
		tag.putFloat("damageCoefficient", this.dataTracker.get(DAMAGE_COEFFICIENT));

		tag.putFloat("thrust", this.dataTracker.get(THRUST));
		tag.putFloat("thrust_curve", this.dataTracker.get(THRUST_CURVE));
	}

	/**
	 * Called whenever a world is loaded. Contains {@link CompoundTag} information
	 * for the {@link QuadcopterEntity} which should persist across restarts of the game.
	 * @param tag the tag to load from
	 */
	@Override
	protected void readCustomDataFromTag(CompoundTag tag) {
		super.readCustomDataFromTag(tag);

		this.dataTracker.set(RATE, tag.getFloat("rate"));
		this.dataTracker.set(SUPER_RATE, tag.getFloat("super_rate"));
		this.dataTracker.set(EXPO, tag.getFloat("expo"));

		this.dataTracker.set(CAMERA_ANGLE, tag.getInt("camera_angle"));
		this.dataTracker.set(DAMAGE_COEFFICIENT, tag.getFloat("damage_coefficient"));

		this.dataTracker.set(THRUST, tag.getFloat("thrust"));
		this.dataTracker.set(THRUST_CURVE, tag.getFloat("thrust_curve"));
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
		super.initDataTracker();
		this.dataTracker.startTracking(RATE, 0.0f);
		this.dataTracker.startTracking(SUPER_RATE, 0.0f);
		this.dataTracker.startTracking(EXPO, 0.0f);

		this.dataTracker.startTracking(CAMERA_ANGLE, 0);
		this.dataTracker.startTracking(DAMAGE_COEFFICIENT, 0.0f);

		this.dataTracker.startTracking(THRUST, 0.0f);
		this.dataTracker.startTracking(THRUST_CURVE, 0.0f);
	}
}
