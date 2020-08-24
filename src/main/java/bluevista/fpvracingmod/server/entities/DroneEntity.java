package bluevista.fpvracingmod.server.entities;

import bluevista.fpvracingmod.client.input.InputTick;
import bluevista.fpvracingmod.client.math.Matrix4fInject;
import bluevista.fpvracingmod.client.math.QuaternionHelper;
import bluevista.fpvracingmod.network.entity.DroneEntityC2S;
import bluevista.fpvracingmod.network.entity.DroneEntityS2C;
import bluevista.fpvracingmod.client.physics.PhysicsEntity;
import bluevista.fpvracingmod.server.ServerInitializer;
import bluevista.fpvracingmod.server.items.DroneSpawnerItem;
import bluevista.fpvracingmod.server.items.TransmitterItem;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
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

public class DroneEntity extends Entity {
	private boolean infiniteTracking;
	private boolean prevGodMode;
	private boolean godMode;

	private int cameraAngle;
	private int band;
	private int channel;
	private float throttle;

	public PhysicsEntity physics;

	public DroneEntity(EntityType<?> type, World world) {
		this(world, Vec3d.ZERO);
	}

	public DroneEntity(World world, Vec3d pos) {
		super(ServerInitializer.DRONE_ENTITY, world);
		this.setPos(pos.x, pos.y, pos.z);
		this.physics = new PhysicsEntity(this);

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

		} else {
			DroneEntityC2S.send(this);

			Vector3f pos = this.physics.getPosition();
			this.setPos(pos.x, pos.y, pos.z);
		}

		this.move(MovementType.SELF, new Vec3d(0, 0, 0));
	}

	@Override
	protected void readCustomDataFromTag(CompoundTag tag) {
		band = tag.getInt("band");
		channel = tag.getInt("channel");
		cameraAngle = tag.getInt("cameraAngle");
		godMode = tag.getInt("godMode") == 1;
	}

	@Override
	protected void writeCustomDataToTag(CompoundTag tag) {
		tag.putInt("band", band);
		tag.putInt("channel", channel);
		tag.putInt("cameraAngle", cameraAngle);
		tag.putInt("godMode", godMode ? 1 : 0);
	}

	public void setValue(String key, Number value) {
		switch (key) {
			case "band":
				setBand(value.intValue());
				break;
			case "channel":
				setChannel(value.intValue());
				break;
			case "cameraAngle":
				setCameraAngle(value.intValue());
				break;
			case "noClip":
				setNoClip(value.intValue());
				break;
			case "prevGodMode":
				setPrevGodMode(value.intValue());
				break;
			case "godMode":
				setGodMode(value.intValue());
				break;
			default:
				break;
		}
	}

	public Number getValue(String key) {
		switch (key) {
			case "band":
				return getBand();
			case "channel":
				return getChannel();
			case "cameraAngle":
				return getCameraAngle();
			case "noClip":
				return getNoClip();
			case "prevGodMode":
				return getPrevGodMode();
			case "godMode":
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

	public void setCameraAngle(int angle) {
		cameraAngle = angle;
	}

	public int getCameraAngle() {
		return cameraAngle;
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

			DroneSpawnerItem.setBand(itemStack, getBand());
			DroneSpawnerItem.setChannel(itemStack, getChannel());
			DroneSpawnerItem.setCameraAngle(itemStack, getCameraAngle());
			DroneSpawnerItem.setNoClip(itemStack, getNoClip());
			DroneSpawnerItem.setPrevGodMode(itemStack, getPrevGodMode());
			DroneSpawnerItem.setGodMode(itemStack, getGodMode());

			this.dropStack(itemStack);
		}

		this.remove();
	}

	@Override
	public void remove() {
		super.remove();
		this.physics.remove();
	}

	/*
	 * If the player is holding a transmitter item when they right click
	 * on the drone, bind it using the drone's UUID.
	 */
	public ActionResult interact(PlayerEntity player, Hand hand) {
		if (!player.world.isClient()) {
			if (player.inventory.getMainHandStack().getItem() instanceof TransmitterItem) {
				player.inventory.getMainHandStack().getOrCreateSubTag("bind").putUuid("bind", this.getUuid());
				player.sendMessage(new TranslatableText("Transmitter bound"), false);
			}
		} else if (!InputTick.controllerExists()) {
			player.sendMessage(new TranslatableText("Controller not found"), false);
		}

		return ActionResult.SUCCESS;
	}

	public Quat4f getOrientation() {
		return this.physics.getOrientation();
	}

	public void setOrientation(Quat4f orientation) {
		this.physics.setOrientation(orientation);
	}

	public float getThrottle() {
		return throttle;
	}

	public void setThrottle(float throttle) {
		this.throttle = throttle;
	}

//	public void addVelocity(Vec3d... vecs) {
//		for(Vec3d vec : vecs) {
//			this.addVelocity(vec.x, vec.y, vec.z);
//		}
//	}

	public boolean isTransmitterBound(ItemStack transmitter) {
		try {
			return this.getUuid().equals(transmitter.getSubTag("bind").getUuid("bind"));
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

	public void setInfiniteTracking(boolean infiniteTracking) {
		this.infiniteTracking = infiniteTracking;
	}

	public boolean hasInfiniteTracking() {
		return infiniteTracking;
	}

	@Override
	public Packet<?> createSpawnPacket() {
		return new EntitySpawnS2CPacket(this);
	}

	public static DroneEntity create(World world, Vec3d pos, float yaw) {
		DroneEntity d = new DroneEntity(world, pos);

		Quat4f q = d.getOrientation();
		QuaternionHelper.rotateY(q, yaw);
		d.setOrientation(q);

		world.spawnEntity(d);
		return d;
	}
}