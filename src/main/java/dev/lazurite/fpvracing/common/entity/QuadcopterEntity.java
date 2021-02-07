package dev.lazurite.fpvracing.common.entity;

import dev.lazurite.fpvracing.common.util.access.Matrix4fAccess;
import dev.lazurite.fpvracing.client.input.InputFrame;
import dev.lazurite.fpvracing.client.input.InputTick;
import dev.lazurite.fpvracing.FPVRacing;
import dev.lazurite.fpvracing.common.util.type.Bindable;
import dev.lazurite.fpvracing.common.util.type.QuadcopterState;
import dev.lazurite.fpvracing.common.item.ChannelWandItem;
import dev.lazurite.fpvracing.common.item.TransmitterItem;
import dev.lazurite.fpvracing.common.util.Axis;
import dev.lazurite.fpvracing.common.util.CustomTrackedDataHandlerRegistry;
import dev.lazurite.fpvracing.common.util.Frequency;
import dev.lazurite.fpvracing.api.event.QuadcopterStepEvents;
import dev.lazurite.rayon.Rayon;
import dev.lazurite.rayon.api.packet.RayonSpawnS2CPacket;
import dev.lazurite.rayon.impl.physics.body.EntityRigidBody;
import dev.lazurite.rayon.impl.util.math.QuaternionHelper;
import dev.lazurite.rayon.impl.util.math.VectorHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.ProjectileDamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import physics.com.jme3.math.Quaternion;
import physics.com.jme3.math.Vector3f;

public abstract class QuadcopterEntity extends Entity implements QuadcopterState {
	private static final TrackedData<Boolean> GOD_MODE = DataTracker.registerData(QuadcopterEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	private static final TrackedData<State> STATE = DataTracker.registerData(QuadcopterEntity.class, CustomTrackedDataHandlerRegistry.STATE);
	private static final TrackedData<Integer> BIND_ID = DataTracker.registerData(QuadcopterEntity.class, TrackedDataHandlerRegistry.INTEGER);

	/* Video Transmission Stuff */
	private static final TrackedData<Frequency> FREQUENCY = DataTracker.registerData(QuadcopterEntity.class, CustomTrackedDataHandlerRegistry.FREQUENCY);
	private static final TrackedData<Integer> CAMERA_ANGLE = DataTracker.registerData(QuadcopterEntity.class, TrackedDataHandlerRegistry.INTEGER);
	private static final TrackedData<Integer> FIELD_OF_VIEW = DataTracker.registerData(QuadcopterEntity.class, TrackedDataHandlerRegistry.INTEGER);
	private static final TrackedData<Integer> POWER = DataTracker.registerData(QuadcopterEntity.class, TrackedDataHandlerRegistry.INTEGER);

	private final InputFrame inputFrame = new InputFrame();

	public QuadcopterEntity(EntityType<?> type, World world) {
		super(type, world);
	}

	public abstract float getThrustForce();
	public abstract float getThrustCurve();

	public void step(float delta) {
		/* Start Step Event */
		QuadcopterStepEvents.START_QUADCOPTER_STEP.invoker().onStartStep(this, delta);

		/* Rotate the quadcopter based on user input */
		rotate(Axis.X, getInputFrame().getPitch());
		rotate(Axis.Y, getInputFrame().getYaw());
		rotate(Axis.Z, getInputFrame().getRoll());

		/* Decrease angular velocity hack */
		decreaseAngularVelocity();

		/* Get the thrust direction vector */
		EntityRigidBody body = Rayon.ENTITY.get(this);
		Quaternion orientation = body.getPhysicsRotation(new Quaternion());
		QuaternionHelper.rotateX(orientation, 90);
		Matrix4f mat = new Matrix4f();
		Matrix4fAccess.from(mat).fromQuaternion(QuaternionHelper.bulletToMinecraft(orientation));

		/* Calculate new vectors based on direction */
		Vector3f direction = Matrix4fAccess.from(mat).matrixToVector();

		Vector3f thrust = new Vector3f();
		thrust.set(direction);
		thrust.multLocal((float) (getThrustForce() * (Math.pow(getInputFrame().getThrottle(), getThrustCurve()))));

		Vector3f yawThrust = new Vector3f();
		yawThrust.set(direction);
		yawThrust.multLocal(Math.abs(getInputFrame().getYaw()));

		/* Add up the net thrust and apply the force */
		Vector3f netThrust = new Vector3f();
		netThrust.add(thrust);
		netThrust.add(yawThrust);
		netThrust.multLocal(-1);
        body.applyCentralForce(netThrust);

		/* Update user input */
		if (getEntityWorld().isClient()) {
			PlayerEntity player = MinecraftClient.getInstance().player;

			if (player != null) {
				ItemStack hand = player.getMainHandStack();

				if (hand.getItem() instanceof TransmitterItem) {
					if (isBound(FPVRacing.TRANSMITTER_CONTAINER.get(hand))) {
						setInputFrame(InputTick.getInstance().getInputFrame(delta));
					}
				}
			}
		}

		/* End Step Event */
		QuadcopterStepEvents.END_QUADCOPTER_STEP.invoker().onEndStep(this, delta);
	}

	public void rotate(Axis axis, float deg) {
		EntityRigidBody body = Rayon.ENTITY.get(this);
		Quaternion orientation = body.getPhysicsRotation(new Quaternion());

		switch(axis) {
			case X:
				body.setPhysicsRotation(QuaternionHelper.rotateX(orientation, deg));
				break;
			case Y:
				body.setPhysicsRotation(QuaternionHelper.rotateY(orientation, deg));
				break;
			case Z:
				body.setPhysicsRotation(QuaternionHelper.rotateZ(orientation, deg));
				break;
		}
	}

	public boolean isBound(Bindable bindable) {
		if (bindable != null) {
			return bindable.getBindId() == getBindId();
		}

		return false;
	}

	protected void decreaseAngularVelocity() {
		EntityRigidBody body = Rayon.ENTITY.get(this);
		body.setAngularVelocity(body.getAngularVelocity(new Vector3f()).multLocal(0.5f));
//		float distance = 0.25f;
//
//		for(int manifoldNum = 0; manifoldNum < body.getDynamicsWorld().getDispatcher().getNumManifolds(); ++manifoldNum) {
//			PersistentManifold manifold = body.getDynamicsWorld().getDispatcher().getManifoldByIndexInternal(manifoldNum);
//
//			BlockRigidBody block;
//			if (manifold.getBody0() instanceof BlockRigidBody && manifold.getBody1().equals(body)) {
//				block = (BlockRigidBody) manifold.getBody0();
//			} else if (manifold.getBody0().equals(body) && manifold.getBody1() instanceof BlockRigidBody) {
//				block = (BlockRigidBody) manifold.getBody1();
//			}
//
//			for(int contactNum = 0; contactNum < manifold.getNumContacts(); ++contactNum) {
//				if (manifold.getContactPoint(contactNum).getDistance() < distance) {
//
//				}
//			}
//		}
	}

	@Override
	public boolean damage(DamageSource source, float amount) {
		if (!isInGodMode()) {
			if (source.getAttacker() instanceof PlayerEntity || source instanceof ProjectileDamageSource) {
				this.kill();
				return true;
			}
		}

		return false;
	}

	@Override
	public ActionResult interact(PlayerEntity player, Hand hand) {
		ItemStack stack = player.inventory.getMainHandStack();

		if (!world.isClient()) {
			if (stack.getItem() instanceof TransmitterItem) {
				Bindable.bind(this, FPVRacing.TRANSMITTER_CONTAINER.get(stack));
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
		setState(State.valueOf(tag.getString("state")));
		setGodMode(tag.getBoolean("god_mode"));
		setBindId(tag.getInt("bind_id"));

		setFrequency(new Frequency((char) tag.getInt("band"), tag.getInt("channel")));
		setCameraAngle(tag.getInt("camera_angle"));
		setFieldOfView(tag.getInt("field_of_view"));
		setPower(tag.getInt("power"));
	}

	@Override
	public void writeCustomDataToTag(CompoundTag tag) {
		tag.putString("state", getState().toString());
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
	public void kill() {
		if (world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
			ItemStack stack = new ItemStack(FPVRacing.VOXEL_RACER_ONE_ITEM);
			CompoundTag tag = new CompoundTag();
			writeCustomDataToTag(tag);
			FPVRacing.QUADCOPTER_CONTAINER.get(stack).readFromNbt(tag);
			dropStack(stack);
		}

		super.kill();
	}

	@Override
	protected void initDataTracker() {
		getDataTracker().startTracking(GOD_MODE, false);
		getDataTracker().startTracking(STATE, State.DISARMED);
		getDataTracker().startTracking(BIND_ID, -1);
		getDataTracker().startTracking(FREQUENCY, new Frequency());
		getDataTracker().startTracking(CAMERA_ANGLE, 0);
		getDataTracker().startTracking(FIELD_OF_VIEW, 90);
		getDataTracker().startTracking(POWER, 25);
	}

	@Override
	public Packet<?> createSpawnPacket() {
		return RayonSpawnS2CPacket.get(this);
	}

	@Override
	public boolean collides() {
		return true;
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

	public void setState(State state) {
		getDataTracker().set(STATE, state);
	}

	public State getState() {
		return getDataTracker().get(STATE);
	}

	public enum State {
		ARMED,
		DISARMED,
		DISABLED
	}
}
