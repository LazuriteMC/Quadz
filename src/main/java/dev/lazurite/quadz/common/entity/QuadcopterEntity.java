package dev.lazurite.quadz.common.entity;

import com.jme3.math.Quaternion;
import com.jme3.math.Transform;
import com.jme3.math.Vector3f;
import com.mojang.math.Matrix4f;
import dev.lazurite.form.api.Templated;
import dev.lazurite.lattice.api.point.ViewPoint;
import dev.lazurite.quadz.Quadz;
import dev.lazurite.quadz.common.item.GogglesItem;
import dev.lazurite.quadz.common.util.BetaflightHelper;
import dev.lazurite.rayon.api.EntityPhysicsElement;
import dev.lazurite.rayon.impl.bullet.collision.body.ElementRigidBody;
import dev.lazurite.quadz.common.util.Matrix4fAccess;
import dev.lazurite.rayon.impl.bullet.collision.body.EntityRigidBody;
import dev.lazurite.rayon.impl.bullet.math.Convert;
import dev.lazurite.remote.api.Bindable;
import dev.lazurite.remote.api.RemoteSearch;
import dev.lazurite.remote.api.entity.RemoteControllableEntity;
import dev.lazurite.remote.impl.common.util.PlayerStorage;
import dev.lazurite.toolbox.api.math.QuaternionHelper;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import java.util.Optional;

public class QuadcopterEntity extends RemoteControllableEntity implements EntityPhysicsElement, Templated, IAnimatable, ViewPoint {
	private static final EntityDataAccessor<String> TEMPLATE = SynchedEntityData.defineId(QuadcopterEntity.class, EntityDataSerializers.STRING);
	private static final EntityDataAccessor<String> PREV_TEMPLATE = SynchedEntityData.defineId(QuadcopterEntity.class, EntityDataSerializers.STRING);
	private final EntityRigidBody rigidBody = new EntityRigidBody(this);
	private final AnimationFactory animationFactory = new AnimationFactory(this);

	private float thrust;
	private float thrustCurve;

	public QuadcopterEntity(EntityType<? extends LivingEntity> entityType, Level level) {
		super(entityType, level);
		this.rigidBody.setBuoyancyType(ElementRigidBody.BuoyancyType.NONE);
	}

	@Override
	public void tick() {
		super.tick();

		if (!getTemplate().equals(getEntityData().get(PREV_TEMPLATE))) {
			getEntityData().set(PREV_TEMPLATE, getTemplate());
			this.refreshDimensions();
		}

		if (!level.isClientSide) {
			Optional.ofNullable(getRigidBody().getPriorityPlayer()).ifPresent(player -> {
				if (!((ServerPlayer) player).getCamera().equals(this)) {
					getRigidBody().prioritize(null);
				}
			});
		}

		RemoteSearch.findPlayer(this).ifPresent(player -> {
			final var pitch = ((PlayerStorage) player).getJoystickValue(new ResourceLocation(Quadz.MODID, "pitch"));
			final var yaw = ((PlayerStorage) player).getJoystickValue(new ResourceLocation(Quadz.MODID, "yaw"));
			final var roll = ((PlayerStorage) player).getJoystickValue(new ResourceLocation(Quadz.MODID, "roll"));
			final var throttle = ((PlayerStorage) player).getJoystickValue(new ResourceLocation(Quadz.MODID, "throttle")) + 1.0f;

			final var rate = ((PlayerStorage) player).getJoystickValue(new ResourceLocation(Quadz.MODID, "rate"));
			final var superRate = ((PlayerStorage) player).getJoystickValue(new ResourceLocation(Quadz.MODID, "super_rate"));
			final var expo = ((PlayerStorage) player).getJoystickValue(new ResourceLocation(Quadz.MODID, "expo"));

			rotate(
					(float) BetaflightHelper.calculateRates(pitch, rate, expo, superRate, 0.05f),
					(float) BetaflightHelper.calculateRates(yaw, rate, expo, superRate, 0.05f),
					(float) BetaflightHelper.calculateRates(roll, rate, expo, superRate, 0.05f)
			);

			/* Decrease angular velocity */
			if (throttle > 0.1f) {
				Vector3f correction = getRigidBody().getAngularVelocity(new Vector3f()).multLocal(0.5f * throttle);

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
			final var thrust = new Vector3f().set(unit).multLocal((float) (getThrust() * (Math.pow(throttle, getThrustCurve()))));

			/* Calculate thrust from yaw spin */
			final var yawThrust = new Vector3f().set(unit).multLocal(Math.abs(yaw * getThrust() * 0.002f));

			/* Add up the net thrust and apply the force */
			if (Float.isFinite(thrust.length())) {
				getRigidBody().applyCentralForce(thrust.add(yawThrust).multLocal(-1));
			} else {
				Quadz.LOGGER.warn("Infinite thrust force!");
			}
		});
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
		getEntityData().define(TEMPLATE, "");
		getEntityData().define(PREV_TEMPLATE, "");
	}

	@Override
	public float getViewYRot(float tickDelta) {
		return QuaternionHelper.getYaw(Convert.toMinecraft(getPhysicsRotation(new Quaternion(), tickDelta)));
	}

	@Override
	public float getViewXRot(float tickDelta) {
		return QuaternionHelper.getPitch(Convert.toMinecraft(getPhysicsRotation(new Quaternion(), tickDelta)));
	}

	public float getThrust() {
		return this.thrust;
	}

	public float getThrustCurve() {
		return this.thrustCurve;
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		setTemplate(tag.getString("template"));
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putString("template", getTemplate());
	}

	@Override
	public EntityRigidBody getRigidBody() {
		return this.rigidBody;
	}

	@Override
	public String getTemplate() {
		return getEntityData().get(TEMPLATE);
	}

	@Override
	public void setTemplate(String template) {
		getEntityData().set(TEMPLATE, template);
	}

	@Override
	public void dropSpawner() {
		final var stack = new ItemStack(Quadz.QUADCOPTER_ITEM);
		Bindable.get(stack).ifPresent(bindable -> bindable.copyFrom(this));
		Templated.get(stack).copyFrom(this);
		this.spawnAtLocation(stack);
	}

	@Override
	public void registerControllers(AnimationData animationData) {
		AnimationController<QuadcopterEntity> controller = new AnimationController<>(this, "quadcopter_entity_controller", 0, event -> isActive() ? PlayState.CONTINUE : PlayState.STOP);
		controller.setAnimation(new AnimationBuilder().addAnimation("armed", true));
		animationData.addAnimationController(controller);
	}

	@Override
	public AnimationFactory getFactory() {
		return this.animationFactory;
	}

	@Override
	public boolean shouldPlayerBeViewing(Player player) {
		return player != null && player.getInventory().armor.get(3).getItem() instanceof GogglesItem;
	}

	@Override
	public boolean shouldRenderPlayer() {
		return true;
	}

	public void setThrust(float thrust) {
		this.thrust = thrust;
	}

	public void setThrustCurve(float thrustCurve) {
		this.thrustCurve = thrustCurve;
	}

	@Override
	public String toString() {
		return "Mass: " + getRigidBody().getMass() + ", Drag: " + getRigidBody().getDragCoefficient() + ", Width: " + getBoundingBox().getXsize() + ", Height: " + getBoundingBox().getYsize() + ", Thrust: " + getThrust() + ", Thrust Curve: " + getThrustCurve();
	}
}