package dev.lazurite.quadz.common.entity;

import com.jme3.math.Quaternion;
import com.jme3.math.Transform;
import com.jme3.math.Vector3f;
import dev.lazurite.lattice.api.entity.Viewable;
import dev.lazurite.quadz.client.input.InputTick;
import dev.lazurite.quadz.client.input.Mode;
import dev.lazurite.quadz.client.render.ui.toast.ControllerNotFoundToast;
import dev.lazurite.quadz.common.util.type.access.Matrix4fAccess;
import dev.lazurite.quadz.client.input.frame.InputFrame;
import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.common.util.type.Bindable;
import dev.lazurite.quadz.common.util.type.QuadcopterState;
import dev.lazurite.quadz.common.util.CustomTrackedDataHandlerRegistry;
import dev.lazurite.quadz.common.util.Frequency;
import dev.lazurite.rayon.core.impl.physics.space.MinecraftSpace;
import dev.lazurite.rayon.core.impl.util.math.QuaternionHelper;
import dev.lazurite.rayon.entity.api.EntityPhysicsElement;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.world.World;

import java.util.ArrayList;

@SuppressWarnings("EntityConstructor")
public abstract class QuadcopterEntity extends LivingEntity implements EntityPhysicsElement, Viewable, QuadcopterState {
	private static final TrackedData<Boolean> GOD_MODE = DataTracker.registerData(QuadcopterEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	private static final TrackedData<Integer> BIND_ID = DataTracker.registerData(QuadcopterEntity.class, TrackedDataHandlerRegistry.INTEGER);
	private static final TrackedData<Boolean> ACTIVE = DataTracker.registerData(QuadcopterEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	private static final TrackedData<Frequency> FREQUENCY = DataTracker.registerData(QuadcopterEntity.class, CustomTrackedDataHandlerRegistry.FREQUENCY);
	private static final TrackedData<Integer> CAMERA_ANGLE = DataTracker.registerData(QuadcopterEntity.class, TrackedDataHandlerRegistry.INTEGER);

	private final InputFrame inputFrame = new InputFrame();

	public QuadcopterEntity(EntityType<? extends LivingEntity> type, World world) {
		super(type, world);
	}

	public abstract float getThrustForce();

	public abstract float getThrustCurve();

	public abstract void dropSpawner();

	@Override
	public void step(MinecraftSpace space) {
		InputFrame frame = new InputFrame(getInputFrame());
		float delta = 0.05f;

		/* Rotate the quadcopter based on user input */
		if (!frame.isEmpty()) {
			if (Mode.RATE.equals(frame.getMode())) {
				rotate(frame.calculatePitch(delta), frame.calculateYaw(delta), frame.calculateRoll(delta));
			} else if (Mode.ANGLE.equals(frame.getMode())) {
				float targetPitch = -frame.getPitch() * frame.getMaxAngle();
				float targetRoll = -frame.getRoll() * frame.getMaxAngle();

				float currentPitch = -1.0F * (float) Math.toDegrees(QuaternionHelper.toEulerAngles(getRigidBody().getPhysicsRotation(new Quaternion())).y);
				float currentRoll = -1.0F * (float) Math.toDegrees(QuaternionHelper.toEulerAngles(getRigidBody().getPhysicsRotation(new Quaternion())).x);

				rotate(currentPitch - targetPitch, frame.calculateYaw(delta), currentRoll - targetRoll);
			}

			/* Decrease angular velocity hack */
			if (frame.getThrottle() > 0.1f) {
				getRigidBody().setAngularVelocity(getRigidBody().getAngularVelocity(new Vector3f()).multLocal(0.5f * frame.getThrottle()));
			}

			/* Get the thrust unit vector */
			Matrix4f mat = new Matrix4f();
			Matrix4fAccess.from(mat).fromQuaternion(QuaternionHelper.bulletToMinecraft(
					QuaternionHelper.rotateX(getRigidBody().getPhysicsRotation(new Quaternion()), 90)));
			Vector3f unit = Matrix4fAccess.from(mat).matrixToVector();

			/* Calculate basic thrust */
			Vector3f thrust = new Vector3f();
			thrust.set(unit);
			thrust.multLocal((float) (getThrustForce() * (Math.pow(frame.getThrottle(), getThrustCurve()))));

			/* Calculate thrust from yaw spin */
			Vector3f yawThrust = new Vector3f();
			yawThrust.set(unit);

			/* Add up the net thrust and apply the force */
			yawThrust.multLocal(Math.abs(frame.calculateYaw(delta) * 0.01f * getThrustForce()));
			if (Float.isFinite(thrust.length())) {
				getRigidBody().applyCentralForce(thrust.add(yawThrust).multLocal(-1));
			} else {
				Quadz.LOGGER.warn("Infinite thrust force!");
			}
		}
	}

	public void rotate(float x, float y, float z) {
		Quaternion rot = new Quaternion();
		QuaternionHelper.rotateX(rot, x);
		QuaternionHelper.rotateY(rot, y);
		QuaternionHelper.rotateZ(rot, z);

		Transform trans = getRigidBody().getTransform(new Transform());
		trans.getRotation().set(trans.getRotation().mult(rot));
		getRigidBody().setPhysicsTransform(trans);
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
	public Iterable<ItemStack> getArmorItems() {
		return new ArrayList<>();
	}

	@Override
	public ItemStack getEquippedStack(EquipmentSlot slot) {
		return new ItemStack(Items.AIR);
	}

	@Override
	public void equipStack(EquipmentSlot slot, ItemStack stack) { }

	@Override
	public Direction getHorizontalFacing() {
		return Direction.fromRotation(QuaternionHelper.getYaw(getRigidBody().getPhysicsRotation(new Quaternion())));
	}

	@Override
	public ActionResult interact(PlayerEntity player, Hand hand) {
		ItemStack stack = player.inventory.getMainHandStack();

		if (!world.isClient()) {
			if (stack.getItem().equals(Quadz.TRANSMITTER_ITEM)) {
				Bindable.bind(this, Quadz.TRANSMITTER_CONTAINER.get(stack));
				player.sendMessage(new LiteralText("Transmitter bound"), true);



				System.out.println(Quadz.TRANSMITTER_CONTAINER.get(stack).getBindId() + " == " + this.getBindId());
			} else if (stack.getItem().equals(Quadz.CHANNEL_WAND_ITEM)) {
				Frequency frequency = getFrequency();
				player.sendMessage(new LiteralText("Frequency: " + frequency.getFrequency() + " (Band: " + frequency.getBand() + " Channel: " + frequency.getChannel() + ")"), true);
			}
		} else {
			if (stack.getItem().equals(Quadz.TRANSMITTER_ITEM)) {
				if (!InputTick.controllerExists()) {
					ControllerNotFoundToast.add();
				}
			}
		}

		return ActionResult.SUCCESS;
	}

	@Override
	public void readCustomDataFromTag(CompoundTag tag) {
		setGodMode(tag.getBoolean("god_mode"));
		setBindId(tag.getInt("bind_id"));
		setFrequency(new Frequency((char) tag.getInt("band"), tag.getInt("channel")));
		setCameraAngle(tag.getInt("camera_angle"));
	}

	@Override
	public void writeCustomDataToTag(CompoundTag tag) {
		tag.putBoolean("god_mode", isInGodMode());
		tag.putInt("bind_id", getBindId());
		tag.putInt("band", getFrequency().getBand());
		tag.putInt("channel", getFrequency().getChannel());
		tag.putInt("camera_angle", getCameraAngle());
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
	}

	@Override
	public float getYaw(float tickDelta) {
		return QuaternionHelper.getYaw(getPhysicsRotation(new Quaternion(), tickDelta));
	}

	@Override
	public float getPitch(float tickDelta) {
		return QuaternionHelper.getPitch(QuaternionHelper.rotateX(
				getPhysicsRotation(new Quaternion(), tickDelta),
				-getCameraAngle()));
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

	public void setInputFrame(InputFrame frame) {
		this.inputFrame.set(frame);
	}

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

	@Override
	public boolean shouldRenderSelf() {
		return false; // for now
	}

	@Override
	public boolean shouldRenderPlayer() {
		return true;
	}
}