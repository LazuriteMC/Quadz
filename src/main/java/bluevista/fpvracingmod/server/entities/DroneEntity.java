package bluevista.fpvracingmod.server.entities;

import bluevista.fpvracingmod.server.ServerInitializer;
import bluevista.fpvracingmod.client.math.helper.QuaternionHelper;
import bluevista.fpvracingmod.server.items.TransmitterItem;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.ProjectileDamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;
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

	private static final float gravity = -0.001F;

	public DroneEntity(World worldIn) {
		super(ServerInitializer.DRONE_ENTITY, worldIn);

		this.orientation = new Quaternion(0, 1, 0, 0);
		this.noClip = false;
		this.setNoGravity(false);

		Random random = new Random(); // testing only
		band = random.nextInt(6) + 1; // 1 - 6
		channel = random.nextInt(8) + 1; // 1 - 8

		// TODO nbt tags - channel, camera_angle, etc.
	}

	@Override
	public void tick() {
//		this.prevPosX = this.posX;
//		this.prevPosY = this.posY;
//		this.prevPosZ = this.posZ;
//		super.tick();

//		this.addVelocity(0, gravity, 0);
//		this.move(MovementType.PLAYER, this.getVelocity());
//		this.setPos(getX() + velocity.getX(), getY() + velocity.getY(), getZ() + velocity.getZ());

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
		final PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());

		buf.writeVarInt(this.getEntityId());
		buf.writeUuid(this.uuid);
		buf.writeVarInt(Registry.ENTITY_TYPE.getRawId(this.getType()));
		buf.writeDouble(this.getX());
		buf.writeDouble(this.getY());
		buf.writeDouble(this.getZ());
		buf.writeByte(MathHelper.floor(this.pitch * 256.0F / 360.0F));
		buf.writeByte(MathHelper.floor(this.yaw * 256.0F / 360.0F));

		return ServerSidePacketRegistry.INSTANCE.toPacket(new Identifier("fpvracing", "spawn_drone"), buf);
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
}