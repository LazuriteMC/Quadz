package dev.lazurite.quadz.common.state.entity;

import com.jme3.math.Quaternion;
import com.jme3.math.Transform;
import com.jme3.math.Vector3f;
import com.mojang.math.Matrix4f;
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
import dev.lazurite.rayon.api.EntityPhysicsElement;
import dev.lazurite.rayon.impl.bullet.collision.body.ElementRigidBody;
import dev.lazurite.rayon.impl.bullet.collision.body.EntityRigidBody;
import dev.lazurite.rayon.impl.bullet.collision.space.MinecraftSpace;
import dev.lazurite.rayon.impl.bullet.math.Convert;
import dev.lazurite.rayon.impl.bullet.thread.PhysicsThread;
import dev.lazurite.toolbox.api.math.QuaternionHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import java.util.ArrayList;
import java.util.Optional;

public class QuadcopterEntity extends LivingEntity implements QuadcopterState, IAnimatable, EntityPhysicsElement {
	/* States */
	private static final EntityDataAccessor<Boolean> ACTIVE = SynchedEntityData.defineId(QuadcopterEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<String> TEMPLATE = SynchedEntityData.defineId(QuadcopterEntity.class, EntityDataSerializers.STRING);

	/* Data */
	private static final EntityDataAccessor<Integer> BIND_ID = SynchedEntityData.defineId(QuadcopterEntity.class, EntityDataSerializers.INT);

	/* Physical Attributes */
	private static final EntityDataAccessor<Integer> CAMERA_ANGLE = SynchedEntityData.defineId(QuadcopterEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Float> THRUST = SynchedEntityData.defineId(QuadcopterEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> THRUST_CURVE = SynchedEntityData.defineId(QuadcopterEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> WIDTH = SynchedEntityData.defineId(QuadcopterEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> HEIGHT = SynchedEntityData.defineId(QuadcopterEntity.class, EntityDataSerializers.FLOAT);

	private final AnimationFactory animationFactory = new AnimationFactory(this);
	private final EntityRigidBody rigidBody = new EntityRigidBody(this);
	private final InputFrame inputFrame = new InputFrame();
	private ServerPlayer lastPlayer;
	private String prevTemplate;

	public QuadcopterEntity(EntityType<? extends LivingEntity> entityType, Level level) {
		super(entityType, level);
		this.noCulling = true;
	}

	@Override
	public void tick() {
		if (!getTemplate().equals(prevTemplate)) {
			prevTemplate = getTemplate();
			Template template = DataDriver.getTemplate(getTemplate());

			if (template != null) {
				setWidth(template.getSettings().getWidth());
				setHeight(template.getSettings().getHeight());
				refreshDimensions();
				setThrust(template.getSettings().getThrust());
				setThrustCurve(template.getSettings().getThrustCurve());

				if (getCameraAngle() == 0) {
					setCameraAngle(template.getSettings().getCameraAngle());
				}

				PhysicsThread.get(level).execute(() -> {
					getRigidBody().setMass(template.getSettings().getMass());
					getRigidBody().setDragCoefficient(template.getSettings().getDragCoefficient());
				});
			}
		}

		if (!level.isClientSide()) {
			Optional<ServerPlayer> player = QuadcopterState.reverseLookup(this);

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

	public void step() {
		InputFrame frame = new InputFrame(getInputFrame());

		if (!frame.isEmpty()) {
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
			Matrix4f mat = new Matrix4f();
			Matrix4fAccess.from(mat).fromQuaternion(QuaternionHelper.rotateX(Convert.toMinecraft(getRigidBody().getPhysicsRotation(new Quaternion())), 90));
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
		var rot = new com.mojang.math.Quaternion(com.mojang.math.Quaternion.ONE);
		QuaternionHelper.rotateX(rot, x);
		QuaternionHelper.rotateY(rot, y);
		QuaternionHelper.rotateZ(rot, z);

		Transform trans = getRigidBody().getTransform(new Transform());
		trans.getRotation().set(trans.getRotation().mult(Convert.toBullet(rot)));
		getRigidBody().setPhysicsTransform(trans);
	}

	public void sendInputFrame() {
		InputFrame frame = getInputFrame();

		if (frame != null) {
			FriendlyByteBuf buf = PacketByteBufs.create();
			buf.writeInt(getId());
			buf.writeFloat(frame.getThrottle());
			buf.writeFloat(frame.getPitch());
			buf.writeFloat(frame.getYaw());
			buf.writeFloat(frame.getRoll());
			buf.writeFloat(frame.getRate());
			buf.writeFloat(frame.getSuperRate());
			buf.writeFloat(frame.getExpo());
			buf.writeFloat(frame.getMaxAngle());
			buf.writeEnum(frame.getMode());

			if (level.isClientSide()) {
				ClientPlayNetworking.send(Quadz.INPUT_FRAME, buf);
			} else {
				PlayerLookup.tracking(this).forEach(player -> ServerPlayNetworking.send(player, Quadz.INPUT_FRAME, buf));
			}
		}
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		if (!level.isClientSide() && source.getEntity() instanceof Player) {
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
			this.spawnAtLocation(stack);
		});
	}

	@Override
	public Iterable<ItemStack> getArmorSlots() {
		return new ArrayList<>();
	}

	@Override
	public ItemStack getItemBySlot(EquipmentSlot slot) {
		return new ItemStack(Items.AIR);
	}

	@Override
	public void setItemSlot(EquipmentSlot slot, ItemStack stack) { }

	@Override
	public Direction getDirection() {
		return Direction.fromYRot(QuaternionHelper.getYaw(Convert.toMinecraft(getPhysicsRotation(new Quaternion(), 1.0f))));
	}

	@Override
	public InteractionResult interact(Player player, InteractionHand hand) {
		ItemStack stack = player.getInventory().getSelected();

		if (!level.isClientSide()) {
			Bindable.get(stack).ifPresent(bindable -> {
				Bindable.bind(this, bindable);
				player.displayClientMessage(new TranslatableComponent("item.quadz.transmitter_item.bound"), true);
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

		return InteractionResult.SUCCESS;
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		setTemplate(tag.getString("template"));
		setBindId(tag.getInt("bind_id"));
		setCameraAngle(tag.getInt("camera_angle"));
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putString("template", getTemplate());
		tag.putInt("bind_id", getBindId());
		tag.putInt("camera_angle", getCameraAngle());
	}

	@Override
	@Environment(EnvType.CLIENT)
	public boolean shouldRenderAtSqrDistance(double distance) {
		return true;
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		getEntityData().define(ACTIVE, false);
		getEntityData().define(TEMPLATE, "");

		getEntityData().define(BIND_ID, -1);

		getEntityData().define(CAMERA_ANGLE, 0);
		getEntityData().define(THRUST, 0.0f);
		getEntityData().define(THRUST_CURVE, 0.0f);
		getEntityData().define(WIDTH, -1.0f);
		getEntityData().define(HEIGHT, -1.0f);
	}

	@Override
	public float getViewYRot(float tickDelta) {
		return QuaternionHelper.getYaw(Convert.toMinecraft(getPhysicsRotation(new Quaternion(), tickDelta)));
	}

	@Override
	public float getViewXRot(float tickDelta) {
		return QuaternionHelper.getPitch(Convert.toMinecraft(getPhysicsRotation(new Quaternion(), tickDelta)));
//		return QuaternionHelper.getPitch(QuaternionHelper.rotateX(
//				getPhysicsRotation(new Quaternion(), tickDelta),
//				-getCameraAngle()));
	}

	@Override
	public boolean isPickable() {
		return true;
	}

	@Override
	public HumanoidArm getMainArm() {
		return null;
	}

	@Override
	public void setBindId(int bindId) {
		getEntityData().set(BIND_ID, bindId);
	}

	@Override
	public int getBindId() {
		return getEntityData().get(BIND_ID);
	}

	public InputFrame getInputFrame() {
		return this.inputFrame;
	}

	@Override
	public void setTemplate(String template) {
		getEntityData().set(TEMPLATE, template);
	}

	@Override
	public void setCameraAngle(int cameraAngle) {
		getEntityData().set(CAMERA_ANGLE, cameraAngle);
	}

	@Override
	public int getCameraAngle() {
		return getEntityData().get(CAMERA_ANGLE);
	}

	public void setActive(boolean active) {
		getEntityData().set(ACTIVE, active);
	}

	public boolean isActive() {
		return getEntityData().get(ACTIVE);
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

	public void setWidth(float width) {
		getEntityData().set(WIDTH, width);
	}

	@Override
	public float getBbWidth() {
		return getEntityData().get(WIDTH);
	}

	public void setHeight(float height) {
		getEntityData().set(HEIGHT, height);
	}

	@Override
	public float getBbHeight() {
		return getEntityData().get(HEIGHT);
	}

	@Environment(EnvType.CLIENT)
	public boolean shouldRenderSelf() {
		return (!Config.getInstance().renderCameraInCenter && Config.getInstance().renderFirstPerson) || QuadzRendering.isInThirdPerson();
	}

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
		return getEntityData().get(TEMPLATE);
	}

	@Override
	public EntityRigidBody getRigidBody() {
		return this.rigidBody;
	}
}