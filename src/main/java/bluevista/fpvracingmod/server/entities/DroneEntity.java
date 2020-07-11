package bluevista.fpvracingmod.server.entities;

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

	private Quaternion orientation;
	private float throttle;

	public DroneEntity(EntityType<?> type, World world) {
		this(world);
	}

	public DroneEntity(World world) {
		super(ServerInitializer.DRONE_ENTITY, world);

		this.orientation = new Quaternion(0, 1, 0, 0);

//		band = random.nextInt(6) + 1; // 1 - 6
//		channel = random.nextInt(8) + 1; // 1 - 8
//		cameraAngle = 20;
	}

	@Override
	public void tick() {
		super.tick();

		this.setVelocity(0.01*getThrottle(), 0, 0);
		this.move(MovementType.SELF, this.getVelocity());

//		if(RenderHandler.isPlayerViewingDrone()) {
//			Vector3f d = QuaternionHelper.rotationMatrixToVector(QuaternionHelper.quatToMatrix(getOrientation()));
//			this.addVelocity(d.getY() * throttle / 5, d.getX() * throttle / 5, d.getZ() * throttle / 5);
//			this.move(MoverType.SELF, this.getMotion());
//

		// Player movement things...
//			if(!world.isRemote) {
//				PlayerEntity playerSP = RenderHandler.getPlayer();
//				if (playerSP != null) this.player = (ServerPlayerEntity) world.getPlayerByUuid(playerSP.getUniqueID());
//				if (player != null) {
//					System.out.println(getDistanceSq(player));
//					if(getDistanceSq(player) >= 2500) {
//						player.connection.setPlayerLocation((float) Math.round(getPositionVec().x), 50, (float) Math.round(getPositionVec().z), player.rotationYaw, player.rotationPitch);
//						if (!player.abilities.isCreativeMode) player.abilities.allowFlying = true;

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

	@Override
	public Packet<?> createSpawnPacket() {
		return new EntitySpawnS2CPacket(this);
	}

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

	public float getThrottle() {
		return throttle;
	}

	public void setThrottle(float throttle) {
		this.throttle = throttle;
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

	@Override
	protected void fall(double heightDifference, boolean onGround, BlockState landedState, BlockPos landedPosition) {
	}
}