package bluevista.fpvracingmod.server.entities;

import bluevista.fpvracingmod.client.math.QuaternionHelper;
import bluevista.fpvracingmod.client.math.inject.MatrixInject;
import bluevista.fpvracingmod.network.DroneInfoToClient;
import bluevista.fpvracingmod.server.ServerInitializer;
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
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

import java.util.List;
import java.util.UUID;

public class DroneEntity extends Entity {
	private static final Vec3d G = new Vec3d(0, -0.04, 0);

	private int cameraAngle;
	private int band;
	private int channel;

	private Quaternion prevOrientation;
	private Quaternion orientation;
	private float throttle;

	public DroneEntity(EntityType<?> type, World world) {
		this(world);
	}

	public DroneEntity(World world) {
		super(ServerInitializer.DRONE_ENTITY, world);
		this.orientation = new Quaternion(0, 1, 0, 0);
		this.prevOrientation = new Quaternion(0, 1, 0 , 0);
	}

	@Override
	public void tick() {
		super.tick();

		if(!this.world.isClient())
			DroneInfoToClient.send(this);

		// Update velocity
		Vec3d d = getThrustVector().multiply(1, -1, 1).multiply(throttle);
		this.addVelocity(G, d);
		this.move(MovementType.SELF, this.getVelocity());

		System.out.println(getCameraAngle());
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

	public void setBand(int band) {
		this.band = band;
	}

	public void setChannel(int channel) {
		this.channel = channel;
	}

	/*
	 * Essentially get the direction the bottom
	 * of the drone is facing. Returned in vector form.
	 */
	public Vec3d getThrustVector() {
		Quaternion q = new Quaternion(getOrientation());
		QuaternionHelper.rotateX(q, 90);

		Matrix4f mat = new Matrix4f();
		MatrixInject.from(mat).fromQuaternion(q);

		return MatrixInject.from(mat).matrixToVector();
	}

	/*
	 * Returns the drone that is nearest to
	 * the provided entity. The provided entity is
	 * typically just the player.
	 */
	public static DroneEntity getNearestTo(Entity entity) {
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

		if (drones.size() > 0) return drones.get(0);
		else return null;
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
			if (this.world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
				ItemStack itemStack = new ItemStack(ServerInitializer.DRONE_SPAWNER_ITEM);
				itemStack.getOrCreateSubTag("frequency").putInt("band", band);
				itemStack.getOrCreateSubTag("frequency").putInt("channel", channel);
				this.dropItem(itemStack.getItem());
			}
			this.remove();
			return true;
		}

		return false;
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

	public float getCameraAngle() {
		return cameraAngle;
	}

	public void setCameraAngle(int angle) {
		cameraAngle = angle;
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

	@Override
	public Packet<?> createSpawnPacket() {
		return new EntitySpawnS2CPacket(this);
	}

	public static DroneEntity create(World world, Vec3d pos) {
		DroneEntity d = new DroneEntity(world);
		d.refreshPositionAndAngles(pos.x, pos.y, pos.z, 0, 0);

//		PlayerManager manager = ServerTick.server.getPlayerManager();
//		WorldLoader l = PlayerManagerInjection.from(manager).createWorldLoader(d);
//		manager.sendToAll(new PlayerListS2CPacket(PlayerListS2CPacket.Action.ADD_PLAYER, new ServerPlayerEntity[]{l}));

		world.spawnEntity(d);
		return d;
	}
}