package bluevista.fpvracingmod.server.entities;

import bluevista.fpvracingmod.client.input.InputTick;
import bluevista.fpvracingmod.client.math.QuaternionHelper;
import bluevista.fpvracingmod.inject.Matrix4fInject;
import bluevista.fpvracingmod.network.DroneInfoS2C;
import bluevista.fpvracingmod.network.DroneQuaternionS2C;
import bluevista.fpvracingmod.server.ServerInitializer;
import bluevista.fpvracingmod.server.items.DroneSpawnerItem;
import bluevista.fpvracingmod.server.items.TransmitterItem;
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
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

import java.util.List;
import java.util.UUID;

public class DroneEntity extends Entity {
	private boolean infiniteTracking;
	private boolean prevGodMode;
	private boolean godMode;

	// Camera/VTX Information
	private int cameraAngle;
	private int band;
	private int channel;

	// Physics Information
	private static final Vec3d G = new Vec3d(0, -0.04, 0);
	private Quaternion prevOrientation;
	private Quaternion orientation;
	private float throttle;

	public DroneEntity(EntityType<?> type, World world) {
		this(world);
	}

	public DroneEntity(World world) {
		super(ServerInitializer.DRONE_ENTITY, world);

		this.godMode = false;
		this.prevGodMode = this.godMode;
		this.noClip = false;

		this.orientation = new Quaternion(0, 1, 0, 0);
		this.prevOrientation = new Quaternion(0, 1, 0 , 0);
	}

	@Override
	public void tick() {
		super.tick();

		if(!this.world.isClient()) {

			if (!this.godMode && (
					this.isSubmergedInWater() ||
					this.isTouchingWaterOrRain() ||
					this.isWet() ||
					this.isInsideWaterOrBubbleColumn() ||
					this.isInLava() ||
					this.isOnFire())) {
				this.kill();
			}

			DroneInfoS2C.send(this);
			DroneQuaternionS2C.send(this);

			ServerWorld w = (ServerWorld) world;
			int x = MathHelper.floor(this.getX() / 15.0D);
			int z = MathHelper.floor(this.getZ() / 15.0D);

//			if (hasInfiniteTracking()) {
//				w.getChunkManager().threadedAnvilChunkStorage.getPlayersWatchingChunk(new ChunkPos(x, z), false).forEach(each -> {
//					System.out.println("KILL ME");
//				});

//				if(!w.getChunkManager().isChunkLoaded(x, z)) {
//					w.getChunkManager().addTicket(ChunkTicketType.PLAYER, new ChunkPos(x, z), 16, new ChunkPos(x, z));
//				}

//				w.getChunkManager().getChunk(x, z);
//				System.out.println("IS CHUNK LOADED?" + w.getChunkManager().isChunkLoaded(chunkX, chunkZ));
//			}
		}

		// Update velocity
		Vec3d d = getThrustVector().multiply(1, -1, 1).multiply(throttle);
		this.addVelocity(G, d);
		this.move(MovementType.SELF, this.getVelocity());
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
		Quaternion q = new Quaternion(getOrientation());
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
			for(Entity e : drones) {
				if(uuid.equals(e.getUuid()) && e instanceof DroneEntity)
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

			DroneSpawnerItem.setBand(itemStack, band);
			DroneSpawnerItem.setChannel(itemStack, channel);
			DroneSpawnerItem.setCameraAngle(itemStack, cameraAngle);
			DroneSpawnerItem.setNoClip(itemStack, noClip ? 1 : 0);
			DroneSpawnerItem.setPrevGodMode(itemStack, prevGodMode ? 1 : 0);
			DroneSpawnerItem.setGodMode(itemStack, godMode ? 1 : 0);

			this.dropStack(itemStack);
		}

		this.remove();
	}

	/*
	 * If the player is holding a transmitter item when they right click
	 * on the drone, bind it using the drone's UUID.
	 */
	public ActionResult interact(PlayerEntity player, Hand hand) {
		if(!player.world.isClient()) {
			if (player.inventory.getMainHandStack().getItem() instanceof TransmitterItem) {
				player.inventory.getMainHandStack().getOrCreateSubTag("bind").putUuid("bind", this.getUuid());
				player.sendMessage(new TranslatableText("Transmitter bound"), false);
			}
		} else if (!InputTick.controllerExists()) {
			player.sendMessage(new TranslatableText("Controller not found"), false);
		}

		return ActionResult.SUCCESS;
	}

	public Quaternion getOrientation() {
		return orientation;
	}

	public void setOrientation(Quaternion q) {
		orientation = q;
	}

	public Quaternion getPrevOrientation() {
		return prevOrientation;
	}

	public void setPrevOrientation(Quaternion q) {
		prevOrientation.set(q.getX(), q.getY(), q.getZ(), q.getW());
	}

	public float getThrottle() {
		return throttle;
	}

	public void setThrottle(float throttle) {
		this.throttle = throttle;
	}

	public void addVelocity(Vec3d... vecs) {
		for(Vec3d vec : vecs) {
			this.addVelocity(vec.x, vec.y, vec.z);
		}
	}

	public boolean isTransmitterBound(ItemStack transmitter) {
		try {
			return this.getUuid().equals(transmitter.getSubTag("bind").getUuid("bind"));
		} catch(Exception e) {
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

//	@Override
//	public Box getHardCollisionBox(Entity collidingEntity) {
//		return collidingEntity.isPushable() ? collidingEntity.getBoundingBox() : null;
//	}

	@Override
	public boolean isPushable() {
		return true;
	}

//	@Override
//	protected void fall(double heightDifference, boolean onGround, BlockState landedState, BlockPos landedPosition) {
//	}

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

	public static DroneEntity create(World world, Vec3d pos) {
		DroneEntity d = new DroneEntity(world);
		d.refreshPositionAndAngles(pos.x, pos.y, pos.z, 0, 0);
		world.spawnEntity(d);
		return d;
	}
}