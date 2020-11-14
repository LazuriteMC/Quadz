package dev.lazurite.fpvracing.server.entity.flyable;

import dev.lazurite.fpvracing.client.input.InputTick;
import dev.lazurite.fpvracing.server.ServerInitializer;
import dev.lazurite.fpvracing.server.entity.FlyableEntity;
import dev.lazurite.fpvracing.server.item.QuadcopterItem;
import dev.lazurite.fpvracing.util.math.BetaflightHelper;
import dev.lazurite.fpvracing.util.math.QuaternionHelper;
import dev.lazurite.fpvracing.network.tracker.Config;
import dev.lazurite.fpvracing.network.tracker.GenericDataTrackerRegistry;
import dev.lazurite.fpvracing.physics.thrust.QuadcopterThrust;
import dev.lazurite.fpvracing.physics.entity.ClientPhysicsHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.ProjectileDamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

import javax.vecmath.Quat4f;

/**
 * Originally called DroneEntity, this class is the main entity used in this mod.
 * @author Ethan Johnson
 * @author Patrick Hofmann
 */
public class QuadcopterEntity extends FlyableEntity {
	public static final GenericDataTrackerRegistry.Entry<Float> RATE = GenericDataTrackerRegistry.register(new Config.Key<>("rate", ServerInitializer.FLOAT_TYPE), 0.5F, QuadcopterEntity.class);
	public static final GenericDataTrackerRegistry.Entry<Float> SUPER_RATE = GenericDataTrackerRegistry.register(new Config.Key<>("superRate", ServerInitializer.FLOAT_TYPE), 0.5F, QuadcopterEntity.class);
	public static final GenericDataTrackerRegistry.Entry<Float> EXPO = GenericDataTrackerRegistry.register(new Config.Key<>("expo", ServerInitializer.FLOAT_TYPE), 0.0F, QuadcopterEntity.class);
	public static final GenericDataTrackerRegistry.Entry<Float> THRUST_CURVE = GenericDataTrackerRegistry.register(new Config.Key<>("thrustCurve", ServerInitializer.FLOAT_TYPE), 0.95F, QuadcopterEntity.class);
	public static final GenericDataTrackerRegistry.Entry<Integer> THRUST = GenericDataTrackerRegistry.register(new Config.Key<>("thrust", ServerInitializer.INTEGER_TYPE), 50, QuadcopterEntity.class);
	public static final GenericDataTrackerRegistry.Entry<Integer> CAMERA_ANGLE = GenericDataTrackerRegistry.register(new Config.Key<>("cameraAngle", ServerInitializer.INTEGER_TYPE), 0, QuadcopterEntity.class);

	/**
	 * The main constructor. Doesn't do a whole lot.
	 * @param type
	 * @param world
	 */
	public QuadcopterEntity(EntityType<?> type, World world) {
		super(type, world);
		thrust = new QuadcopterThrust(this);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void stepInput(float delta) {
		ClientPhysicsHandler physics = (ClientPhysicsHandler) getPhysics();
		float deltaX = (float) BetaflightHelper.calculateRates(InputTick.axisValues.currX, getValue(RATE), getValue(EXPO), getValue(SUPER_RATE), delta);
		float deltaY = (float) BetaflightHelper.calculateRates(InputTick.axisValues.currY, getValue(RATE), getValue(EXPO), getValue(SUPER_RATE), delta);
		float deltaZ = (float) BetaflightHelper.calculateRates(InputTick.axisValues.currZ, getValue(RATE), getValue(EXPO), getValue(SUPER_RATE), delta);

		physics.rotateX(deltaX);
		physics.rotateY(deltaY);
		physics.rotateZ(deltaZ);

		physics.applyForce(thrust.getForce());
	}

	@Override
	public void updateEulerRotations() {
		super.updateEulerRotations();

		Quat4f cameraPitch = physics.getOrientation();
		QuaternionHelper.rotateX(cameraPitch, -getValue(CAMERA_ANGLE));
		pitch = QuaternionHelper.getPitch(cameraPitch);
	}

	/**
	 * Break the {@link QuadcopterEntity} when it's shot or otherwise damaged in some way.
	 *
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
		super.kill();

		if (world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
			ItemStack itemStack = new ItemStack(ServerInitializer.DRONE_SPAWNER_ITEM);
			writeTagToSpawner(itemStack);
			dropStack(itemStack);
		}
	}
}
