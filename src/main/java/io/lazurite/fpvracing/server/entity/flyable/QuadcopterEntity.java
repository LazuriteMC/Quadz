package io.lazurite.fpvracing.server.entity.flyable;

import com.bulletphysics.dynamics.RigidBody;
import io.lazurite.fpvracing.client.ClientInitializer;
import io.lazurite.fpvracing.client.ClientTick;
import io.lazurite.fpvracing.client.input.InputTick;
import io.lazurite.fpvracing.network.tracker.Config;
import io.lazurite.fpvracing.network.tracker.GenericDataTrackerRegistry;
import io.lazurite.fpvracing.physics.thrust.QuadcopterThrust;
import io.lazurite.fpvracing.physics.entity.ClientPhysicsHandler;
import io.lazurite.fpvracing.server.ServerInitializer;
import io.lazurite.fpvracing.server.entity.FlyableEntity;
import io.lazurite.fpvracing.server.item.QuadcopterItem;
import io.lazurite.fpvracing.server.item.TransmitterItem;
import io.lazurite.fpvracing.util.math.BetaflightHelper;
import io.lazurite.fpvracing.util.math.QuaternionHelper;
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
import javax.vecmath.Vector3f;
import java.util.List;

/**
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

	private final QuadcopterThrust thrust = new QuadcopterThrust(this);

	/**
	 * @param type  the {@link EntityType}
	 * @param world the {@link World} that the {@link QuadcopterEntity} will be spawned in
	 */
	public QuadcopterEntity(EntityType<?> type, World world) {
		super(type, world);
	}

	@Environment(EnvType.CLIENT)
	public void step(ClientPhysicsHandler physics, float delta) {
		super.step(physics, delta);
		decreaseAngularVelocity();

		/*
		 * Change rotation of the quad using controller input.
		 */
		if (TransmitterItem.isBoundTransmitter(ClientInitializer.client.player.getMainHandStack(), this)) {
			float deltaX = (float) BetaflightHelper.calculateRates(InputTick.axisValues.currX, getValue(RATE), getValue(EXPO), getValue(SUPER_RATE), delta);
			float deltaY = (float) BetaflightHelper.calculateRates(InputTick.axisValues.currY, getValue(RATE), getValue(EXPO), getValue(SUPER_RATE), delta);
			float deltaZ = (float) BetaflightHelper.calculateRates(InputTick.axisValues.currZ, getValue(RATE), getValue(EXPO), getValue(SUPER_RATE), delta);

			physics.rotateX(deltaX);
			physics.rotateY(deltaY);
			physics.rotateZ(deltaZ);

			physics.applyForce(thrust.getForce());
		}
	}

	@Override
	public void updateEulerRotations() {
		Quat4f cameraPitch = physics.getOrientation();
		QuaternionHelper.rotateX(cameraPitch, -getValue(CAMERA_ANGLE));
		pitch = QuaternionHelper.getPitch(cameraPitch);

		super.updateEulerRotations();
	}

	/**
	 * Gets whether the {@link QuadcopterEntity} can be killed
	 * by conventional means (e.g. punched, rained on, set on fire, etc.)
	 *
	 * @return whether or not the {@link QuadcopterEntity} is killable
	 */
	@Override
	public boolean isKillable() {
		return !(getValue(FlyableEntity.GOD_MODE) || noClip);
	}

	@Environment(EnvType.CLIENT)
	public void decreaseAngularVelocity() {
		List<RigidBody> bodies = ClientInitializer.physicsWorld.getRigidBodies();
		RigidBody rigidBody = ((ClientPhysicsHandler) getPhysics()).getRigidBody();
		boolean mightCollide = false;
		float t = 0.25f;

		for (RigidBody body : bodies) {
			if (body != rigidBody) {
				Vector3f dist = body.getCenterOfMassPosition(new Vector3f());
				dist.sub(rigidBody.getCenterOfMassPosition(new Vector3f()));

				if (dist.length() < 1.0f) {
					mightCollide = true;
					break;
				}
			}
		}

		if (!mightCollide) {
			rigidBody.setAngularVelocity(new Vector3f(0, 0, 0));
		} else {
			float it = 1 - InputTick.axisValues.currT;

			if (Math.abs(InputTick.axisValues.currX) * it > t ||
					Math.abs(InputTick.axisValues.currY) * it > t ||
					Math.abs(InputTick.axisValues.currZ) * it > t) {
				rigidBody.setAngularVelocity(new Vector3f(0, 0, 0));
			}
		}
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
		if (world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
			ItemStack itemStack = new ItemStack(ServerInitializer.DRONE_SPAWNER_ITEM);
			writeTagToSpawner(itemStack);
			dropStack(itemStack);
		}

		remove();
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
}
