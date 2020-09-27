package bluevista.fpvracing.server.entities;

import bluevista.fpvracing.client.ClientInitializer;
import bluevista.fpvracing.client.ClientTick;
import bluevista.fpvracing.client.input.AxisValues;
import bluevista.fpvracing.client.input.InputTick;
import bluevista.fpvracing.client.math.VectorHelper;
import bluevista.fpvracing.config.Config;
import bluevista.fpvracing.client.math.BetaflightHelper;
import bluevista.fpvracing.client.math.Matrix4fInject;
import bluevista.fpvracing.client.math.QuaternionHelper;
import bluevista.fpvracing.network.entity.DroneEntityS2C;
import bluevista.fpvracing.network.entity.DroneEntityC2S;
import bluevista.fpvracing.server.ServerInitializer;
import bluevista.fpvracing.server.items.DroneSpawnerItem;
import bluevista.fpvracing.server.items.TransmitterItem;
import com.bulletphysics.collision.broadphase.Dispatcher;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.narrowphase.PersistentManifold;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.Transform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
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

/**
 * This class does most of the work in this mod. Not only does it store all of the physics
 * information necessary, but it also handles the update logic for both physics and minecraft entities.
 * @author Ethan Johnson
 * @author Patrick Hofmann
 */
public class DroneEntity extends Entity {
	public static final int TRACKING_RANGE = 80;
	public static final int PSEUDO_TRACKING_RANGE = TRACKING_RANGE / 2;

	/* Misc */
	private final HashMap<PlayerEntity, Vec3d> playerStartPos;
	private float damageCoefficient;

	/* Camera Settings */
	private int cameraAngle;
	private float fieldOfView;
	private int band;
	private int channel;

	/* Controller Settings */
	private final AxisValues axisValues;
	private float rate;
	private float superRate;
	private float expo;

	/* Physics Settings */
	private float dragCoefficient;
	private float mass;
	private int size;
	private float thrust;
	private float thrustCurve;
	private int crashMomentumThreshold;

	/* Misc Physics Info */
	private Quat4f prevOrientation;
	private Quat4f netOrientation;
	private int playerID;
	private int bindID;
	private RigidBody body;
	private boolean godMode;

	/**
	 * The constructor called by the Fabric API in {@link ServerInitializer}. Invokes the main constructor.
	 * @param type the {@link EntityType}
	 * @param world the {@link World} that the {@link DroneEntity} will be spawned in
	 */
	public DroneEntity(EntityType<?> type, World world) {
		this(world, 0, Vec3d.ZERO, 0);
	}

	/**
	 * The main constructor for {@link DroneEntity}.
	 * @param world the {@link World} the {@link DroneEntity} will be spawned in
	 * @param playerID the {@link UUID} of the {@link PlayerEntity} who spawned it
	 * @param pos the position where the {@link DroneEntity} will be spawned
	 * @param yaw the yaw of the {@link DroneEntity}
	 */
	public DroneEntity(World world, int playerID, Vec3d pos, float yaw) {
		super(ServerInitializer.DRONE_ENTITY, world);

		this.prevOrientation = new Quat4f(0, 1, 0, 0);
		this.netOrientation = new Quat4f(0, 1, 0, 0);
		this.axisValues = new AxisValues();
		this.playerStartPos = new HashMap();

		this.ignoreCameraFrustum = true;
		this.godMode = false;
		this.playerID = playerID;
		this.bindID = -1;

		this.yaw = yaw;
		this.prevYaw = yaw;

		this.updatePositionAndAngles(pos.x, pos.y, pos.z, yaw, 0);
		this.createRigidBody();
		this.rotateY(180f - yaw);

		if (world.isClient()) {
			ClientInitializer.physicsWorld.add(this);
		}
	}

	/**
	 * Called every tick, this method handles several pieces of logic.
	 * Packets are also sent from both the client and the server within this method.
	 */
	@Override
	public void tick() {
		super.tick();

		// Update the position of the drone based on the rigid body's position
		Vector3f pos = this.getRigidBody().getCenterOfMassPosition(new Vector3f());
		this.updatePosition(pos.x, pos.y, pos.z);

		if (this.world.isClient()) {
			updateYawAndPitch();

			if (isActive()) {
				DroneEntityC2S.send(this);
			} else {
				setPrevOrientation(getOrientation());
				setOrientation(netOrientation);
			}
		} else {
			DroneEntityS2C.send(this);

			if (this.world.getEntityById(playerID) == null) {
				this.kill();
			}

			this.world.getOtherEntities(this, getBoundingBox(), (entity -> true)).forEach((entity -> {
				if (entity instanceof LivingEntity) {
					Vector3f vec = getRigidBody().getLinearVelocity(new Vector3f());
					vec.scale(mass);
					entity.damage(DamageSource.GENERIC, vec.length() * damageCoefficient);
				}
			}));

			if (isKillable() && (
					this.world.hasRain(getBlockPos()) ||
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

	/**
	 * Called every frame, this method calculates forces and modifies the orientation of the {@link DroneEntity}.
	 * @param d delta time
	 */
	@Environment(EnvType.CLIENT)
	public void stepPhysics(float d) {
		if (isActive()) {
//			if (isKillable()) calculateCrashConditions();

			if (TransmitterItem.isBoundTransmitter(ClientInitializer.client.player.getMainHandStack(), this)) {
				this.axisValues.set(InputTick.axisValues);
			}

			float deltaX = (float) BetaflightHelper.calculateRates(axisValues.currX, rate, expo, superRate, d);
			float deltaY = (float) BetaflightHelper.calculateRates(axisValues.currY, rate, expo, superRate, d);
			float deltaZ = (float) BetaflightHelper.calculateRates(axisValues.currZ, rate, expo, superRate, d);

			rotateX(deltaX);
			rotateY(deltaY);
			rotateZ(deltaZ);

			decreaseAngularVelocity();
			Vector3f thrust = getThrustForce();
			Vector3f air = getAirResistanceForce();
			applyForce(thrust, air);
		}
	}

	/**
	 * The main config value setter for the {@link DroneEntity}.
	 * @param key the {@link Config} key to set
	 * @param value the {@link Number} value to set
	 */
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
			case Config.BIND:
				this.bindID = value.intValue();
				break;
			case Config.PLAYER_ID:
				this.playerID = value.intValue();
				break;
			case Config.DAMAGE_COEFFICIENT:
				this.damageCoefficient = value.floatValue();
				break;
			case Config.THRUST:
				this.thrust = value.floatValue();
				break;
			case Config.THRUST_CURVE:
				this.thrustCurve = value.floatValue();
				break;
			case Config.MASS:
				this.setMass(value.floatValue());
				break;
			case Config.SIZE:
				this.setSize(value.intValue());
				break;
//			case Config.CRASH_MOMENTUM_THRESHOLD:
//				this.crashMomentumThreshold = value.intValue();
//				break;
			case Config.DRAG_COEFFICIENT:
				this.dragCoefficient = value.floatValue();
				break;
			default:
				break;
		}
	}

	/**
	 * The main config value getter for the {@link DroneEntity}.
	 * @param key the {@link Config} key to get
	 * @return the {@link Number} value based off of the {@link Config} key
	 */
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
			case Config.BIND:
				return this.bindID;
			case Config.PLAYER_ID:
				return this.playerID;
			case Config.DAMAGE_COEFFICIENT:
				return this.damageCoefficient;
			case Config.THRUST:
				return this.thrust;
			case Config.THRUST_CURVE:
				return this.thrustCurve;
			case Config.MASS:
				return this.mass;
			case Config.SIZE:
				return this.size;
//			case Config.CRASH_MOMENTUM_THRESHOLD:
//				return this.crashMomentumThreshold;
			case Config.DRAG_COEFFICIENT:
				return this.dragCoefficient;
			default:
				return null;
		}
	}

	/**
	 * Adds a start position for the given {@link PlayerEntity}.
	 * @param player the {@link PlayerEntity} to add
	 */
	public void addPlayerStartPos(PlayerEntity player) {
		this.playerStartPos.put(player, player.getPos());
	}

	/**
	 * Removes a start position for the given {@link PlayerEntity}.
	 * @param player the {@link PlayerEntity} to remove
	 */
	public void removePlayerStartPos(PlayerEntity player) {
		this.playerStartPos.remove(player);
	}

	/**
	 * Gets the start position {@link HashMap} made up of players.
	 * @return a {@link HashMap} containing players vs their start position
	 */
	public HashMap<PlayerEntity, Vec3d> getPlayerStartPos() {
		return this.playerStartPos;
	}

	/**
	 * Finds all instances of {@link DroneEntity} within range of the given {@link Entity}.
	 * @param entity the {@link Entity} as the origin
	 * @return a {@link List} of type {@link DroneEntity}
	 */
	public static List<Entity> getDrones(Entity entity) {
		ServerWorld world = (ServerWorld) entity.getEntityWorld();
		List<Entity> drones = world.getEntitiesByType(ServerInitializer.DRONE_ENTITY, EntityPredicates.EXCEPT_SPECTATOR);
		return drones;
	}

	/**
	 * Called whenever a world is saved. Contains {@link CompoundTag} information
	 * for the {@link DroneEntity} which should persist across restarts of the game.
	 * @param tag the tag to save to
	 */
	@Override
	protected void writeCustomDataToTag(CompoundTag tag) {
		tag.putInt(Config.BAND, getConfigValues(Config.BAND).intValue());
		tag.putInt(Config.CHANNEL, getConfigValues(Config.CHANNEL).intValue());
		tag.putInt(Config.CAMERA_ANGLE, getConfigValues(Config.CAMERA_ANGLE).intValue());
		tag.putFloat(Config.FIELD_OF_VIEW, getConfigValues(Config.FIELD_OF_VIEW).floatValue());
		tag.putFloat(Config.RATE, getConfigValues(Config.RATE).floatValue());
		tag.putFloat(Config.SUPER_RATE, getConfigValues(Config.SUPER_RATE).floatValue());
		tag.putFloat(Config.EXPO, getConfigValues(Config.EXPO).floatValue());
		tag.putFloat(Config.THRUST, getConfigValues(Config.THRUST).floatValue());
		tag.putFloat(Config.THRUST_CURVE, getConfigValues(Config.THRUST_CURVE).floatValue());
		tag.putFloat(Config.DAMAGE_COEFFICIENT, getConfigValues(Config.DAMAGE_COEFFICIENT).floatValue());
//		tag.putInt(Config.CRASH_MOMENTUM_THRESHOLD, getConfigValues(Config.CRASH_MOMENTUM_THRESHOLD).intValue());

		tag.putInt(Config.PLAYER_ID, getConfigValues(Config.PLAYER_ID).intValue());
		tag.putInt(Config.BIND, getConfigValues(Config.BIND).intValue());
		tag.putFloat(Config.MASS, getConfigValues(Config.MASS).floatValue());
		tag.putInt(Config.SIZE, getConfigValues(Config.SIZE).intValue());
		tag.putFloat(Config.DRAG_COEFFICIENT, getConfigValues(Config.DRAG_COEFFICIENT).floatValue());

		// don't write noClip or prevGodMode because...
		// noClip shouldn't be preserved after a restart (your drone may fall through the world) and ...
		// prevGodMode is only used when noClip is set, keeping this value between restarts isn't required
		tag.putInt(Config.GOD_MODE, getConfigValues(Config.GOD_MODE).intValue());
	}

	/**
	 * Called whenever a world is loaded. Contains {@link CompoundTag} information
	 * for the {@link DroneEntity} which should persist across restarts of the game.
	 * @param tag the tag to load from
	 */
	@Override
	protected void readCustomDataFromTag(CompoundTag tag) {
		setConfigValues(Config.BAND, tag.getInt(Config.BAND));
		setConfigValues(Config.CHANNEL, tag.getInt(Config.CHANNEL));
		setConfigValues(Config.CAMERA_ANGLE, tag.getInt(Config.CAMERA_ANGLE));
		setConfigValues(Config.FIELD_OF_VIEW, tag.getFloat(Config.FIELD_OF_VIEW));
		setConfigValues(Config.RATE, tag.getFloat(Config.RATE));
		setConfigValues(Config.SUPER_RATE, tag.getFloat(Config.SUPER_RATE));
		setConfigValues(Config.EXPO, tag.getFloat(Config.EXPO));
		setConfigValues(Config.THRUST, tag.getFloat(Config.THRUST));
		setConfigValues(Config.THRUST_CURVE, tag.getFloat(Config.THRUST_CURVE));
		setConfigValues(Config.DAMAGE_COEFFICIENT, tag.getFloat(Config.DAMAGE_COEFFICIENT));
//		setConfigValues(Config.CRASH_MOMENTUM_THRESHOLD, tag.getInt(Config.CRASH_MOMENTUM_THRESHOLD));

		setConfigValues(Config.PLAYER_ID, tag.getInt(Config.PLAYER_ID));
		setConfigValues(Config.BIND, tag.getInt(Config.BIND));
		setConfigValues(Config.MASS, tag.getFloat(Config.MASS));
		setConfigValues(Config.SIZE, tag.getInt(Config.SIZE));
		setConfigValues(Config.DRAG_COEFFICIENT, tag.getFloat(Config.DRAG_COEFFICIENT));

		// don't retrieve noClip or prevGodMode because they weren't written (reason in writeCustomDataToTag)
		setConfigValues(Config.GOD_MODE, tag.getInt(Config.GOD_MODE));
	}

	/**
	 * Break the {@link DroneEntity} when it's shot or otherwise damaged in some way.
	 * @param source the source of the damage
	 * @param amount the amount of damage taken
	 * @return
	 */
	@Override
	public boolean damage(DamageSource source, float amount) {
		if (source.getAttacker() instanceof PlayerEntity || (isKillable() && source instanceof ProjectileDamageSource)) {
			this.kill();
			return true;
		}
		return false;
	}

	/**
	 * Called whenever the {@link DroneEntity} is killed. Drops {@link DroneSpawnerItem} containing tag info.
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

	/**
	 * If the {@link PlayerEntity} is holding a {@link TransmitterItem} when they right
	 * click on the {@link DroneEntity}, bind it using the drone's UUID.
	 * @param player the {@link PlayerEntity} who is interacting
	 * @param hand the hand of the {@link PlayerEntity}
	 * @return
	 */
	@Override
	public ActionResult interact(PlayerEntity player, Hand hand) {
		if (!player.world.isClient()) {
			if (player.inventory.getMainHandStack().getItem() instanceof TransmitterItem) {
				Random rand = new Random();

				bindID = rand.nextInt();
				playerID = player.getEntityId();

				TransmitterItem.setTagValue(player.getMainHandStack(), Config.BIND, bindID);
				player.sendMessage(new TranslatableText("Transmitter bound"), false);
			}
		} else if (!InputTick.controllerExists()) {
			player.sendMessage(new TranslatableText("Controller not found"), false);
		}

		return ActionResult.SUCCESS;
	}

	/**
	 * Whenever the {@link DroneEntity} is killed or
	 * otherwise not supposed to be there, this is called.
	 */
	@Override
	public void remove() {
		super.remove();

		if (isActive()) {
			DroneEntityC2S.send(this);
		}
	}

	/**
	 * Allows the drone to be seen from far away.
	 * @param distance the distance away from the drone
	 * @return whether or not the drone is outside of the view distance
	 */
	@Override
	@Environment(EnvType.CLIENT)
	public boolean shouldRender(double distance) {
		return distance < Math.pow(ClientInitializer.client.options.viewDistance * 16, 2);
	}

	@Override
	public Packet<?> createSpawnPacket() {
		return new EntitySpawnS2CPacket(this);
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

	/**
	 * Gets whether the {@link DroneEntity} is active. It is active when
	 * the {@link RigidBody} is in the {@link bluevista.fpvracing.client.physics.PhysicsWorld}.
	 * @return whether or not the {@link DroneEntity} is active
	 */
	public boolean isActive() {
		if (this.world.isClient()) {
			return ClientTick.isPlayerIDClient(playerID);
		}

		return false;
	}

	/**
	 * Gets whether the {@link DroneEntity} can be killed
	 * by conventional means (e.g. punched, rained on, set on fire, etc.)
	 * @return whether or not the {@link DroneEntity} is killable
	 */
	private boolean isKillable() {
		return !(this.godMode || this.noClip);
	}

	/**
	 * Gets the throttle position.
	 * @return the throttle position
	 */
	public float getThrottle() {
		return this.axisValues.currT;
	}

	/**
	 * Sets the {@link RigidBody}.
	 * @param body the new {@link RigidBody}
	 */
	public void setRigidBody(RigidBody body) {
		this.body = body;
	}

	/**
	 * Gets the {@link RigidBody}.
	 * @return the drone's current {@link RigidBody}
	 */
	public RigidBody getRigidBody() {
		return this.body;
	}

	/**
	 * Sets the orientation of the {@link RigidBody}.
	 * @param q the new orientation
	 */
	public void setOrientation(Quat4f q) {
		Transform trans = this.body.getWorldTransform(new Transform());
		trans.setRotation(q);
		this.body.setWorldTransform(trans);
	}

	/**
	 * Gets the orientation of the {@link RigidBody}.
	 * @return a new {@link Quat4f} containing orientation
	 */
	public Quat4f getOrientation() {
		return this.body.getWorldTransform(new Transform()).getRotation(new Quat4f());
	}

	/**
	 * Sets the previous orientation of the {@link DroneEntity}.
	 * @param q the new previous orientation
	 */
	public void setPrevOrientation(Quat4f q) {
		this.prevOrientation.set(q);
	}

	/**
	 * Gets the previous orientation of the {@link DroneEntity}.
	 * @return a new previous orientation
	 */
	public Quat4f getPrevOrientation() {
		Quat4f out = new Quat4f();
		out.set(prevOrientation);
		return out;
	}

	/**
	 * Sets the orientation received over the network.
	 * @param q the new net orientation
	 */
	public void setNetOrientation(Quat4f q) {
		this.netOrientation.set(q);
	}

	/**
	 * Gets the orientation received over the network.
	 * @return a new net orientation:
	 */
	public Quat4f getNetQuaternion() {
		Quat4f out = new Quat4f();
		out.set(netOrientation);
		return out;
	}

	/**
	 * Sets the position of the {@link RigidBody}.
	 * @param vec the new position
	 */
	public void setRigidBodyPos(Vector3f vec) {
		Transform trans = this.body.getWorldTransform(new Transform());
		trans.origin.set(vec);
		this.body.setWorldTransform(trans);
	}

	/**
	 * Sets the mass of the drone. Also refreshes the {@link RigidBody}.
	 * @param mass the new mass
	 */
	public void setMass(float mass) {
		float old = this.mass;
		this.mass = mass;

		if (old != mass) {
			refreshRigidBody();
		}
	}

	/**
	 * Sets the size of the drone. Also refreshes the {@link RigidBody}.
	 * @param size the new size
	 */
	public void setSize(int size) {
		int old = this.size;
		this.size = size;

		if (old != size) {
			refreshRigidBody();
		}
	}

	/**
	 * Get the direction the bottom of the
	 * drone is facing.
	 * @return {@link Vec3d} containing thrust direction
	 */
	protected Vec3d getThrustVector() {
		Quat4f q = getOrientation();
		QuaternionHelper.rotateX(q, 90);

		Matrix4f mat = new Matrix4f();
		Matrix4fInject.from(mat).fromQuaternion(QuaternionHelper.quat4fToQuaternion(q));

		return Matrix4fInject.from(mat).matrixToVector().multiply(-1, -1, -1);
	}

	/**
	 * Calculates the amount of force generated by air resistance.
	 * @return a {@link Vector3f} containing the direction and amount of force (in newtons)
	 */
	protected Vector3f getAirResistanceForce() {
		Vector3f vec3f = getRigidBody().getLinearVelocity(new Vector3f());
		Vec3d velocity = new Vec3d(vec3f.x, vec3f.y, vec3f.z);
		float k = (ClientInitializer.physicsWorld.airDensity * dragCoefficient * (float) Math.pow(size / 16f, 2)) / 2.0f;

		Vec3d airVec3d = velocity.multiply(k).multiply(velocity.lengthSquared()).negate();
		Vector3f airResistance = new Vector3f((float) airVec3d.x, (float) airVec3d.y, (float) airVec3d.z);
		return airResistance;
	}

	/**
	 * Calculates the amount of force thrust should produce based on throttle and yaw input.
	 * @return a {@link Vector3f} containing the direction and amount of force (in newtons)
	 */
	protected Vector3f getThrustForce() {
		Vector3f thrust = VectorHelper.vec3dToVector3f(getThrustVector().multiply(calculateThrustCurve()).multiply(this.thrust));
		Vector3f yaw = VectorHelper.vec3dToVector3f(getThrustVector().multiply(Math.abs(axisValues.currY * 15)));

		Vector3f out = new Vector3f();
		out.add(thrust, yaw);
		return out;
	}

	/**
	 * Calculates the thrust curve using a power between zero and one (one being perfectly linear).
	 * @return a point on the thrust curve
	 */
	protected float calculateThrustCurve() {
		return (float) (Math.pow(getThrottle(), thrustCurve));
	}

	/**
	 * Apply a list of forces. Mostly a convenience method.
	 * @param forces an array of forces to apply to the {@link RigidBody}
	 */
	public void applyForce(Vector3f... forces) {
		for (Vector3f force : forces) {
			getRigidBody().applyCentralForce(force);
		}
	}

	/**
	 * This method changes the yaw and the pitch of
	 * the drone based on it's orientation.
	 */
	public void updateYawAndPitch() {
		Quat4f cameraPitch = getOrientation();
		QuaternionHelper.rotateX(cameraPitch, -cameraAngle);
		pitch = QuaternionHelper.getPitch(cameraPitch);

		prevYaw = yaw;
		yaw = QuaternionHelper.getYaw(getOrientation());

		while(this.yaw - this.prevYaw < -180.0F) {
			this.prevYaw -= 360.0F;
		}

		while(this.yaw - this.prevYaw >= 180.0F) {
			this.prevYaw += 360.0F;
		}
	}

	/**
	 * Rotate the drone's {@link Quat4f} by the given degrees on the X axis.
	 * @param deg degrees to rotate by
	 */
	public void rotateX(float deg) {
		Quat4f quat = getOrientation();
		QuaternionHelper.rotateX(quat, deg);
		setOrientation(quat);
	}

	/**
	 * Rotate the drone's {@link Quat4f} by the given degrees on the Y axis.
	 * @param deg degrees to rotate by
	 */
	public void rotateY(float deg) {
		Quat4f quat = getOrientation();
		QuaternionHelper.rotateY(quat, deg);
		setOrientation(quat);
	}

	/**
	 * Rotate the drone's {@link Quat4f} by the given degrees on the Z axis.
	 * @param deg degrees to rotate by
	 */
	public void rotateZ(float deg) {
		Quat4f quat = getOrientation();
		QuaternionHelper.rotateZ(quat, deg);
		setOrientation(quat);
	}

	protected void calculateCrashConditions() {
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

						// kil
						if (vec.length() > this.crashMomentumThreshold) {
							kill();
						}
					}

					break;
				}
			}
		}
	}

	protected void decreaseAngularVelocity() {
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

	/**
	 * Creates a new {@link RigidBody} using the old body's information
	 * and replaces it within the {@link bluevista.fpvracing.client.physics.PhysicsWorld}.
	 */
	protected void refreshRigidBody() {
		RigidBody old = this.getRigidBody();
		this.createRigidBody();

		this.getRigidBody().setLinearVelocity(old.getLinearVelocity(new Vector3f()));
		this.getRigidBody().setAngularVelocity(old.getAngularVelocity(new Vector3f()));
		this.setRigidBodyPos(old.getCenterOfMassPosition(new Vector3f()));
		this.setOrientation(old.getOrientation(new Quat4f()));

		if (this.world.isClient()) {
			ClientInitializer.physicsWorld.removeRigidBody(old);
			ClientInitializer.physicsWorld.addRigidBody(this.getRigidBody());
		}
	}

	/**
	 * Creates a new {@link RigidBody} based off of the drone's attributes.
	 */
	protected void createRigidBody() {
		float s = size / 16.0f;
		Box cBox = new Box(-s / 2.0f, -s / 8.0f, -s / 2.0f, s / 2.0f, s / 8.0f, s / 2.0f);
		Vector3f inertia = new Vector3f(0.0F, 0.0F, 0.0F);
		Vector3f box = new Vector3f(
				((float) (cBox.maxX - cBox.minX) / 2.0F) + 0.005f,
				((float) (cBox.maxY - cBox.minY) / 2.0F) + 0.005f,
				((float) (cBox.maxZ - cBox.minZ) / 2.0F) + 0.005f);
		CollisionShape shape = new BoxShape(box);
		shape.calculateLocalInertia(this.mass, inertia);

		Vec3d pos = this.getPos();
		Vector3f position = new Vector3f((float) pos.x, (float) pos.y + 0.125f, (float) pos.z);

		DefaultMotionState motionState = new DefaultMotionState(new Transform(new javax.vecmath.Matrix4f(new Quat4f(0, 1, 0, 0), position, 1.0f)));
		RigidBodyConstructionInfo ci = new RigidBodyConstructionInfo(this.mass, motionState, shape, inertia);

		RigidBody body = new RigidBody(ci);
		body.setActivationState(CollisionObject.DISABLE_DEACTIVATION);
		setRigidBody(body);
	}
}
