package bluevista.fpvracingmod.server.entities;

import bluevista.fpvracingmod.server.ServerInitializer;
import bluevista.fpvracingmod.server.items.TransmitterItem;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.ProjectileDamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.BlockTags;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.*;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;
import java.util.UUID;

public class DroneEntity extends Entity {

	private int band;
	private int channel;
	private Quaternion orientation;

	private float throttle = 0.0f;

	public DroneEntity(World worldIn) {
		super(ServerInitializer.DRONE_ENTITY, worldIn);

		this.orientation = new Quaternion(0, 1, 0, 0);

		Random random = new Random(); // testing only
		band = random.nextInt(6) + 1; // 1 - 6
		channel = random.nextInt(8) + 1; // 1 - 8

		// TODO nbt tags - channel, camera_angle, etc.
	}

	@Override
	public void tick() {
		this.prevX = this.getX();
		this.prevY = this.getY();
		this.prevZ = this.getZ();

		super.tick();
//		System.out.println("Is Server?: " + isLogicalSideForUpdatingMovement());
//		if(isLogicalSideForUpdatingMovement()) {
			this.setVelocity(0.1, 0, 0);
//			this.updateTrackedPosition(getX(), getY(), getZ());
//			this.addVelocity(1000, 0, 0);
			this.move(MovementType.SELF, this.getVelocity());
//		}

//		System.out.println("X: " + this.getX());
//		System.out.println("Y: " + this.getY());
//		System.out.println("Z: " + this.getZ());
//		System.out.println();
//		this.setPos(getX() + getVelocity().getX(), getY() + getVelocity().getY(), getZ() + getVelocity().getZ());

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
//					}
//				}
//			}
//		}

	}

	@Override
	protected void readCustomDataFromTag(CompoundTag tag) {
		band = tag.getInt("band");
		channel = tag.getInt("channel");
	}

	@Override
	protected void writeCustomDataToTag(CompoundTag tag) {
		tag.putInt("band", band);
		tag.putInt("channel", channel);
	}

	@Override
	public Packet<?> createSpawnPacket() {
		return new EntitySpawnS2CPacket(this);
	}

	public static DroneEntity getNearestTo(Entity entity) {
		World world = entity.getEntityWorld();
		List<DroneEntity> drones = world.getEntities(
				DroneEntity.class,
				new Box(entity.getPos().getX() - 100, entity.getPos().getY() - 100, entity.getPos().getZ() - 100, entity.getPos().getX() + 100, entity.getPos().getY() + 100, entity.getPos().getZ() + 100),
				null
		);
		if (drones.size() > 0) return drones.get(0);
		else return null;
	}

	public static DroneEntity getByUuid(Entity entity, UUID uuid) {
		World world = entity.getEntityWorld();
		List<DroneEntity> drones = world.getEntities(
				DroneEntity.class,
				new Box(entity.getPos().getX() - 100, entity.getPos().getY() - 100, entity.getPos().getZ() - 100, entity.getPos().getX() + 100, entity.getPos().getY() + 100, entity.getPos().getZ() + 100),
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
//			if (this.world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS))
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
//				if(subTag.getUuid("bind").equals(this.getUuid())) {
//					player.sendMessage(new TranslatableText("Already bound"), false);
//				} else {
					subTag.putUuid("bind", this.getUuid());
					player.sendMessage(new TranslatableText("Transmitter bound"), false);
//				}
			}
		}
		return ActionResult.SUCCESS;
	}

	public void setChannel(int channel) {
		this.channel = channel;
	}

	public int getChannel() {
		return this.channel;
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

	@Override
	public Box getHardCollisionBox(Entity collidingEntity) {
		return collidingEntity.isPushable() ? collidingEntity.getBoundingBox() : null;
	}

	@Override
	public boolean isPushable() {
		return true;
	}

	@Override
	protected void fall(double heightDifference, boolean onGround, BlockState landedState, BlockPos landedPosition) {
	}
}