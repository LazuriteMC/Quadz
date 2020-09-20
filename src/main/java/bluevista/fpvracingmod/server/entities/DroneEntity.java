package bluevista.fpvracingmod.server.entities;

import bluevista.fpvracingmod.client.ClientInitializer;
import bluevista.fpvracingmod.client.input.AxisValues;
import bluevista.fpvracingmod.client.input.InputTick;
import bluevista.fpvracingmod.config.Config;
import bluevista.fpvracingmod.helper.BetaflightHelper;
import bluevista.fpvracingmod.helper.Matrix4fInject;
import bluevista.fpvracingmod.helper.QuaternionHelper;
import bluevista.fpvracingmod.network.entity.DroneEntityS2C;
import bluevista.fpvracingmod.network.entity.KillDroneC2S;
import bluevista.fpvracingmod.server.ServerInitializer;
import bluevista.fpvracingmod.server.items.DroneSpawnerItem;
import bluevista.fpvracingmod.server.items.TransmitterItem;
import com.bulletphysics.collision.broadphase.Dispatcher;
import com.bulletphysics.collision.narrowphase.PersistentManifold;
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
import net.minecraft.util.math.*;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.util.*;

public class DroneEntity extends PhysicsEntity {
	public static final int TRACKING_RANGE = 80;
	public static int PLAYER_HEIGHT = 100;
	public static int NEAR_TRACKING_RANGE = TRACKING_RANGE - 5;

	/* Misc */
	private final HashMap<PlayerEntity, Vec3d> playerStartPos;
	private float thrust;
	private float damageCoefficient;
	private int crashMomentumThreshold;

	/* God Mode */
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
		this(world, null, Vec3d.ZERO);
	}

	public DroneEntity(World world, UUID playerID, Vec3d pos) {
		super(ServerInitializer.DRONE_ENTITY, world, playerID, pos);

		this.axisValues = new AxisValues();
		this.playerStartPos = new HashMap();
		this.godMode = false;
		this.createRigidBody();
	}

	public static DroneEntity create(World world, UUID playerID, Vec3d pos, float yaw) {
		DroneEntity drone = new DroneEntity(world, playerID, pos);

		if (!world.isClient()) {
			drone.rotateY(yaw);
		}

		world.spawnEntity(drone);
		return drone;
	}

	/* TICKS */

	@Override
	public void tick() {
		super.tick();

		if (!this.world.isClient()) {
			DroneEntityS2C.send(this);

			PLAYER_HEIGHT = (int) this.getPos().getY() + 100;

			this.world.getOtherEntities(this, this.getBoundingBox(), (entity -> true)).forEach((entity -> {
				Vector3f vec = this.getRigidBody().getLinearVelocity(new Vector3f());
				vec.scale(this.getMass());

				entity.damage(DamageSource.GENERIC, vec.length() * damageCoefficient);
			}));

			if (isKillable() && (
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
	public void stepPhysics(float d, float tickDelta) {
		super.stepPhysics(d, tickDelta);

		if (isActive()) {
			if (TransmitterItem.isBoundTransmitter(ClientInitializer.client.player.getMainHandStack(), this)) {
				this.axisValues.set(InputTick.axisValues);
			}

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

			if (isKillable()) {
				calculateCrashConditions();
			}
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
				break;
			case Config.DAMAGE_COEFFICIENT:
				this.damageCoefficient = value.floatValue();
				break;
			case Config.CRASH_MOMENTUM_THRESHOLD:
				this.crashMomentumThreshold = value.intValue();
				break;
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
		tag.putFloat(Config.DAMAGE_COEFFICIENT, getConfigValues(Config.DAMAGE_COEFFICIENT).floatValue());
		tag.putInt(Config.CRASH_MOMENTUM_THRESHOLD, getConfigValues(Config.CRASH_MOMENTUM_THRESHOLD).intValue());

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
			case Config.DAMAGE_COEFFICIENT:
				return this.damageCoefficient;
			case Config.CRASH_MOMENTUM_THRESHOLD:
				return this.crashMomentumThreshold;
			default:
				return super.getConfigValues(key);
//				return null; // 0?
		}
	}

	private boolean isKillable() {
		return !(this.godMode || this.noClip);
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
		Matrix4fInject.from(mat).fromQuaternion(QuaternionHelper.quat4fToQuaternion(q));

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
		setConfigValues(Config.DAMAGE_COEFFICIENT, tag.getFloat(Config.DAMAGE_COEFFICIENT));
		setConfigValues(Config.CRASH_MOMENTUM_THRESHOLD, tag.getInt(Config.CRASH_MOMENTUM_THRESHOLD));

		// don't retrieve noClip or prevGodMode because they weren't written (reason in writeCustomDataToTag)
		setConfigValues(Config.GOD_MODE, tag.getInt(Config.GOD_MODE));
	}

	/* DOERS */

	private void calculateCrashConditions() {
		// drone crash stuff
		Dispatcher dispatcher = ClientInitializer.physicsWorld.getDynamicsWorld().getDispatcher();

		// manifold is a potential collision between rigid bodies
		// run through every manifold (every loaded rigid body)
		for (int manifoldNum = 0; manifoldNum < dispatcher.getNumManifolds(); ++manifoldNum) {

			// stops block-to-block collisions from continuing
			if (ClientInitializer.physicsWorld.collisionBlocks.containsValue((RigidBody) dispatcher.getManifoldByIndexInternal(manifoldNum).getBody0()) &&
					ClientInitializer.physicsWorld.collisionBlocks.containsValue((RigidBody) dispatcher.getManifoldByIndexInternal(manifoldNum).getBody1())) {
				continue;
			}

			// current manifold
			PersistentManifold manifold = dispatcher.getManifoldByIndexInternal(manifoldNum);

			// for every contact within this manifold
			for (int contactNum = 0; contactNum < manifold.getNumContacts(); ++contactNum) {

				// if the two rigid bodies are touching on this contact
				if (manifold.getContactPoint(contactNum).getDistance() <= 0.0f) {

					// if one or both of the touching rigid bodies is this drone
					if (this.getRigidBody().equals(manifold.getBody0()) || this.getRigidBody().equals(manifold.getBody1())) {

						// get the velocity of the first rigid body
						Vector3f vec0 = ((RigidBody)manifold.getBody0()).getLinearVelocity(new Vector3f());
						vec0.scale(1.0f / ((RigidBody) manifold.getBody0()).getInvMass());

						// get the velocity of the second rigid body
						Vector3f vec1 = ((RigidBody)manifold.getBody1()).getLinearVelocity(new Vector3f());
						vec1.scale(1.0f / ((RigidBody) manifold.getBody1()).getInvMass());

						Vector3f vec = new Vector3f(0, 0, 0);

						// if both of the rigid bodies have momentum
						if (!Float.isNaN(vec0.length()) && !Float.isNaN(vec1.length())) {

							// add their momentums together
							vec.add(vec0, vec1);

							// if only one of the rigid bodies has momentum (the drone)
						} else if (!Float.isNaN(vec0.length()) || !Float.isNaN(vec1.length())) {

							// use the momentum from that rigid body
							vec.set(!Float.isNaN(vec0.length()) ? vec0 : vec1);
						}

						// setup the drone to die
						if (vec.length() > this.crashMomentumThreshold) {
							KillDroneC2S.send(this);
						}
					}

					break;
				}
			}
		}
	}

	public void decreaseAngularVelocity() {
		List<RigidBody> bodies = ClientInitializer.physicsWorld.getRigidBodies();
		boolean mightCollide = false;
		float t = 0.25f;

		for (RigidBody body : bodies) {
			if (body != getRigidBody()) {
				Vector3f dist = body.getCenterOfMassPosition(new Vector3f());
				dist.sub(this.getRigidBody().getCenterOfMassPosition(new Vector3f()));

				if (dist.length() < 1.0f) {
					mightCollide = true;
					break;
				}
			}
		}

		if (!mightCollide) {
			getRigidBody().setAngularVelocity(new Vector3f(0, 0, 0));
		} else {
			float it = 1 - getThrottle();

			if (Math.abs(axisValues.currX) * it > t ||
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
		if (source.getAttacker() instanceof PlayerEntity || ((!this.godMode || !this.noClip) && source instanceof ProjectileDamageSource)) {
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
