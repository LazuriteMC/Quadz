package dev.lazurite.quadz.common.state.entity;

import com.jme3.math.Quaternion;
import com.jme3.math.Transform;
import com.jme3.math.Vector3f;
import dev.lazurite.lattice.api.entity.Viewable;
import dev.lazurite.quadz.client.Config;
import dev.lazurite.quadz.client.input.InputTick;
import dev.lazurite.quadz.client.input.Mode;
import dev.lazurite.quadz.client.render.ui.toast.ControllerNotFoundToast;
import dev.lazurite.quadz.common.data.DataDriver;
import dev.lazurite.quadz.common.data.model.Template;
import dev.lazurite.quadz.common.util.Matrix4fAccess;
import dev.lazurite.quadz.common.util.input.InputFrame;
import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.common.state.Bindable;
import dev.lazurite.quadz.common.state.QuadcopterState;
import dev.lazurite.quadz.common.util.CustomTrackedDataHandlerRegistry;
import dev.lazurite.quadz.common.util.Frequency;
import dev.lazurite.rayon.core.impl.physics.PhysicsThread;
import dev.lazurite.rayon.core.impl.physics.space.MinecraftSpace;
import dev.lazurite.rayon.core.impl.physics.space.body.ElementRigidBody;
import dev.lazurite.rayon.core.impl.physics.space.body.shape.BoundingBoxShape;
import dev.lazurite.rayon.core.impl.util.math.QuaternionHelper;
import dev.lazurite.rayon.entity.api.EntityPhysicsElement;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
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
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import java.util.ArrayList;

@SuppressWarnings("EntityConstructor")
public class QuadcopterEntity extends LivingEntity implements IAnimatable, EntityPhysicsElement, Viewable, QuadcopterState {
	private static final TrackedData<Boolean> GOD_MODE = DataTracker.registerData(QuadcopterEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	private static final TrackedData<Integer> BIND_ID = DataTracker.registerData(QuadcopterEntity.class, TrackedDataHandlerRegistry.INTEGER);
	private static final TrackedData<Boolean> ACTIVE = DataTracker.registerData(QuadcopterEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	private static final TrackedData<Frequency> FREQUENCY = DataTracker.registerData(QuadcopterEntity.class, CustomTrackedDataHandlerRegistry.FREQUENCY);
	private static final TrackedData<Integer> CAMERA_ANGLE = DataTracker.registerData(QuadcopterEntity.class, TrackedDataHandlerRegistry.INTEGER);
	private static final TrackedData<String> TEMPLATE = DataTracker.registerData(QuadcopterEntity.class, TrackedDataHandlerRegistry.STRING);
	private static final TrackedData<Float> THRUST = DataTracker.registerData(QuadcopterEntity.class, TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<Float> THRUST_CURVE = DataTracker.registerData(QuadcopterEntity.class, TrackedDataHandlerRegistry.FLOAT);

	private final AnimationFactory animationFactory = new AnimationFactory(this);
	private final ElementRigidBody rigidBody = new ElementRigidBody(this);
	private final InputFrame inputFrame = new InputFrame();
	private String prevTemplate;

	public QuadcopterEntity(World world) {
		super(Quadz.QUADCOPTER_ENTITY, world);
	}

	public QuadcopterEntity(World world, String template) {
		this(world);
		this.ignoreCameraFrustum = true;
		setTemplate(template);
	}

	@Override
	public void tick() {
		if (!getTemplate().equals(prevTemplate)) {
			prevTemplate = getTemplate();
			Template template = DataDriver.getTemplate(getTemplate());

			if (template != null) {
				calculateDimensions();
				setCameraAngle(template.getSettings().getCameraAngle());
				setThrust(template.getSettings().getThrust());
				setThrustCurve(template.getSettings().getThrustCurve());

				PhysicsThread.get(world).execute(() -> {
					getRigidBody().setMass(template.getSettings().getMass());
					getRigidBody().setDragCoefficient(template.getSettings().getDragCoefficient());
					getRigidBody().setCollisionShape(new BoundingBoxShape(getBoundingBox()));
				});
			}
		}

		super.tick();
	}

	@Override
	public void step(MinecraftSpace space) {
		InputFrame frame = new InputFrame(getInputFrame());

		if (!frame.isEmpty()) {

			/* Rate Mode */
			if (Mode.RATE.equals(frame.getMode())) {
				rotate(frame.calculatePitch(0.05f), frame.calculateYaw(0.05f), frame.calculateRoll(0.05f));

			/* Self Leveling Mode */
			} else if (Mode.ANGLE.equals(frame.getMode())) {
				float targetPitch = -frame.getPitch() * frame.getMaxAngle();
				float targetRoll = -frame.getRoll() * frame.getMaxAngle();

				float currentPitch = -1.0F * (float) Math.toDegrees(QuaternionHelper.toEulerAngles(getRigidBody().getPhysicsRotation(new Quaternion())).y);
				float currentRoll = -1.0F * (float) Math.toDegrees(QuaternionHelper.toEulerAngles(getRigidBody().getPhysicsRotation(new Quaternion())).x);

				rotate(currentPitch - targetPitch, frame.calculateYaw(0.05f), currentRoll - targetRoll);
			}

			/* Decrease angular velocity */
			if (frame.getThrottle() > 0.1f) {
				getRigidBody().setAngularVelocity(getRigidBody().getAngularVelocity(new Vector3f()).multLocal(0.5f * frame.getThrottle()));
			}

			/* Get the thrust unit vector */
			Matrix4f mat = new Matrix4f();
			Matrix4fAccess.from(mat).fromQuaternion(QuaternionHelper.bulletToMinecraft(
					QuaternionHelper.rotateX(getRigidBody().getPhysicsRotation(new Quaternion()), 90)));
			Vector3f unit = Matrix4fAccess.from(mat).matrixToVector();

			/* Calculate basic thrust */
			Vector3f thrust = new Vector3f().set(unit)
					.multLocal((float) (getThrust() * (Math.pow(frame.getThrottle(), getThrustCurve()))));

			/* Calculate thrust from yaw spin */
			Vector3f yawThrust = new Vector3f().set(unit)
					.multLocal(Math.abs(frame.calculateYaw(0.05f) * getThrust() * 0.002f));

			/* Add up the net thrust and apply the force */
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

	@Environment(EnvType.CLIENT)
	public void sendInputFrame() {
		InputFrame frame = getInputFrame();

		if (!frame.isEmpty()) {
			PacketByteBuf buf = PacketByteBufs.create();
			buf.writeInt(getEntityId());
			buf.writeFloat(frame.getThrottle());
			buf.writeFloat(frame.getPitch());
			buf.writeFloat(frame.getYaw());
			buf.writeFloat(frame.getRoll());
			buf.writeFloat(frame.getRate());
			buf.writeFloat(frame.getSuperRate());
			buf.writeFloat(frame.getExpo());
			buf.writeFloat(frame.getMaxAngle());
			buf.writeEnumConstant(frame.getMode());
			ClientPlayNetworking.send(Quadz.INPUT_FRAME_C2S, buf);
		}
	}

	@Override
	public boolean damage(DamageSource source, float amount) {
		if (!world.isClient()) {
			if (source.equals(DamageSource.OUT_OF_WORLD)) {
				this.remove();
				return true;
			}

			if (source.getAttacker() instanceof PlayerEntity) {
				this.dropSpawner();
				this.remove();
			}
		}

		return !isInGodMode();
	}

	public void dropSpawner() {
		ItemStack stack = new ItemStack(Quadz.QUADCOPTER_ITEM);
		QuadcopterState.get(stack).ifPresent(state -> {
			state.copyFrom(this);
			this.dropStack(stack);
		});
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
		return Direction.fromRotation(QuaternionHelper.getYaw(getPhysicsRotation(new Quaternion(), 1.0f)));
	}

	@Override
	public ActionResult interact(PlayerEntity player, Hand hand) {
		ItemStack stack = player.inventory.getMainHandStack();

		if (!world.isClient()) {
			if (stack.getItem().equals(Quadz.TRANSMITTER_ITEM)) {
				Bindable.get(stack).ifPresent(bindable -> {
					Bindable.bind(this, bindable);
					player.sendMessage(new LiteralText("Transmitter bound"), true);
				});
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
		super.readCustomDataFromTag(tag);
		setGodMode(tag.getBoolean("god_mode"));
		setBindId(tag.getInt("bind_id"));
		setFrequency(new Frequency((char) tag.getInt("band"), tag.getInt("channel")));
		setCameraAngle(tag.getInt("camera_angle"));
		setTemplate(tag.getString("template"));
	}

	@Override
	public void writeCustomDataToTag(CompoundTag tag) {
		super.writeCustomDataToTag(tag);
		tag.putBoolean("god_mode", isInGodMode());
		tag.putInt("bind_id", getBindId());
		tag.putInt("band", getFrequency().getBand());
		tag.putInt("channel", getFrequency().getChannel());
		tag.putInt("camera_angle", getCameraAngle());
		tag.putString("template", getTemplate());
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
		getDataTracker().startTracking(TEMPLATE, "");
		getDataTracker().startTracking(THRUST, 0.0f);
		getDataTracker().startTracking(THRUST_CURVE, 0.0f);
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

	public InputFrame getInputFrame() {
		return this.inputFrame;
	}

	@Override
	public void setFrequency(Frequency frequency) {
		getDataTracker().set(FREQUENCY, frequency);
	}

	@Override
	public void setTemplate(String template) {
		getDataTracker().set(TEMPLATE, template);
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

	public void setThrust(float thrust) {
		getDataTracker().set(THRUST, thrust);
	}

	public float getThrust() {
		return getDataTracker().get(THRUST);
	}

	public void setThrustCurve(float thrustCurve) {
		getDataTracker().set(THRUST_CURVE, thrustCurve);
	}

	public float getThrustCurve() {
		return getDataTracker().get(THRUST_CURVE);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public boolean shouldRenderSelf() {
		return true;//Config.getInstance().renderFirstPerson;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public boolean shouldRenderPlayer() {
		return true;
	}

	/* Called each frame */
	private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
		return isActive() ? PlayState.CONTINUE : PlayState.STOP;
	}

	@Override
	public void registerControllers(AnimationData animationData) {
		AnimationController<QuadcopterEntity> controller = new AnimationController<>(this, "quadcopter_controller", 0, this::predicate);
		controller.setAnimation(new AnimationBuilder().addAnimation("armed", true));
		animationData.addAnimationController(controller);
	}

	@Override
	public AnimationFactory getFactory() {
		return this.animationFactory;
	}

	@Override
	public String getTemplate() {
		return getDataTracker().get(TEMPLATE);
	}

	@Override
	public ElementRigidBody getRigidBody() {
		return this.rigidBody;
	}
}