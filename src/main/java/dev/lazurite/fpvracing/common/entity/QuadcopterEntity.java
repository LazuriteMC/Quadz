package dev.lazurite.fpvracing.common.entity;

import dev.lazurite.fpvracing.client.input.InputFrame;
import dev.lazurite.fpvracing.client.input.InputTick;
import dev.lazurite.fpvracing.FPVRacing;
import dev.lazurite.fpvracing.common.entity.component.Controllable;
import dev.lazurite.fpvracing.common.entity.component.Thrust;
import dev.lazurite.fpvracing.common.entity.component.VideoTransmission;
import dev.lazurite.fpvracing.common.item.ChannelWandItem;
import dev.lazurite.fpvracing.common.item.TransmitterItem;
import dev.lazurite.fpvracing.common.util.BetaflightHelper;
import dev.lazurite.fpvracing.common.util.CustomTrackedDataHandlerRegistry;
import dev.lazurite.fpvracing.common.util.Frequency;
import dev.lazurite.rayon.api.packet.RayonSpawnS2CPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

import java.util.Random;

public abstract class QuadcopterEntity extends Entity implements VideoTransmission, Controllable, Thrust {
	private static final TrackedData<Boolean> GOD_MODE = DataTracker.registerData(QuadcopterEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

	/* Controllable Stuff */
	private static final TrackedData<InputFrame> INPUT_FRAME = DataTracker.registerData(QuadcopterEntity.class, CustomTrackedDataHandlerRegistry.INPUT_FRAME);
	private static final TrackedData<Integer> BIND_ID = DataTracker.registerData(QuadcopterEntity.class, TrackedDataHandlerRegistry.INTEGER);
	private static final TrackedData<Float> RATE = DataTracker.registerData(QuadcopterEntity.class, TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<Float> SUPER_RATE = DataTracker.registerData(QuadcopterEntity.class, TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<Float> EXPO = DataTracker.registerData(QuadcopterEntity.class, TrackedDataHandlerRegistry.FLOAT);

	/* Video Transmission Stuff */
	private static final TrackedData<Frequency> FREQUENCY = DataTracker.registerData(QuadcopterEntity.class, CustomTrackedDataHandlerRegistry.FREQUENCY);
	private static final TrackedData<Integer> CAMERA_ANGLE = DataTracker.registerData(QuadcopterEntity.class, TrackedDataHandlerRegistry.INTEGER);
	private static final TrackedData<Integer> FIELD_OF_VIEW = DataTracker.registerData(QuadcopterEntity.class, TrackedDataHandlerRegistry.INTEGER);
	private static final TrackedData<Integer> POWER = DataTracker.registerData(QuadcopterEntity.class, TrackedDataHandlerRegistry.INTEGER);

	/* Thrust Stuff */
	private static final TrackedData<Float> THRUST_FORCE = DataTracker.registerData(QuadcopterEntity.class, TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<Float> THRUST_CURVE = DataTracker.registerData(QuadcopterEntity.class, TrackedDataHandlerRegistry.FLOAT);

	public QuadcopterEntity(EntityType<?> type, World world) {
		super(type, world);
	}

	public void step(float delta) {
		if (getEntityWorld().isClient()) {
			setInputFrame(InputTick.INSTANCE.getFrame());
		}

		rotateX((float) BetaflightHelper.calculateRates(getInputFrame().getX(), getRate(), getExpo(), getSuperRate(), delta));
		rotateY((float) BetaflightHelper.calculateRates(getInputFrame().getY(), getRate(), getExpo(), getSuperRate(), delta));
		rotateZ((float) BetaflightHelper.calculateRates(getInputFrame().getZ(), getRate(), getExpo(), getSuperRate(), delta));
//        body.applyForce(thrust.getForce());
	}

	@Override
	public boolean damage(DamageSource source, float amount) {
		if (source.getAttacker() instanceof PlayerEntity || (isKillable() && source instanceof ProjectileDamageSource)) {
			this.kill();
			return true;
		}
		return false;
	}

	@Override
	protected void readCustomDataFromTag(CompoundTag tag) {
		setInputFrame(InputFrame.fromTag(tag));
		setBindId(tag.getInt("bind_id"));
		setRate(tag.getFloat("rate"));
		setSuperRate(tag.getFloat("super_rate"));
		setExpo(tag.getFloat("expo"));

		setFrequency(new Frequency((char) tag.getInt("band"), tag.getInt("channel")));
		setCameraAngle(tag.getInt("camera_angle"));
		setFieldOfView(tag.getInt("field_of_view"));
		setPower(tag.getInt("power"));

		setThrustForce(tag.getFloat("thrust_force"));
		setThrustCurve(tag.getFloat("thrust_curve"));
	}

	@Override
	protected void writeCustomDataToTag(CompoundTag tag) {
		getInputFrame().toTag(tag);
		tag.putInt("bind_id", getBindId());
		tag.putFloat("rate", getRate());
		tag.putFloat("super_rate", getSuperRate());
		tag.putFloat("expo", getExpo());

		tag.putInt("band", getFrequency().getBand());
		tag.putInt("channel", getFrequency().getChannel());
		tag.putInt("camera_angle", getCameraAngle());
		tag.putInt("field_of_view", getFieldOfView());
		tag.putInt("power", getPower());

		tag.putFloat("thrust_force", getThrustForce());
		tag.putFloat("thrust_curve", getThrustCurve());
	}

	@Override
	public ActionResult interact(PlayerEntity player, Hand hand) {
		ItemStack stack = player.inventory.getMainHandStack();

		if (stack.getItem() instanceof TransmitterItem) {
			Random rand = new Random();
			setBindId(rand.nextInt(10000));
			player.sendMessage(new LiteralText("Transmitter bound"), false);
		} else if (stack.getItem() instanceof ChannelWandItem) {
			Frequency frequency = getFrequency();
			player.sendMessage(new LiteralText("Frequency: " + frequency.getFrequency() + " (Band: " + frequency.getBand() + " Channel: " + frequency.getChannel() + ")"), false);
		}

		if (world.isClient()) {
			if (!InputTick.controllerExists()) {
				player.sendMessage(new LiteralText("Controller not found"), false);
			}
		}

		return ActionResult.SUCCESS;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public boolean shouldRender(double distance) {
		return true;
	}

	@Override
	public void kill() {
		if (world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
			dropStack(new ItemStack(FPVRacing.DRONE_SPAWNER_ITEM));
		}

		super.kill();
	}

	@Override
	protected void initDataTracker() {
		getDataTracker().startTracking(GOD_MODE, false);
		getDataTracker().startTracking(INPUT_FRAME, new InputFrame());
		getDataTracker().startTracking(BIND_ID, -1);
		getDataTracker().startTracking(RATE, 1.0f);
		getDataTracker().startTracking(SUPER_RATE, 1.0f);
		getDataTracker().startTracking(EXPO, 1.0f);
		getDataTracker().startTracking(FREQUENCY, new Frequency());
		getDataTracker().startTracking(CAMERA_ANGLE, 0);
		getDataTracker().startTracking(FIELD_OF_VIEW, 90);
		getDataTracker().startTracking(THRUST_FORCE, 50.0f);
		getDataTracker().startTracking(THRUST_CURVE, 1.0f);
	}

	@Override
	public Packet<?> createSpawnPacket() {
		return RayonSpawnS2CPacket.get(this);
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
		getDataTracker().set(INPUT_FRAME, frame);
	}

	@Override
	public InputFrame getInputFrame() {
		return getDataTracker().get(INPUT_FRAME);
	}

	@Override
	public void setRate(float rate) {
		getDataTracker().set(RATE, rate);
	}

	@Override
	public void setSuperRate(float superRate) {
		getDataTracker().set(SUPER_RATE, superRate);
	}

	@Override
	public void setExpo(float expo) {
		getDataTracker().set(EXPO, expo);
	}

	@Override
	public float getRate() {
		return getDataTracker().get(RATE);
	}

	@Override
	public float getSuperRate() {
		return getDataTracker().get(SUPER_RATE);
	}

	@Override
	public float getExpo() {
		return getDataTracker().get(EXPO);
	}

	@Override
	public void setThrustForce(float thrust) {
		getDataTracker().set(THRUST_FORCE, thrust);
	}

	@Override
	public float getThrustForce() {
		return getDataTracker().get(THRUST_FORCE);
	}

	@Override
	public void setThrustCurve(float thrustCurve) {
		getDataTracker().set(THRUST_CURVE, thrustCurve);
	}

	@Override
	public float getThrustCurve() {
		return getDataTracker().get(THRUST_CURVE);
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
}
