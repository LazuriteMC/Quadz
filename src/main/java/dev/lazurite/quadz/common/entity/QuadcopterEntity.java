package dev.lazurite.quadz.common.entity;

import com.jme3.math.Quaternion;
import com.jme3.math.Transform;
import com.jme3.math.Vector3f;
import com.mojang.math.Matrix4f;
import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.common.util.Matrix4fAccess;
import dev.lazurite.rayon.api.EntityPhysicsElement;
import dev.lazurite.rayon.impl.bullet.collision.body.ElementRigidBody;
import dev.lazurite.rayon.impl.bullet.collision.body.EntityRigidBody;
import dev.lazurite.rayon.impl.bullet.math.Convert;
import dev.lazurite.toolbox.api.math.QuaternionHelper;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class QuadcopterEntity extends RemoteControllableEntity implements EntityPhysicsElement {
	private static final EntityDataAccessor<Float> THRUST = SynchedEntityData.defineId(QuadcopterEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> THRUST_CURVE = SynchedEntityData.defineId(QuadcopterEntity.class, EntityDataSerializers.FLOAT);
	private final EntityRigidBody rigidBody = new EntityRigidBody(this);

	public QuadcopterEntity(EntityType<? extends LivingEntity> entityType, Level level) {
		super(entityType, level);
		this.rigidBody.setBuoyancyType(ElementRigidBody.BuoyancyType.NONE);
	}

	@Override
	public void tick() {
		super.tick();

		if (!this.joystickValues.isEmpty()) {
			final var roll = this.joystickValues.get(new ResourceLocation(Quadz.MODID, "roll"));
			final var pitch = this.joystickValues.get(new ResourceLocation(Quadz.MODID, "pitch"));
			final var yaw = this.joystickValues.get(new ResourceLocation(Quadz.MODID, "yaw"));
			final var throttle = this.joystickValues.get(new ResourceLocation(Quadz.MODID, "throttle"));

			/* Rate Mode */
			if (Mode.RATE.equals(frame.getMode())) {
				rotate(frame.calculatePitch(1/60f), frame.calculateYaw(1/60f), frame.calculateRoll(1/60f));

			/* Self Leveling Mode */
			} else if (Mode.ANGLE.equals(frame.getMode())) {
				float targetPitch = -frame.getPitch() * frame.getMaxAngle();
				float targetRoll = -frame.getRoll() * frame.getMaxAngle();

				float currentPitch = -1.0F * (float) Math.toDegrees(QuaternionHelper.toEulerAngles(Convert.toMinecraft(getRigidBody().getPhysicsRotation(new Quaternion()))).y());
				float currentRoll = -1.0F * (float) Math.toDegrees(QuaternionHelper.toEulerAngles(Convert.toMinecraft(getRigidBody().getPhysicsRotation(new Quaternion()))).x());

				rotate(currentPitch - targetPitch, frame.calculateYaw(0.05f), currentRoll - targetRoll);
			}

			/* Decrease angular velocity */
			if (frame.getThrottle() > 0.1f) {
				Vector3f correction = getRigidBody().getAngularVelocity(new Vector3f()).multLocal(0.5f * frame.getThrottle());

				// eye roll
				if (Float.isFinite(correction.lengthSquared())) {
					getRigidBody().setAngularVelocity(correction);
				}
			}

			/* Get the thrust unit vector */
			final var mat = new Matrix4f();
			Matrix4fAccess.from(mat).fromQuaternion(QuaternionHelper.rotateX(Convert.toMinecraft(getRigidBody().getPhysicsRotation(new Quaternion())), 90));
			final var unit = Matrix4fAccess.from(mat).matrixToVector();

			/* Calculate basic thrust */
			final var thrust = new Vector3f().set(unit).multLocal((float) (getThrust() * (Math.pow(frame.getThrottle(), getThrustCurve()))));

			/* Calculate thrust from yaw spin */
			final var yawThrust = new Vector3f().set(unit).multLocal(Math.abs(frame.calculateYaw(0.05f) * getThrust() * 0.002f));

			/* Add up the net thrust and apply the force */
			if (Float.isFinite(thrust.length())) {
				getRigidBody().applyCentralForce(thrust.add(yawThrust).multLocal(-1));
			} else {
				Quadz.LOGGER.warn("Infinite thrust force!");
			}
		}
	}

	public void rotate(float x, float y, float z) {
		var rot = new com.mojang.math.Quaternion(com.mojang.math.Quaternion.ONE);
		QuaternionHelper.rotateX(rot, x);
		QuaternionHelper.rotateY(rot, y);
		QuaternionHelper.rotateZ(rot, z);

		Transform trans = getRigidBody().getTransform(new Transform());
		trans.getRotation().set(trans.getRotation().mult(Convert.toBullet(rot)));
		getRigidBody().setPhysicsTransform(trans);
	}

	@Override
	public Direction getDirection() {
		return Direction.fromYRot(QuaternionHelper.getYaw(Convert.toMinecraft(getPhysicsRotation(new Quaternion(), 1.0f))));
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		getEntityData().define(THRUST, 0.0f);
		getEntityData().define(THRUST_CURVE, 0.0f);
	}

	@Override
	public float getViewYRot(float tickDelta) {
		return QuaternionHelper.getYaw(Convert.toMinecraft(getPhysicsRotation(new Quaternion(), tickDelta)));
	}

	@Override
	public float getViewXRot(float tickDelta) {
		return QuaternionHelper.getPitch(Convert.toMinecraft(getPhysicsRotation(new Quaternion(), tickDelta)));
	}

	public void setThrust(float thrust) {
		getEntityData().set(THRUST, thrust);
	}

	public float getThrust() {
		return getEntityData().get(THRUST);
	}

	public void setThrustCurve(float thrustCurve) {
		getEntityData().set(THRUST_CURVE, thrustCurve);
	}

	public float getThrustCurve() {
		return getEntityData().get(THRUST_CURVE);
	}

	@Override
	public EntityRigidBody getRigidBody() {
		return this.rigidBody;
	}
}