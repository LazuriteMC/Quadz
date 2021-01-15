package dev.lazurite.fpvracing.common.entity;

import dev.lazurite.fpvracing.client.input.InputTick;
import dev.lazurite.fpvracing.FPVRacing;
import dev.lazurite.fpvracing.common.item.QuadcopterItem;
import dev.lazurite.fpvracing.common.util.BetaflightHelper;
import dev.lazurite.fpvracing.common.physics.thrust.QuadcopterThrust;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.ProjectileDamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

public abstract class QuadcopterEntity extends FlyableEntity {
	public static final GenericDataTrackerRegistry.Entry<Float> RATE = GenericDataTrackerRegistry.register(new Config.Key<>("rate", FPVRacing.FLOAT_TYPE), 0.5F, QuadcopterEntity.class);
	public static final GenericDataTrackerRegistry.Entry<Float> SUPER_RATE = GenericDataTrackerRegistry.register(new Config.Key<>("superRate", FPVRacing.FLOAT_TYPE), 0.5F, QuadcopterEntity.class);
	public static final GenericDataTrackerRegistry.Entry<Float> EXPO = GenericDataTrackerRegistry.register(new Config.Key<>("expo", FPVRacing.FLOAT_TYPE), 0.0F, QuadcopterEntity.class);
	public static final GenericDataTrackerRegistry.Entry<Float> THRUST_CURVE = GenericDataTrackerRegistry.register(new Config.Key<>("thrustCurve", FPVRacing.FLOAT_TYPE), 0.95F, QuadcopterEntity.class);
	public static final GenericDataTrackerRegistry.Entry<Integer> THRUST = GenericDataTrackerRegistry.register(new Config.Key<>("thrust", FPVRacing.INTEGER_TYPE), 50, QuadcopterEntity.class);
	public static final GenericDataTrackerRegistry.Entry<Integer> CAMERA_ANGLE = GenericDataTrackerRegistry.register(new Config.Key<>("cameraAngle", FPVRacing.INTEGER_TYPE), 0, QuadcopterEntity.class);

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
			ItemStack itemStack = new ItemStack(FPVRacing.DRONE_SPAWNER_ITEM);
			writeTagToSpawner(itemStack);
			dropStack(itemStack);
		}
	}
}
