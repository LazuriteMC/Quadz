package bluevista.fpvracingmod.server.entities;

import bluevista.fpvracingmod.client.input.InputTick;
import bluevista.fpvracingmod.client.math.QuaternionHelper;
import bluevista.fpvracingmod.inject.Matrix4fInject;
import bluevista.fpvracingmod.network.entity.DroneEntityS2C;
import bluevista.fpvracingmod.server.ServerInitializer;
import bluevista.fpvracingmod.server.items.DroneSpawnerItem;
import bluevista.fpvracingmod.server.items.TransmitterItem;
import bluevista.fpvracingmod.physics.PhysicsEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
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

import javax.vecmath.Vector3f;
import java.util.List;
import java.util.UUID;

public class DroneEntity extends Entity {
	// Camera/VTX Information
	private boolean infiniteTracking;
	private int cameraAngle;
	private int band;
	private int channel;

	// Physics Information
	public PhysicsEntity physics;
//	private static final Vec3d G = new Vec3d(0, -0.04, 0);
	private float throttle;

	public DroneEntity(EntityType<?> type, World world) {
		this(world, Vec3d.ZERO);
	}

	public DroneEntity(World world, Vec3d pos) {
		super(ServerInitializer.DRONE_ENTITY, world);
		this.refreshPositionAndAngles(pos.x, pos.y, pos.z, 0, 0);
		this.physics = new PhysicsEntity(this);
		this.noClip = true;
	}

	@Override
	public void tick() {
		super.tick();

		if(!this.world.isClient()) {
			DroneEntityS2C.send(this);

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

		if(physics != null) {
			Vec3d pos = this.physics.getPosition();
			this.setPos(pos.x, pos.y, pos.z);

			Vec3d v = getThrustVector().multiply(1, -1, 1).multiply(throttle * 1000);
			this.physics.applyForce(new Vector3f((float) v.x, (float) v.y, (float) v.z));
		}

		// Update velocity
//		Vec3d d = getThrustVector().multiply(1, -1, 1).multiply(throttle);
//		this.addVelocity(G, d);
//		this.move(MovementType.SELF, this.getVelocity());
	}

	@Override
	protected void readCustomDataFromTag(CompoundTag tag) {
		band = tag.getInt("band");
		channel = tag.getInt("channel");
		cameraAngle = tag.getInt("cameraAngle");
	}

	@Override
	protected void writeCustomDataToTag(CompoundTag tag) {
		tag.putInt("band", band);
		tag.putInt("channel", channel);
		tag.putInt("cameraAngle", cameraAngle);
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
			default:
				break;
		}
	}

	public int getValue(String key) {
		switch (key) {
			case "band":
				return getBand();
			case "channel":
				return getChannel();
			case "cameraAngle":
				return getCameraAngle();
			default:
				return 0; // unknown key, default value
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
		if (source instanceof ProjectileDamageSource || source.getAttacker() instanceof PlayerEntity) {
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
			DroneSpawnerItem.setBand(itemStack, channel);
			DroneSpawnerItem.setCameraAngle(itemStack, cameraAngle);

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
		return QuaternionHelper.quat4fToQuaternion(this.physics.getOrientation());
	}

	public void setOrientation(Quaternion orientation) {
		this.physics.setOrientation(QuaternionHelper.quaternionToQuat4f(orientation));
	}

	public void rotateX(float deg) {
		Quaternion q = getOrientation();
		QuaternionHelper.rotateX(q, deg);
		setOrientation(q);
	}

	public void rotateY(float deg) {
		Quaternion q = getOrientation();
		QuaternionHelper.rotateY(q, deg);
		setOrientation(q);
	}

	public void rotateZ(float deg) {
		Quaternion q = getOrientation();
		QuaternionHelper.rotateZ(q, deg);
		setOrientation(q);
	}

	public float getThrottle() {
		return throttle;
	}

	public void setThrottle(float throttle) {
		this.throttle = throttle;
	}

	public PhysicsEntity getPhysics() {
		return this.physics;
	}

//	public void addVelocity(Vec3d... vecs) {
//		for(Vec3d vec : vecs) {
//			this.addVelocity(vec.x, vec.y, vec.z);
//		}
//	}

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
//	public boolean isPushable() {
//		return true;
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

	public static DroneEntity create(World world, Vec3d pos, float yaw) {
		DroneEntity d = new DroneEntity(world, pos);
		d.rotateY(yaw);
		world.spawnEntity(d);
		return d;
	}
}