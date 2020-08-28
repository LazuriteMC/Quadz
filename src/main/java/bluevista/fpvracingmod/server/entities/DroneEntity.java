package bluevista.fpvracingmod.server.entities;

import bluevista.fpvracingmod.client.ClientInitializer;
import bluevista.fpvracingmod.client.input.AxisValues;
import bluevista.fpvracingmod.client.input.InputTick;
import bluevista.fpvracingmod.config.Config;
import bluevista.fpvracingmod.math.BetaflightHelper;
import bluevista.fpvracingmod.math.Matrix4fInject;
import bluevista.fpvracingmod.math.QuaternionHelper;
import bluevista.fpvracingmod.network.entity.DroneEntityS2C;
import bluevista.fpvracingmod.server.ServerInitializer;
import bluevista.fpvracingmod.server.items.DroneSpawnerItem;
import bluevista.fpvracingmod.server.items.TransmitterItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.ProjectileDamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.util.List;
import java.util.UUID;

public class DroneEntity extends PhysicsEntity {
	public static final UUID NULL_UUID = new UUID(0, 0);

	private final AxisValues axisValues;

	private boolean infiniteTracking;
	private boolean prevGodMode;
	private boolean godMode;

	private int cameraAngle;
	private float fieldOfView;
	private int band;
	private int channel;

	public DroneEntity(EntityType<?> type, World world) {
		this(world, Vec3d.ZERO, 0);
	}

	public DroneEntity(World world, Vec3d pos, float yaw) {
		super(ServerInitializer.DRONE_ENTITY, world, pos);
		this.rotateY(yaw);

		this.axisValues = new AxisValues();
		this.playerID = NULL_UUID;
		this.noClip = false;
		this.godMode = false;
		this.prevGodMode = this.godMode;
	}

	@Override
	public void tick() {
		super.tick();

		if (!this.world.isClient()) {
			DroneEntityS2C.send(this);

			if (!this.godMode && (
					this.world.isRaining() ||
					this.world.getBlockState(this.getBlockPos()).getBlock() == Blocks.WATER ||
					this.world.getBlockState(this.getBlockPos()).getBlock() == Blocks.BUBBLE_COLUMN ||
					this.world.getBlockState(this.getBlockPos()).getBlock() == Blocks.LAVA ||
					this.world.getBlockState(this.getBlockPos()).getBlock() == Blocks.CAMPFIRE ||
					this.world.getBlockState(this.getBlockPos()).getBlock() == Blocks.SOUL_CAMPFIRE ||
					this.world.getBlockState(this.getBlockPos()).getBlock() == Blocks.SOUL_FIRE ||
					this.world.getBlockState(this.getBlockPos()).getBlock() == Blocks.FIRE)) {
				this.setInfiniteTracking(false);
				this.kill();
			}
		}
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void stepPhysics(float d) {
		super.stepPhysics(d);

		if(TransmitterItem.isBoundTransmitter(ClientInitializer.client.player.getMainHandStack(), this))
			axisValues.set(InputTick.axisValues);

		float deltaX = (float) BetaflightHelper.calculateRates(axisValues.currX, ClientInitializer.getConfig().getFloatOption(Config.RATE), ClientInitializer.getConfig().getFloatOption(Config.EXPO), ClientInitializer.getConfig().getFloatOption(Config.SUPER_RATE)) * d;
		float deltaY = (float) BetaflightHelper.calculateRates(axisValues.currY, ClientInitializer.getConfig().getFloatOption(Config.RATE), ClientInitializer.getConfig().getFloatOption(Config.EXPO), ClientInitializer.getConfig().getFloatOption(Config.SUPER_RATE)) * d;
		float deltaZ = (float) BetaflightHelper.calculateRates(axisValues.currZ, ClientInitializer.getConfig().getFloatOption(Config.RATE), ClientInitializer.getConfig().getFloatOption(Config.EXPO), ClientInitializer.getConfig().getFloatOption(Config.SUPER_RATE)) * d;

		rotateX(deltaX);
		rotateY(deltaY);
		rotateZ(deltaZ);

		Vec3d thrust = this.getThrustVector().multiply(this.getThrottle()).multiply(thrustNewtons);
		Vec3d yawForce = this.getThrustVector().multiply(Math.abs(deltaY));

		System.out.println(this.getHeading());
		this.yaw = this.getHeading();

		this.decreaseAngularVelocity();
		this.applyForce(
				new Vector3f((float) thrust.x, (float) thrust.y, (float) thrust.z),
				new Vector3f((float) yawForce.x, (float) yawForce.y, (float) yawForce.z)
		);
	}

	@Override
	protected void readCustomDataFromTag(CompoundTag tag) {
		setBand(tag.getInt(Config.BAND));
		setChannel(tag.getInt(Config.CHANNEL));
		setCameraAngle(tag.getInt(Config.CAMERA_ANGLE));
		setFieldOfView(tag.getInt(Config.FIELD_OF_VIEW));

		// don't retrieve noCLip or prevGodMode because they weren't written (reason in writeCustomDataToTag)
		setGodMode(tag.getInt(Config.GOD_MODE));
	}

	@Override
	protected void writeCustomDataToTag(CompoundTag tag) {
		tag.putInt(Config.BAND, getBand());
		tag.putInt(Config.CHANNEL, getChannel());
		tag.putInt(Config.CAMERA_ANGLE, getCameraAngle());
		tag.putFloat(Config.FIELD_OF_VIEW, getFieldOfView());

		// don't write noClip or prevGodMode because...
		// noClip shouldn't be preserved after a restart (your drone may fall through the world) and ...
		// prevGodMode is only used when noClip is set, keeping this value between restarts isn't required
		tag.putInt(Config.GOD_MODE, getGodMode());
	}

	public void setValue(String key, Number value) {
		switch (key) {
			case Config.BAND:
				setBand(value.intValue());
				break;
			case Config.CHANNEL:
				setChannel(value.intValue());
				break;
			case Config.CAMERA_ANGLE:
				setCameraAngle(value.intValue());
				break;
			case Config.FIELD_OF_VIEW:
				setFieldOfView(value.floatValue());
				break;
			case Config.NO_CLIP:
				setNoClip(value.intValue());
				break;
			case Config.PREV_GOD_MODE:
				setPrevGodMode(value.intValue());
				break;
			case Config.GOD_MODE:
				setGodMode(value.intValue());
				break;
			default:
				break;
		}
	}

	public Number getValue(String key) {
		switch (key) {
			case Config.BAND:
				return getBand();
			case Config.CHANNEL:
				return getChannel();
			case Config.CAMERA_ANGLE:
				return getCameraAngle();
			case Config.FIELD_OF_VIEW:
				return getFieldOfView();
			case Config.NO_CLIP:
				return getNoClip();
			case Config.PREV_GOD_MODE:
				return getPrevGodMode();
			case Config.GOD_MODE:
				return getGodMode();
			default:
				return null; // 0?
		}
	}

	public void setAxisValues(AxisValues axisValues) {
		this.axisValues.set(axisValues);
	}

	public AxisValues getAxisValues() {
		return this.axisValues;
	}

	public void setBand(int band) {
		this.band = band;
	}

	public int getBand() {
		return band;
	}

	public void setChannel(int channel) {
		this.channel = channel;
	}

	public int getChannel() {
		return channel;
	}

	public void setCameraAngle(int cameraAngle) {
		this.cameraAngle = cameraAngle;
	}

	public int getCameraAngle() {
		return cameraAngle;
	}

	public void setFieldOfView(float fieldOfView) {
		this.fieldOfView = fieldOfView;
	}

	public float getFieldOfView() {
		return fieldOfView;
	}

	public void setNoClip(int noClip) {
		this.noClip = noClip == 1;
		if (getNoClip() == 1) {
			setPrevGodMode(getGodMode());
			setGodMode(1);
		} else {
			setGodMode(getPrevGodMode());
		}
	}

	public int getNoClip() {
		return noClip ? 1 : 0;
	}

	public void setPrevGodMode(int prevGodMode) {
		this.prevGodMode = prevGodMode == 1;
	}

	public int getPrevGodMode() {
		return prevGodMode ? 1 : 0;
	}

	public void setGodMode(int godMode) {
		this.godMode = godMode == 1;
	}

	public int getGodMode() {
		return godMode ? 1 : 0;
	}

	public void decreaseAngularVelocity() {
		Vector3f angular = getRigidBody().getAngularVelocity(new Vector3f());
		Vector3f sub = new Vector3f();
		sub.x = (1-getThrottle()) * angular.x;
		sub.y = (1-getThrottle()) * angular.y;
		sub.z = (1-getThrottle()) * angular.z;
		angular.sub(sub);
		getRigidBody().setAngularVelocity(angular);
	}

	/*
	 * Essentially get the direction the bottom
	 * of the drone is facing. Returned in vector form.
	 */
	public Vec3d getThrustVector() {
		Quat4f q = getOrientation();
		QuaternionHelper.rotateX(q, 90);

		Matrix4f mat = new Matrix4f();
		Matrix4fInject.from(mat).fromQuaternion(q);

		return Matrix4fInject.from(mat).matrixToVector().multiply(-1, -1, -1);
	}

	public float getHeading() {
		Quat4f q = getOrientation();

		AxisAngle4f axis = new AxisAngle4f();
		axis.set(q);

		return (float) Math.toDegrees(axis.angle);
	}

	/*
	 * Returns the drone that is nearest to
	 * the provided entity. The provided entity is
	 * typically just the player.
	 */
	public static List<DroneEntity> getNearbyDrones(Entity entity, int x) {
		World world = entity.getEntityWorld();
		List<DroneEntity> drones = world.getEntities(
				DroneEntity.class,
				new Box(entity.getPos().getX() - x,
						entity.getPos().getY() - x,
						entity.getPos().getZ() - x,
						entity.getPos().getX() + x,
						entity.getPos().getY() + x,
						entity.getPos().getZ() + x),
				null
		);
		return drones;
	}

	/*
	 * Get a drone by it's UUID given it is nearby the
	 * provided entity.
	 */
	public static DroneEntity getByUuid(Entity entity, UUID uuid) {
		World world = entity.getEntityWorld();
		List<DroneEntity> drones = world.getEntities(
				DroneEntity.class,
				new Box(entity.getPos().getX() - 100,
						entity.getPos().getY() - 100,
						entity.getPos().getZ() - 100,
						entity.getPos().getX() + 100,
						entity.getPos().getY() + 100,
						entity.getPos().getZ() + 100),
				null
		);

		if (drones.size() > 0) {
			for (Entity e : drones) {
				if (uuid.equals(e.getUuid()) && e instanceof DroneEntity)
					return (DroneEntity) e;
			}
		}

		return null;
	}

	/*
	 * Break the drone when it's shot or hit by the player.
	 */
	@Override
	public boolean damage(DamageSource source, float amount) {
		if (source.getAttacker() instanceof PlayerEntity || (!this.godMode && source instanceof ProjectileDamageSource)) {
			this.kill();
			return true;
		}
		return false;
	}

	/*
	 * Called whenever the drone is broken. Drops drone spawner item containing nbt info
	 */
	@Override
	public void kill() {
		if (this.world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
			ItemStack itemStack = new ItemStack(ServerInitializer.DRONE_SPAWNER_ITEM);
			DroneSpawnerItem.prepDestroyedDrone(this, itemStack);

			this.dropStack(itemStack);
		}

		this.remove();
	}

	@Override
	public void remove() {
		super.remove();
	}

	/*
	 * If the player is holding a transmitter item when they right click
	 * on the drone, bind it using the drone's UUID.
	 */
	public ActionResult interact(PlayerEntity player, Hand hand) {
		if (!player.world.isClient()) {
			if (player.inventory.getMainHandStack().getItem() instanceof TransmitterItem) {
				player.inventory.getMainHandStack().getOrCreateSubTag(ServerInitializer.MODID).putUuid(Config.BIND, this.getUuid());
				player.sendMessage(new TranslatableText("Transmitter bound"), false);
			}
		} else if (!InputTick.controllerExists()) {
			player.sendMessage(new TranslatableText("Controller not found"), false);
		}

		return ActionResult.SUCCESS;
	}

	public float getThrottle() {
		return this.axisValues.currT;
	}

	public void setThrottle(float throttle) {
		this.axisValues.currT = throttle;
	}

	public void setInfiniteTracking(boolean infiniteTracking) {
		this.infiniteTracking = infiniteTracking;
	}

	public boolean hasInfiniteTracking() {
		return infiniteTracking;
	}

	public boolean isTransmitterBound(ItemStack transmitter) {
		try {
			return this.getUuid().equals(transmitter.getSubTag(Config.BIND).getUuid(Config.BIND));
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	protected void initDataTracker() {
	}

	@Override
	public boolean isGlowing() {
		return false;
	}

	@Override
	public boolean collides() {
		return true;
	}

	@Override
	public Box getCollisionBox() {
		return super.getBoundingBox();
	}

	@Override
	public Packet<?> createSpawnPacket() {
		return new EntitySpawnS2CPacket(this);
	}

	public boolean isFresh() {
		return this.fresh;
	}

	public void setNotFresh() {
		this.fresh = false;
	}

	public static DroneEntity create(UUID playerID, World world, Vec3d pos, float yaw) {
		DroneEntity d = new DroneEntity(world, pos, yaw);
		d.playerID = playerID;
		world.spawnEntity(d);
		return d;
	}
}