package dev.lazurite.quadz.common.entity;

import com.jme3.math.Quaternion;
import com.jme3.math.Transform;
import com.jme3.math.Vector3f;
import dev.lazurite.quadz.client.input.Mode;
import dev.lazurite.quadz.common.util.access.Matrix4fAccess;
import dev.lazurite.quadz.client.input.frame.InputFrame;
import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.common.util.type.Bindable;
import dev.lazurite.quadz.common.util.type.QuadcopterState;
import dev.lazurite.quadz.common.item.ChannelWandItem;
import dev.lazurite.quadz.common.item.TransmitterItem;
import dev.lazurite.quadz.common.util.Axis;
import dev.lazurite.quadz.common.util.CustomTrackedDataHandlerRegistry;
import dev.lazurite.quadz.common.util.Frequency;
import dev.lazurite.rayon.api.element.PhysicsElement;
import dev.lazurite.rayon.impl.bullet.thread.PhysicsThread;
import dev.lazurite.rayon.impl.bullet.world.MinecraftSpace;
import dev.lazurite.rayon.impl.util.math.QuaternionHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;

public abstract class QuadcopterEntity extends LivingEntity implements PhysicsElement, QuadcopterState {
	private static final TrackedData<Boolean> GOD_MODE = DataTracker.registerData(QuadcopterEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	private static final TrackedData<Integer> BIND_ID = DataTracker.registerData(QuadcopterEntity.class, TrackedDataHandlerRegistry.INTEGER);
	private static final TrackedData<Boolean> ACTIVE = DataTracker.registerData(QuadcopterEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

	private static final TrackedData<Frequency> FREQUENCY = DataTracker.registerData(QuadcopterEntity.class, CustomTrackedDataHandlerRegistry.FREQUENCY);
	private static final TrackedData<Integer> CAMERA_ANGLE = DataTracker.registerData(QuadcopterEntity.class, TrackedDataHandlerRegistry.INTEGER);
	private static final TrackedData<Integer> FIELD_OF_VIEW = DataTracker.registerData(QuadcopterEntity.class, TrackedDataHandlerRegistry.INTEGER);
	private static final TrackedData<Integer> POWER = DataTracker.registerData(QuadcopterEntity.class, TrackedDataHandlerRegistry.INTEGER);

	private final InputFrame inputFrame = new InputFrame();

	public QuadcopterEntity(EntityType<? extends LivingEntity> type, World world) {
		super(type, world);
	}

	public abstract float getThrustForce();

	public abstract float getThrustCurve();

	public abstract void dropSpawner();

	@Override
	public void step(MinecraftSpace space) {
		/* Rotate the quadcopter based on user input */
		if (!getInputFrame().isEmpty()) {
			if (Mode.RATE.equals(getInputFrame().getMode())) {
				rotate(Axis.X, getInputFrame().calculatePitch(PhysicsThread.STEP_SIZE));
				rotate(Axis.Y, getInputFrame().calculateYaw(PhysicsThread.STEP_SIZE));
				rotate(Axis.Z, getInputFrame().calculateRoll(PhysicsThread.STEP_SIZE));
			} else if (Mode.ANGLE.equals(getInputFrame().getMode())) {
				float targetPitch = -getInputFrame().getPitch() * getInputFrame().getMaxAngle();
				float targetRoll = -getInputFrame().getRoll() * getInputFrame().getMaxAngle();

				float currentPitch = -1.0F * (float) Math.toDegrees(QuaternionHelper.toEulerAngles(getRigidBody().getPhysicsRotation(new Quaternion())).y);
				float currentRoll = -1.0F * (float) Math.toDegrees(QuaternionHelper.toEulerAngles(getRigidBody().getPhysicsRotation(new Quaternion())).x);

				rotate(Axis.X, currentPitch - targetPitch);
				rotate(Axis.Y, getInputFrame().calculateYaw(PhysicsThread.STEP_SIZE));
				rotate(Axis.Z, currentRoll - targetRoll);
			}

			/* Decrease angular velocity hack */
			if (getInputFrame().getThrottle() > 0.1f) {
				getRigidBody().setAngularVelocity(getRigidBody().getAngularVelocity(new Vector3f()).multLocal(0.5f * getInputFrame().getThrottle()));
			}

			/* Get the thrust direction vector */
			Matrix4f mat = new Matrix4f();
			Matrix4fAccess.from(mat).fromQuaternion(QuaternionHelper.bulletToMinecraft(
					QuaternionHelper.rotateX(getRigidBody().getPhysicsRotation(new Quaternion()), 90)));
			Vector3f direction = Matrix4fAccess.from(mat).matrixToVector();

			/* Calculate basic thrust */
			Vector3f thrust = new Vector3f();
			thrust.set(direction);
			thrust.multLocal((float) (getThrustForce() * (Math.pow(getInputFrame().getThrottle(), getThrustCurve()))));

			/* Calculate thrust from yaw spin */
			Vector3f yawThrust = new Vector3f();
			yawThrust.set(direction);
			yawThrust.multLocal(Math.abs(getInputFrame().getYaw()));

			/* Add up the net thrust and apply the force */
			if (Float.isFinite(thrust.length())) {
				getRigidBody().applyCentralForce(thrust/*.add(yawThrust)*/.multLocal(-1));
			} else {
				Quadz.LOGGER.warn("Infinite thrust force prevented!");
			}
		}
	}

	public void rotate(Axis axis, float deg) {
		Transform trans;

		switch (axis) {
			case X:
				trans = getRigidBody().getTransform(new Transform());
				trans.getRotation().set(QuaternionHelper.rotateX(getRigidBody().getPhysicsRotation(new Quaternion()), deg));
				getRigidBody().setPhysicsTransform(trans);
				break;
			case Y:
				trans = getRigidBody().getTransform(new Transform());
				trans.getRotation().set(QuaternionHelper.rotateY(getRigidBody().getPhysicsRotation(new Quaternion()), deg));
				getRigidBody().setPhysicsTransform(trans);
				break;
			case Z:
				trans = getRigidBody().getTransform(new Transform());
				trans.getRotation().set(QuaternionHelper.rotateZ(getRigidBody().getPhysicsRotation(new Quaternion()), deg));
				getRigidBody().setPhysicsTransform(trans);
				break;
		}
	}

	@Override
	public boolean damage(DamageSource source, float amount) {
		if (source.equals(DamageSource.OUT_OF_WORLD)) {
			this.remove();
			return true;
		}

		if (source.getAttacker() instanceof PlayerEntity) {
			this.dropSpawner();
			this.remove();
		}

		return !isInGodMode();
	}

	@Override
	public void travel(Vec3d pos) {
	}

	@Override
	protected void fall(double heightDifference, boolean onGround, BlockState landedState, BlockPos landedPosition) {
	}

	@Override
	protected void playStepSound(BlockPos pos, BlockState blockIn) {
	}

	@Override
	public Iterable<ItemStack> getArmorItems() {
		return new ArrayList<>();
	}

	@Override
	public ItemStack getEquippedStack(EquipmentSlot slot) {
		return new ItemStack(Items.AIR);
	}

	@Override
	public void equipStack(EquipmentSlot slot, ItemStack stack) {
	}

	@Override
	public ActionResult interact(PlayerEntity player, Hand hand) {
		ItemStack stack = player.inventory.getMainHandStack();

		if (!world.isClient()) {
			if (stack.getItem() instanceof TransmitterItem) {
				Bindable.bind(this, Quadz.TRANSMITTER_CONTAINER.get(stack));
				player.sendMessage(new LiteralText("Transmitter bound"), true);
			} else if (stack.getItem() instanceof ChannelWandItem) {
				Frequency frequency = getFrequency();
				player.sendMessage(new LiteralText("Frequency: " + frequency.getFrequency() + " (Band: " + frequency.getBand() + " Channel: " + frequency.getChannel() + ")"), true);
			}
		}

		// TODO transmitter not found message (toast maybe?)

		return ActionResult.SUCCESS;
	}

	@Override
	public void readCustomDataFromTag(CompoundTag tag) {
		setGodMode(tag.getBoolean("god_mode"));
		setBindId(tag.getInt("bind_id"));
		setFrequency(new Frequency((char) tag.getInt("band"), tag.getInt("channel")));
		setCameraAngle(tag.getInt("camera_angle"));
		setFieldOfView(tag.getInt("field_of_view"));
		setPower(tag.getInt("power"));
	}

	@Override
	public void writeCustomDataToTag(CompoundTag tag) {
		tag.putBoolean("god_mode", isInGodMode());
		tag.putInt("bind_id", getBindId());
		tag.putInt("band", getFrequency().getBand());
		tag.putInt("channel", getFrequency().getChannel());
		tag.putInt("camera_angle", getCameraAngle());
		tag.putInt("field_of_view", getFieldOfView());
		tag.putInt("power", getPower());
	}

	@Override
	@Environment(EnvType.CLIENT)
	public boolean shouldRender(double distance) {
		return true;
	}

	@Override
	protected void initDataTracker() {
		super.initDataTracker();
		getDataTracker().startTracking(GOD_MODE, false);
		getDataTracker().startTracking(BIND_ID, -1);
		getDataTracker().startTracking(ACTIVE, false);
		getDataTracker().startTracking(FREQUENCY, new Frequency());
		getDataTracker().startTracking(CAMERA_ANGLE, 0);
		getDataTracker().startTracking(FIELD_OF_VIEW, 90);
		getDataTracker().startTracking(POWER, 25);
	}

	@Override
	public float getYaw(float tickDelta) {
		return QuaternionHelper.getYaw(getPhysicsRotation(new Quaternion(), tickDelta));
	}

	@Override
	public float getPitch(float tickDelta) {
		return QuaternionHelper.getPitch(getPhysicsRotation(new Quaternion(), tickDelta));
	}

	@Override
	public boolean collides() {
		return true;
	}

	@Override
	public Arm getMainArm() {
		return null;
	}

	@Override
	public void setBindId(int bindId) {
		getDataTracker().set(BIND_ID, bindId);
	}

	@Override
	public int getBindId() {
		return getDataTracker().get(BIND_ID);
	}

	@Override
	public void setInputFrame(InputFrame frame) {
		this.inputFrame.set(frame);
	}

	@Override
	public InputFrame getInputFrame() {
		return this.inputFrame;
	}

	@Override
	public void setFrequency(Frequency frequency) {
		getDataTracker().set(FREQUENCY, frequency);
	}

	@Override
	public Frequency getFrequency() {
		return getDataTracker().get(FREQUENCY);
	}

	@Override
	public void setPower(int milliWatts) {
		getDataTracker().set(POWER, milliWatts);
	}

	@Override
	public int getPower() {
		return getDataTracker().get(POWER);
	}

	@Override
	public void setFieldOfView(int fieldOfView) {
		getDataTracker().set(FIELD_OF_VIEW, fieldOfView);
	}

	@Override
	public int getFieldOfView() {
		return getDataTracker().get(FIELD_OF_VIEW);
	}

	@Override
	public void setCameraAngle(int cameraAngle) {
		getDataTracker().set(CAMERA_ANGLE, cameraAngle);
	}

	@Override
	public int getCameraAngle() {
		return getDataTracker().get(CAMERA_ANGLE);
	}

	public void setGodMode(boolean godMode) {
		getDataTracker().set(GOD_MODE, godMode);
	}

	public boolean isInGodMode() {
		return getDataTracker().get(GOD_MODE);
	}

	public void setActive(boolean active) {
		getDataTracker().set(ACTIVE, active);
	}

	public boolean isActive() {
		return getDataTracker().get(ACTIVE);
	}
}