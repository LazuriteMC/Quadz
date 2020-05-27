package bluevista.fpvracingmod.server.entities;

import bluevista.fpvracingmod.client.math.QuaternionHelper;
import com.bluevista.fpvracing.client.handler.RenderHandler;
import com.bluevista.fpvracing.client.math.QuaternionHelper;
import com.bluevista.fpvracing.server.EntityRegistry;
import net.minecraft.client.renderer.Quaternion;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;

import java.util.List;

public class DroneEntity extends Entity {

//	private CompoundNBT properties;
	private Quaternion orientation;
//	private ServerPlayerEntity player;

	private float throttle = 0.0f;

	public DroneEntity(EntityType<?> entityTypeIn, World worldIn) {
		super(entityTypeIn, worldIn);
		orientation = QuaternionHelper.rotateX(new Quaternion(0.0f, 1.0f, 0.0f, 0.0f), 0);
//		properties = new CompoundNBT();
//		properties.putInt("channel", 0);
		// TODO nbt tags - channel, camera_angle, etc.

		this.setNoGravity(true);
		this.setMotion(Vec3d.ZERO);
	}

	public DroneEntity(FMLPlayMessages.SpawnEntity packet, World worldIn) {
		this(com.bluevista.fpvracing.server.EntityRegistry.DRONE.get(), worldIn);
	}

	public void tick() {
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		super.tick();

		if(RenderHandler.isPlayerViewingDrone()) {
			Vector3f d = QuaternionHelper.rotationMatrixToVector(QuaternionHelper.quatToMatrix(getOrientation()));
			this.addVelocity(d.getY() * throttle / 5, d.getX() * throttle / 5, d.getZ() * throttle / 5);
			this.move(MoverType.SELF, this.getMotion());


			// Player movement things...
			if(!world.isRemote) {
				PlayerEntity playerSP = RenderHandler.getPlayer();
				if (playerSP != null) this.player = (ServerPlayerEntity) world.getPlayerByUuid(playerSP.getUniqueID());
				if (player != null) {
					System.out.println(getDistanceSq(player));
					if(getDistanceSq(player) >= 2500) {
						player.connection.setPlayerLocation((float) Math.round(getPositionVec().x), 50, (float) Math.round(getPositionVec().z), player.rotationYaw, player.rotationPitch);
						if (!player.abilities.isCreativeMode) player.abilities.allowFlying = true;
					}
				}
			}
		}

	}

	public void setChannel(int channel) {
		this.properties.putInt("channel", channel);
	}

	public int getChannel() {
		return this.properties.getInt("channel");
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
	public boolean processInitialInteract(PlayerEntity player, Hand hand) {
		return true;
	}

    @Override
    public boolean canBePushed() {
        return true;
    }
	
	@Override
	protected void registerData() { }

	@Override
	protected void readAdditional(CompoundNBT compound) { }

	@Override
	protected void writeAdditional(CompoundNBT compound) { }

	@Override
	public IPacket<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	public EntityType<?> getType() {
		return com.bluevista.fpvracing.server.EntityRegistry.DRONE.get();
	}

	public static DroneEntity getNearestTo(Entity entity) {
		World world = entity.getEntityWorld();
		List<DroneEntity> drones = world.getEntitiesWithinAABB(DroneEntity.class,
				new AxisAlignedBB(entity.getPosition().getX()-100, entity.getPosition().getY()-100, entity.getPosition().getZ()-100,
						entity.getPosition().getX()+100, entity.getPosition().getY()+100, entity.getPosition().getZ()+100));
		if(drones.size() > 0) return drones.get(0);
		else return null;
	}
}