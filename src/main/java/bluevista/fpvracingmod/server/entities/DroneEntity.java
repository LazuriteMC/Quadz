package bluevista.fpvracingmod.server.entities;

import bluevista.fpvracingmod.client.ClientInitializer;
import bluevista.fpvracingmod.client.ClientTick;
import bluevista.fpvracingmod.client.input.AxisValues;
import bluevista.fpvracingmod.client.input.InputTick;
import bluevista.fpvracingmod.config.Config;
import bluevista.fpvracingmod.helper.BetaflightHelper;
import bluevista.fpvracingmod.helper.Matrix4fInject;
import bluevista.fpvracingmod.helper.QuaternionHelper;
import bluevista.fpvracingmod.network.entity.DroneEntityS2C;
import bluevista.fpvracingmod.server.ServerInitializer;
import bluevista.fpvracingmod.server.items.DroneSpawnerItem;
import bluevista.fpvracingmod.server.items.TransmitterItem;
import com.bulletphysics.dynamics.RigidBody;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.ProjectileDamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.util.*;

public class DroneEntity extends PhysicsEntity {
	public static final UUID NULL_UUID = new UUID(0, 0);
	public static final int TRACKING_RANGE = 80;
	public static final int PLAYER_HEIGHT = 200;

	public static int NEAR_TRACKING_RANGE = TRACKING_RANGE - 5;

	/* Misc */
	private final HashMap<PlayerEntity, Vec3d> playerStartPos;
	private float thrust;

	/* God Mode */
	private boolean prevGodMode;
	private boolean godMode;

	/* Video Settings */
	private int cameraAngle;
	private float fieldOfView;
	private int band;
	private int channel;

	/* Controller Settings */
	private final AxisValues axisValues;
	private float rate;
	private float superRate;
	private float expo;

	/* CONSTRUCTORS */

	public DroneEntity(EntityType<?> type, World world) {
		this(world, null, Vec3d.ZERO, 0);
	}

	public DroneEntity(World world, PlayerEntity player, Vec3d pos, float yaw) {
		super(ServerInitializer.DRONE_ENTITY, world, player != null ? player.getUuid() : NULL_UUID, pos);

		this.axisValues = new AxisValues();
		this.playerStartPos = new HashMap();

		this.godMode = false;
		this.prevGodMode = false;

		this.createRigidBody();
		this.rotateY(yaw);
	}

	public static DroneEntity create(PlayerEntity player, World world, Vec3d pos, float yaw) {
		DroneEntity d = new DroneEntity(world, player, pos, yaw);
		world.spawnEntity(d);
		return d;
	}

	/* TICKS */

	@Override
	public void tick() {
		super.tick();

		if(!this.world.isClient()) {
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
				this.kill();
			}
		}
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void stepPhysics(float d) {
		super.stepPhysics(d);

		if(isActive()) {
			this.axisValues.set(InputTick.axisValues);

			float deltaX = (float) BetaflightHelper.calculateRates(axisValues.currX, rate, expo, superRate, d);
			float deltaY = (float) BetaflightHelper.calculateRates(axisValues.currY, rate, expo, superRate, d);
			float deltaZ = (float) BetaflightHelper.calculateRates(axisValues.currZ, rate, expo, superRate, d);

			rotateX(deltaX);
			rotateY(deltaY);
			rotateZ(deltaZ);

			Vec3d thrust = this.getThrustVector().multiply(this.getThrottle()).multiply(this.thrust);
			Vec3d yawForce = this.getThrustVector().multiply(Math.abs(deltaY));

			this.prevYaw = this.yaw;
			this.prevPitch = this.pitch;
			this.yaw = QuaternionHelper.getYaw(this.getOrientation());
			this.pitch = QuaternionHelper.getPitch(this.getOrientation());

			this.decreaseAngularVelocity();
			this.applyForce(
					new Vector3f((float) thrust.x, (float) thrust.y, (float) thrust.z),
					new Vector3f((float) yawForce.x, (float) yawForce.y, (float) yawForce.z)
			);
		}
	}

	/* SETTERS */

	@Override
	public void setConfigValues(String key, Number value) {
		switch (key) {
			case Config.BAND:
				this.band = value.intValue();
				break;
			case Config.CHANNEL:
				this.channel = value.intValue();
				break;
			case Config.CAMERA_ANGLE:
				this.cameraAngle = value.intValue();
				break;
			case Config.FIELD_OF_VIEW:
				this.fieldOfView = value.floatValue();
				break;
			case Config.NO_CLIP:
				this.noClip = value.intValue() == 1;
				if (this.noClip) {
					setConfigValues(Config.PREV_GOD_MODE, getConfigValues(Config.GOD_MODE));
					setConfigValues(Config.GOD_MODE, 1);
				} else {
					setConfigValues(Config.GOD_MODE, getConfigValues(Config.PREV_GOD_MODE));
				}
				break;
			case Config.PREV_GOD_MODE:
				this.prevGodMode = value.intValue() == 1;
				break;
			case Config.GOD_MODE:
				this.godMode = value.intValue() == 1;
				break;
			case Config.RATE:
				this.rate = value.floatValue();
				break;
			case Config.SUPER_RATE:
				this.superRate = value.floatValue();
				break;
			case Config.EXPO:
				this.expo = value.floatValue();
				break;
			case Config.THRUST:
				this.thrust = value.floatValue();
			default:
				super.setConfigValues(key, value);
				break;
		}
	}

	public void setAxisValues(AxisValues axisValues) {
		this.axisValues.set(axisValues);
	}

	public void addPlayerStartPos(PlayerEntity player) {
		this.playerStartPos.put(player, player.getPos());
	}

	public void removePlayerStartPos(PlayerEntity player) {
		this.playerStartPos.remove(player);
	}

	@Override
	protected void writeCustomDataToTag(CompoundTag tag) {
		super.writeCustomDataToTag(tag);
		tag.putInt(Config.BAND, getConfigValues(Config.BAND).intValue());
		tag.putInt(Config.CHANNEL, getConfigValues(Config.CHANNEL).intValue());
		tag.putInt(Config.CAMERA_ANGLE, getConfigValues(Config.CAMERA_ANGLE).intValue());
		tag.putFloat(Config.FIELD_OF_VIEW, getConfigValues(Config.FIELD_OF_VIEW).floatValue());
		tag.putFloat(Config.RATE, getConfigValues(Config.RATE).floatValue());
		tag.putFloat(Config.SUPER_RATE, getConfigValues(Config.SUPER_RATE).floatValue());
		tag.putFloat(Config.EXPO, getConfigValues(Config.EXPO).floatValue());
		tag.putFloat(Config.THRUST, getConfigValues(Config.THRUST).floatValue());

		// don't write noClip or prevGodMode because...
		// noClip shouldn't be preserved after a restart (your drone may fall through the world) and ...
		// prevGodMode is only used when noClip is set, keeping this value between restarts isn't required
		tag.putInt(Config.GOD_MODE, getConfigValues(Config.GOD_MODE).intValue());
	}

	/* GETTERS */

	@Override
	public Number getConfigValues(String key) {
		switch (key) {
			case Config.BAND:
				return this.band;
			case Config.CHANNEL:
				return this.channel;
			case Config.CAMERA_ANGLE:
				return this.cameraAngle;
			case Config.FIELD_OF_VIEW:
				return this.fieldOfView;
			case Config.NO_CLIP:
				return this.noClip ? 1 : 0;
			case Config.PREV_GOD_MODE:
				return this.prevGodMode ? 1 : 0;
			case Config.GOD_MODE:
				return this.godMode ? 1 : 0;
			case Config.RATE:
				return this.rate;
			case Config.SUPER_RATE:
				return this.superRate;
			case Config.EXPO:
				return this.expo;
			case Config.THRUST:
				return this.thrust;
			default:
				return super.getConfigValues(key);
//				return null; // 0?
		}
	}

	public AxisValues getAxisValues() {
		return this.axisValues;
	}

	public float getThrottle() {
		return this.axisValues.currT;
	}

	public HashMap<PlayerEntity, Vec3d> getPlayerStartPos() {
		return this.playerStartPos;
	}

	@Override
	public boolean isGlowing() {
		return false;
	}

	@Override
	public boolean collides() {
		return true;
	}

	/*
	 * Essentially get the direction the bottom
	 * of the drone is facing. Returned in vector form.
	 */
	public Vec3d getThrustVector() {
		Quat4f q = getOrientation();
		QuaternionHelper.rotateX(q, 90);

		Matrix4f mat = new Matrix4f();
		Matrix4fInject.from(mat).fromQuaternion(q);

		return Matrix4fInject.from(mat).matrixToVector().multiply(-1, -1, -1);
	}

	/*
	 * Returns all drones within the entity's world.
	 * The provided entity is typically just the player.
	 */
	public static List<Entity> getNearbyDrones(Entity entity) {
		ServerWorld world = (ServerWorld) entity.getEntityWorld();
		List<Entity> drones = world.getEntitiesByType(ServerInitializer.DRONE_ENTITY, EntityPredicates.EXCEPT_SPECTATOR);
		return drones;
	}

	public static DroneEntity getByEntityID(Entity entity, Number entityID) {
		if (entityID != null) {
			return (DroneEntity) entity.getEntityWorld().getEntityById(entityID.intValue());
		}
		return null;
	}

	@Override
	protected void readCustomDataFromTag(CompoundTag tag) {
		super.readCustomDataFromTag(tag);
		setConfigValues(Config.BAND, tag.getInt(Config.BAND));
		setConfigValues(Config.CHANNEL, tag.getInt(Config.CHANNEL));
		setConfigValues(Config.CAMERA_ANGLE, tag.getInt(Config.CAMERA_ANGLE));
		setConfigValues(Config.FIELD_OF_VIEW, tag.getFloat(Config.FIELD_OF_VIEW));
		setConfigValues(Config.RATE, tag.getFloat(Config.RATE));
		setConfigValues(Config.SUPER_RATE, tag.getFloat(Config.SUPER_RATE));
		setConfigValues(Config.EXPO, tag.getFloat(Config.EXPO));
		setConfigValues(Config.THRUST, tag.getFloat(Config.THRUST));

		// don't retrieve noClip or prevGodMode because they weren't written (reason in writeCustomDataToTag)
		setConfigValues(Config.GOD_MODE, tag.getInt(Config.GOD_MODE));
	}

	/* DOERS */

	public void decreaseAngularVelocity() {
		List<RigidBody> bodies = ClientInitializer.physicsWorld.getRigidBodies();
		boolean mightCollide = false;
		float t = 0.25f;

		for(RigidBody body : bodies) {
			if(body != getRigidBody()) {
				Vector3f dist = body.getCenterOfMassPosition(new Vector3f());
				dist.sub(this.getRigidBody().getCenterOfMassPosition(new Vector3f()));

				if(dist.length() < 1.0f) {
					mightCollide = true;
					break;
				}
			}
		}

		if(!mightCollide) {
			getRigidBody().setAngularVelocity(new Vector3f(0, 0, 0));
		} else {
			float it = 1 - getThrottle();

			if(Math.abs(axisValues.currX) * it > t ||
					Math.abs(axisValues.currY) * it > t ||
					Math.abs(axisValues.currZ) * it > t) {
				getRigidBody().setAngularVelocity(new Vector3f(0, 0, 0));
			}
		}
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
			DroneSpawnerItem.prepDestroyedDrone(this, itemStack);

			this.dropStack(itemStack);
		}

		this.remove();
	}

	/*
	 * If the player is holding a transmitter item when they right click
	 * on the drone, bind it using the drone's UUID.
	 */
	public ActionResult interact(PlayerEntity player, Hand hand) {
		if (!player.world.isClient()) {
			if (player.inventory.getMainHandStack().getItem() instanceof TransmitterItem) {
				TransmitterItem.setTagValue(player.getMainHandStack(), Config.BIND, this.getEntityId());
				player.sendMessage(new TranslatableText("Transmitter bound"), false);
				this.playerID = player.getUuid();
			}
		} else if (!InputTick.controllerExists()) {
			player.sendMessage(new TranslatableText("Controller not found"), false);
		}

		return ActionResult.SUCCESS;
	}

	@Override
	public Packet<?> createSpawnPacket() {
		return new EntitySpawnS2CPacket(this);
	}

	@Override
	protected void initDataTracker() {
	}
}