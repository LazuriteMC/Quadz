package dev.lazurite.quadz.common.state.entity;

import com.jme3.math.Quaternion;
import com.jme3.math.Transform;
import com.jme3.math.Vector3f;
import dev.lazurite.quadz.client.Config;
import dev.lazurite.quadz.client.input.InputTick;
import dev.lazurite.quadz.client.input.Mode;
import dev.lazurite.quadz.client.render.QuadzRendering;
import dev.lazurite.quadz.client.render.ui.screen.QuadcopterScreen;
import dev.lazurite.quadz.client.render.ui.toast.ControllerNotFoundToast;
import dev.lazurite.quadz.common.data.DataDriver;
import dev.lazurite.quadz.common.data.model.Template;
import dev.lazurite.quadz.common.state.item.StackQuadcopterState;
import dev.lazurite.quadz.common.util.Matrix4fAccess;
import dev.lazurite.quadz.common.util.input.InputFrame;
import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.common.state.Bindable;
import dev.lazurite.quadz.common.state.QuadcopterState;
import dev.lazurite.rayon.core.impl.bullet.collision.space.MinecraftSpace;
import dev.lazurite.rayon.core.impl.bullet.math.Converter;
import dev.lazurite.rayon.core.impl.bullet.thread.PhysicsThread;
import dev.lazurite.rayon.entity.api.EntityPhysicsElement;
import dev.lazurite.rayon.entity.impl.collision.body.EntityRigidBody;
import dev.lazurite.toolbox.api.math.QuaternionHelper;
import dev.lazurite.toolbox.api.render.Viewable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
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
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
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
import java.util.Optional;

public class QuadcopterEntity extends LivingEntity implements QuadcopterState, IAnimatable, EntityPhysicsElement, Viewable {
	/* States */
	private static final TrackedData<Boolean> ACTIVE = DataTracker.registerData(QuadcopterEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	private static final TrackedData<String> TEMPLATE = DataTracker.registerData(QuadcopterEntity.class, TrackedDataHandlerRegistry.STRING);

	/* Data */
	private static final TrackedData<Integer> BIND_ID = DataTracker.registerData(QuadcopterEntity.class, TrackedDataHandlerRegistry.INTEGER);

	/* Physical Attributes */
	private static final TrackedData<Integer> CAMERA_ANGLE = DataTracker.registerData(QuadcopterEntity.class, TrackedDataHandlerRegistry.INTEGER);
	private static final TrackedData<Float> THRUST = DataTracker.registerData(QuadcopterEntity.class, TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<Float> THRUST_CURVE = DataTracker.registerData(QuadcopterEntity.class, TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<Float> WIDTH = DataTracker.registerData(QuadcopterEntity.class, TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<Float> HEIGHT = DataTracker.registerData(QuadcopterEntity.class, TrackedDataHandlerRegistry.FLOAT);

	private final AnimationFactory animationFactory = new AnimationFactory(this);
	private final EntityRigidBody rigidBody = new EntityRigidBody(this);
	private final InputFrame inputFrame = new InputFrame();
	private ServerPlayerEntity lastPlayer;
	private String prevTemplate;

	public QuadcopterEntity(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
		this.ignoreCameraFrustum = true;
	}

	@Override
	public void tick() {
		if (!getTemplate().equals(prevTemplate)) {
			prevTemplate = getTemplate();
			Template template = DataDriver.getTemplate(getTemplate());

			if (template != null) {
				setWidth(template.getSettings().getWidth());
				setHeight(template.getSettings().getHeight());
				calculateDimensions();
				setThrust(template.getSettings().getThrust());
				setThrustCurve(template.getSettings().getThrustCurve());

				if (getCameraAngle() == 0) {
					setCameraAngle(template.getSettings().getCameraAngle());
				}

				PhysicsThread.get(world).execute(() -> {
					getRigidBody().setMass(template.getSettings().getMass());
					getRigidBody().setDragCoefficient(template.getSettings().getDragCoefficient());
				});
			}
		}

		if (!world.isClient) {
			Optional<ServerPlayerEntity> player = QuadcopterState.reverseLookup(this);

			if (player.isPresent()) {
				lastPlayer = player.get();
			} else if (lastPlayer != null) {
				this.inputFrame.set(new InputFrame());
				this.sendInputFrame();
				lastPlayer = null;
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

				float currentPitch = -1.0F * (float) Math.toDegrees(QuaternionHelper.toEulerAngles(Converter.toMinecraft(getRigidBody().getPhysicsRotation(new Quaternion()))).getY());
				float currentRoll = -1.0F * (float) Math.toDegrees(QuaternionHelper.toEulerAngles(Converter.toMinecraft(getRigidBody().getPhysicsRotation(new Quaternion()))).getX());

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
			Matrix4f mat = new Matrix4f();
			Matrix4fAccess.from(mat).fromQuaternion(QuaternionHelper.rotateX(Converter.toMinecraft(getRigidBody().getPhysicsRotation(new Quaternion())), 90));
			Vector3f unit = Matrix4fAccess.from(mat).matrixToVector();

			/* Calculate basic thrust */
			Vector3f thrust = new Vector3f().set(unit).multLocal((float) (getThrust() * (Math.pow(frame.getThrottle(), getThrustCurve()))));

			/* Calculate thrust from yaw spin */
			Vector3f yawThrust = new Vector3f().set(unit).multLocal(Math.abs(frame.calculateYaw(0.05f) * getThrust() * 0.002f));

			/* Add up the net thrust and apply the force */
			if (Float.isFinite(thrust.length())) {
				getRigidBody().applyCentralForce(thrust.add(yawThrust).multLocal(-1));
			} else {
				Quadz.LOGGER.warn("Infinite thrust force!");
			}
		}
	}

	public void rotate(float x, float y, float z) {
		var rot = new net.minecraft.util.math.Quaternion(net.minecraft.util.math.Quaternion.IDENTITY);
		QuaternionHelper.rotateX(rot, x);
		QuaternionHelper.rotateY(rot, y);
		QuaternionHelper.rotateZ(rot, z);

		Transform trans = getRigidBody().getTransform(new Transform());
		trans.getRotation().set(trans.getRotation().mult(Converter.toBullet(rot)));
		getRigidBody().setPhysicsTransform(trans);
	}

	public void sendInputFrame() {
		InputFrame frame = getInputFrame();

		if (frame != null) {
			PacketByteBuf buf = PacketByteBufs.create();
			buf.writeInt(getId());
			buf.writeFloat(frame.getThrottle());
			buf.writeFloat(frame.getPitch());
			buf.writeFloat(frame.getYaw());
			buf.writeFloat(frame.getRoll());
			buf.writeFloat(frame.getRate());
			buf.writeFloat(frame.getSuperRate());
			buf.writeFloat(frame.getExpo());
			buf.writeFloat(frame.getMaxAngle());
			buf.writeEnumConstant(frame.getMode());

			if (world.isClient()) {
				ClientPlayNetworking.send(Quadz.INPUT_FRAME, buf);
			} else {
				PlayerLookup.tracking(this).forEach(player -> ServerPlayNetworking.send(player, Quadz.INPUT_FRAME, buf));
			}
		}
	}

	@Override
	public boolean damage(DamageSource source, float amount) {
		if (!world.isClient() && source.getAttacker() instanceof PlayerEntity) {
			this.kill();
			return true;
		}

		return false;
	}

	@Override
	public void kill() {
		this.dropSpawner();
		this.remove(RemovalReason.KILLED);
	}

	/**
	 * Copies the {@link Template} along with other
	 * necessary information from this {@link QuadcopterState}
	 * to the new {@link StackQuadcopterState}.
	 */
	public void dropSpawner() {
		ItemStack stack = new ItemStack(Quadz.QUADCOPTER_ITEM);
		QuadcopterState.fromStack(stack).ifPresent(state -> {
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
		return Direction.fromRotation(QuaternionHelper.getYaw(Converter.toMinecraft(getPhysicsRotation(new Quaternion(), 1.0f))));
	}

	@Override
	public ActionResult interact(PlayerEntity player, Hand hand) {
		ItemStack stack = player.getInventory().getMainHandStack();

		if (!world.isClient()) {
			Bindable.get(stack).ifPresent(bindable -> {
				Bindable.bind(this, bindable);
				player.sendMessage(new TranslatableText("item.quadz.transmitter_item.bound"), true);
			});
		} else {
			if (stack.getItem().equals(Quadz.TRANSMITTER_ITEM)) {
				if (!InputTick.controllerExists() && Config.getInstance().controllerId != -1) {
					ControllerNotFoundToast.add();
				}
			} else if (!getTemplate().isEmpty()) {
				QuadcopterScreen.show(this);
			}
		}

		return ActionResult.SUCCESS;
	}

	@Override
	public void readCustomDataFromNbt(NbtCompound tag) {
		super.readCustomDataFromNbt(tag);
		setTemplate(tag.getString("template"));
		setBindId(tag.getInt("bind_id"));
		setCameraAngle(tag.getInt("camera_angle"));
	}

	@Override
	public void writeCustomDataToNbt(NbtCompound tag) {
		super.writeCustomDataToNbt(tag);
		tag.putString("template", getTemplate());
		tag.putInt("bind_id", getBindId());
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
		getDataTracker().startTracking(ACTIVE, false);
		getDataTracker().startTracking(TEMPLATE, "");

		getDataTracker().startTracking(BIND_ID, -1);

		getDataTracker().startTracking(CAMERA_ANGLE, 0);
		getDataTracker().startTracking(THRUST, 0.0f);
		getDataTracker().startTracking(THRUST_CURVE, 0.0f);
		getDataTracker().startTracking(WIDTH, -1.0f);
		getDataTracker().startTracking(HEIGHT, -1.0f);
	}

	@Override
	public float getYaw(float tickDelta) {
		return QuaternionHelper.getYaw(Converter.toMinecraft(getPhysicsRotation(new Quaternion(), tickDelta)));
	}

	@Override
	public float getPitch(float tickDelta) {
		return QuaternionHelper.getPitch(Converter.toMinecraft(getPhysicsRotation(new Quaternion(), tickDelta)));
//		return QuaternionHelper.getPitch(QuaternionHelper.rotateX(
//				getPhysicsRotation(new Quaternion(), tickDelta),
//				-getCameraAngle()));
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
	public void setTemplate(String template) {
		getDataTracker().set(TEMPLATE, template);
	}

	@Override
	public void setCameraAngle(int cameraAngle) {
		getDataTracker().set(CAMERA_ANGLE, cameraAngle);
	}

	@Override
	public int getCameraAngle() {
		return getDataTracker().get(CAMERA_ANGLE);
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

	public void setWidth(float width) {
		getDataTracker().set(WIDTH, width);
	}

	@Override
	public float getWidth() {
		return getDataTracker().get(WIDTH);
	}

	public void setHeight(float height) {
		getDataTracker().set(HEIGHT, height);
	}

	@Override
	public float getHeight() {
		return getDataTracker().get(HEIGHT);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public boolean shouldRenderSelf() {
		return (!Config.getInstance().renderCameraInCenter && Config.getInstance().renderFirstPerson) || QuadzRendering.isInThirdPerson();
	}

	@Override
	@Environment(EnvType.CLIENT)
	public boolean shouldRenderPlayer() {
		return true;
	}

	/* Called each frame */
	@Environment(EnvType.CLIENT)
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
	public EntityRigidBody getRigidBody() {
		return this.rigidBody;
	}
}