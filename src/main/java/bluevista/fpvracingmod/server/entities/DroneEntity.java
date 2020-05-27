package bluevista.fpvracingmod.server.entities;

import bluevista.fpvracingmod.FPVRacingMod;
import bluevista.fpvracingmod.client.math.Quaternion;
import bluevista.fpvracingmod.client.math.QuaternionHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.List;
import java.util.function.Predicate;

public class DroneEntity extends Entity {

//	private CompoundNBT properties;
	private Quaternion orientation;
//	private ServerPlayerEntity player;

	private float throttle = 0.0f;

	public DroneEntity(World worldIn) {
//		super(FPVRacingMod.DRONE_ENTITY, worldIn);
		super(EntityType.PLAYER, worldIn);
		orientation = QuaternionHelper.rotateX(new Quaternion(0.0f, 1.0f, 0.0f, 0.0f), 0);
//		properties = new CompoundNBT();
//		properties.putInt("channel", 0);
		// TODO nbt tags - channel, camera_angle, etc.

//		this.setNoGravity(true);
//		this.setMotion(Vec3d.ZERO);
	}

	public void tick() {
//		this.prevPosX = this.posX;
//		this.prevPosY = this.posY;
//		this.prevPosZ = this.posZ;
		super.tick();

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

	}

	@Override
	protected void writeCustomDataToTag(CompoundTag tag) {

	}

	@Override
	public Packet<?> createSpawnPacket() {
		return new EntitySpawnS2CPacket(this);
	}

//	public void setChannel(int channel) {
//		this.properties.putInt("channel", channel);
//	}

//	public int getChannel() {
//		return this.properties.getInt("channel");
//	}

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
	protected void initDataTracker() { }

	public static DroneEntity getNearestTo(Entity entity) {
		World world = entity.getEntityWorld();
		List<DroneEntity> drones = world.getEntities(
				DroneEntity.class,
				new Box(entity.getPos().getX() - 100, entity.getPos().getY() - 100, entity.getPos().getZ() - 100, entity.getPos().getX() + 100, entity.getPos().getY() + 100, entity.getPos().getZ() + 100),
				null
		);
		if(drones.size() > 0) return drones.get(0);
		else return null;
	}
}