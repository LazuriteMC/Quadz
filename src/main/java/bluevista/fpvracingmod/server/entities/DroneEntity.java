package bluevista.fpvracingmod.server.entities;

import bluevista.fpvracingmod.client.input.InputTick;
import bluevista.fpvracingmod.client.math.Matrix4fInject;
import bluevista.fpvracingmod.client.math.QuaternionHelper;
import bluevista.fpvracingmod.config.Config;
import bluevista.fpvracingmod.network.entity.DroneEntityS2C;
import bluevista.fpvracingmod.server.ServerInitializer;
import bluevista.fpvracingmod.server.items.DroneSpawnerItem;
import bluevista.fpvracingmod.server.items.TransmitterItem;
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

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.util.List;
import java.util.UUID;

public class DroneEntity extends PhysicsEntity {
	private boolean infiniteTracking;
	private boolean prevGodMode;
	private boolean godMode;

	private int cameraAngle;
	private float fieldOfView;
	private int band;
	private int channel;
	private float throttle;

	public DroneEntity(EntityType<?> type, World world) {
		this(world, Vec3d.ZERO, 0);
	}

	public DroneEntity(World world, Vec3d pos, float yaw) {
		super(ServerInitializer.DRONE_ENTITY, world);
		this.setRigidBodyPos(new Vector3f((float) pos.x, (float) pos.y, (float) pos.z));
		this.setPos(pos.x, pos.y, pos.z);
		this.rotateY(yaw);

		this.noClip = false;
		this.godMode = false;
		this.prevGodMode = this.godMode;

		this.playerID = new UUID(0, 0);
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

	public void stepPhysics() {
		if(this.getThrottle() > 0.15) {
			this.getRigidBody().setAngularVelocity(new Vector3f(0, 0, 0));
		}

		Vec3d v = this.getThrustVector().multiply(1, -1, 1).multiply(this.getThrottle()).multiply(thrustNewtons);
		this.applyForce(new Vector3f((float) v.x, (float) v.y, (float) v.z));
	}

	@Override
	protected void readCustomDataFromTag(CompoundTag tag) {
		band = tag.getInt(Config.BAND);
		channel = tag.getInt(Config.CHANNEL);
		cameraAngle = tag.getInt(Config.CAMERA_ANGLE);
		fieldOfView = tag.getFloat(Config.FIELD_OF_VIEW);
		godMode = tag.getInt(Config.GOD_MODE) == 1;
	}

	@Override
	protected void writeCustomDataToTag(CompoundTag tag) {
		tag.putInt(Config.BAND, getBand());
		tag.putInt(Config.CHANNEL, getChannel());
		tag.putInt(Config.CAMERA_ANGLE, getCameraAngle());
		tag.putFloat(Config.FIELD_OF_VIEW, getFieldOfView());
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

	/*
	 * Essentially get the direction the bottom
	 * of the drone is facing. Returned in vector form.
	 */
	public Vec3d getThrustVector() {
		Quat4f q = new Quat4f(getOrientation());
		QuaternionHelper.rotateX(q, 90);

		Matrix4f mat = new Matrix4f();
		Matrix4fInject.from(mat).fromQuaternion(q);

		return Matrix4fInject.from(mat).matrixToVector();
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
				player.inventory.getMainHandStack().getOrCreateSubTag(Config.BIND).putUuid(Config.BIND, this.getUuid());
				player.sendMessage(new TranslatableText("Transmitter bound"), false);
			}
		} else if (!InputTick.controllerExists()) {
			player.sendMessage(new TranslatableText("Controller not found"), false);
		}

		return ActionResult.SUCCESS;
	}

	public float getThrottle() {
		return throttle;
	}

	public void setThrottle(float throttle) {
		this.throttle = throttle;
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

	public static DroneEntity create(UUID playerID, World world, Vec3d pos, float yaw) {
		DroneEntity d = new DroneEntity(world, pos, yaw);
		d.playerID = playerID;
		world.spawnEntity(d);
		return d;
	}
}