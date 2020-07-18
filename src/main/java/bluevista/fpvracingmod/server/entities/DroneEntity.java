package bluevista.fpvracingmod.server.entities;

import bluevista.fpvracingmod.client.math.MatrixInjection;
import bluevista.fpvracingmod.client.math.QuaternionHelper;
import bluevista.fpvracingmod.client.network.DroneInfoPacketHandler;
import bluevista.fpvracingmod.server.ServerInitializer;
import bluevista.fpvracingmod.server.items.TransmitterItem;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.ProjectileDamageSource;
import net.minecraft.entity.player.PlayerEntity;
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
	private static float cameraAngle;

	private Quaternion orientation;
	private float throttle;

	public DroneEntity(EntityType<?> type, World world) {
		this(world);
	}

	public DroneEntity(World world) {
		super(ServerInitializer.DRONE_ENTITY, world);
		this.orientation = new Quaternion(0, 1, 0, 0);
		this.cameraAngle = 20;
	}

	@Override
	public void tick() {
		super.tick();

		// Send info to server
		if(this.world.isClient())
			DroneInfoPacketHandler.send(this.getOrientation(), this.throttle, this);

		// Update velocity
		Vec3d d = getThrustVector().multiply(1, -1, 1).multiply(throttle);
		this.addVelocity(G, d);
		this.move(MovementType.SELF, this.getVelocity());
	}

	@Override
	protected void readCustomDataFromTag(CompoundTag tag) {
//		band = tag.getInt("band");
//		channel = tag.getInt("channel");
//		cameraAngle = tag.getInt("camera_angle");
	}

	@Override
	protected void writeCustomDataToTag(CompoundTag tag) {
//		tag.putInt("band", band);
//		tag.putInt("channel", channel);
//		tag.putInt("camera_angle", cameraAngle);
	}

	/*
	 * Essentially get the direction the bottom
	 * of the drone is facing. Returned in vector form.
	 */
	public Vec3d getThrustVector() {
		Quaternion q = new Quaternion(getOrientation());
		QuaternionHelper.rotateX(q, 90);

		Matrix4f mat = new Matrix4f();
		MatrixInjection.from(mat).fromQuaternion(q);

		return MatrixInjection.from(mat).matrixToVector();
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
			if (this.world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS))
			this.dropItem(ServerInitializer.DRONE_SPAWNER_ITEM.asItem());
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
				CompoundTag subTag = player.inventory.getMainHandStack().getOrCreateSubTag("bind");
				subTag.putUuid("bind", this.getUuid());
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

	public float getCameraAngle() {
		return cameraAngle;
	}

	public float getThrottle() {
		return throttle;
	}

	public void setThrottle(float throttle) {
		this.throttle = throttle;
	}

	public static void setCameraAngle(float angle) {
		DroneEntity.cameraAngle = angle;
	}

	public void addVelocity(Vec3d... vecs) {
		for(Vec3d vec : vecs) {
			this.addVelocity(vec.x, vec.y, vec.z);
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
}