package bluevista.fpvracingmod.server.entities;

import bluevista.fpvracingmod.client.ClientInitializer;
import bluevista.fpvracingmod.client.ClientTick;
import bluevista.fpvracingmod.client.input.AxisValues;
import bluevista.fpvracingmod.client.input.InputTick;
import bluevista.fpvracingmod.client.math.VectorHelper;
import bluevista.fpvracingmod.config.Config;
import bluevista.fpvracingmod.client.math.BetaflightHelper;
import bluevista.fpvracingmod.client.math.Matrix4fInject;
import bluevista.fpvracingmod.client.math.QuaternionHelper;
import bluevista.fpvracingmod.network.NetQuat4f;
import bluevista.fpvracingmod.network.entity.DroneEntityS2C;
import bluevista.fpvracingmod.network.entity.DroneEntityC2S;
import bluevista.fpvracingmod.server.ServerInitializer;
import bluevista.fpvracingmod.server.items.DroneSpawnerItem;
import bluevista.fpvracingmod.server.items.TransmitterItem;
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

public class DroneEntity extends Entity {
	public static final UUID NULL_UUID = new UUID(0, 0);
	public static final int TRACKING_RANGE = 80;
	public static final int PSEUDO_TRACKING_RANGE = TRACKING_RANGE / 2;

	/* Misc */
	private final HashMap<PlayerEntity, Vec3d> playerStartPos;
	private float damageCoefficient;
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

	/* Physics Settings */
	private float dragCoefficient;
	private float mass;
	private int size;
	private float thrust;
	private float thrustCurve;
	private int crashMomentumThreshold;

	/* Misc Physics Info */
	private RigidBody body;
	public NetQuat4f netQuat;
	public UUID playerID;
	private boolean active;

	public DroneEntity(EntityType<?> type, World world) {
		this(world, null, Vec3d.ZERO, 0);
	}

	public DroneEntity(World world, UUID playerID, Vec3d pos, float yaw) {
		super(ServerInitializer.DRONE_ENTITY, world);

		this.netQuat = new NetQuat4f(new Quat4f(0, 1, 0, 0));
		this.axisValues = new AxisValues();
		this.playerStartPos = new HashMap();

		this.godMode = false;
		this.playerID = playerID;

		this.updatePositionAndAngles(pos.x, pos.y, pos.z, yaw, 0);
		this.createRigidBody();
		this.rotateY(180f - yaw);

		if (world.isClient()) {
			ClientInitializer.physicsWorld.add(this);
		}
	}

	@Override
	public void tick() {
		super.tick();

		Vector3f pos = this.getRigidBody().getCenterOfMassPosition(new Vector3f());
		this.updatePosition(pos.x, pos.y, pos.z);

		if (this.world.isClient()) {
			this.active = ClientTick.isPlayerIDClient(playerID);

			if (active) {
				DroneEntityC2S.send(this);
			} else {
				this.netQuat.setPrev(this.getOrientation());
			}

			prevYaw = yaw;
			prevPitch = pitch;
			Quat4f cameraPitch = getOrientation();
			QuaternionHelper.rotateX(cameraPitch, -cameraAngle);
			yaw = QuaternionHelper.getYaw(getOrientation());
			pitch = QuaternionHelper.getPitch(cameraPitch);
		} else {
			DroneEntityS2C.send(this);

			if (this.world.getPlayerByUuid(playerID) == null) {
				this.kill();
			}

			this.world.getOtherEntities(this, getBoundingBox(), (entity -> true)).forEach((entity -> {
				Vector3f vec = getRigidBody().getLinearVelocity(new Vector3f());
				vec.scale(mass);
				entity.damage(DamageSource.GENERIC, vec.length() * damageCoefficient);
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

	@Environment(EnvType.CLIENT)
	public void stepPhysics(float d, float tickDelta) {
		if (isActive()) {
			if (isKillable()) calculateCrashConditions();

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
			Vector3f thrust = getThrustForce(deltaY);
			Vector3f air = getAirResistanceForce();
			applyForce(thrust, air);
		} else {
			this.setOrientation(this.netQuat.slerp(tickDelta));
		}
	}

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
			case Config.CRASH_MOMENTUM_THRESHOLD:
				this.crashMomentumThreshold = value.intValue();
				break;
			case Config.DRAG_COEFFICIENT:
				this.dragCoefficient = value.floatValue();
				break;
			default:
				break;
		}
	}

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
			case Config.CRASH_MOMENTUM_THRESHOLD:
				return this.crashMomentumThreshold;
			case Config.DRAG_COEFFICIENT:
				return this.dragCoefficient;
			default:
				return null;
		}
	}

	public void addPlayerStartPos(PlayerEntity player) {
		this.playerStartPos.put(player, player.getPos());
	}

	public void removePlayerStartPos(PlayerEntity player) {
		this.playerStartPos.remove(player);
	}

	public HashMap<PlayerEntity, Vec3d> getPlayerStartPos() {
		return this.playerStartPos;
	}

	private boolean isKillable() {
		return !(this.godMode || this.noClip);
	}

	public float getThrottle() {
		return this.axisValues.currT;
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
		tag.putInt(Config.CRASH_MOMENTUM_THRESHOLD, getConfigValues(Config.CRASH_MOMENTUM_THRESHOLD).intValue());

		tag.putUuid(Config.PLAYER_ID, this.playerID);
		tag.putFloat(Config.MASS, getConfigValues(Config.MASS).floatValue());
		tag.putInt(Config.SIZE, getConfigValues(Config.SIZE).intValue());
		tag.putFloat(Config.DRAG_COEFFICIENT, getConfigValues(Config.DRAG_COEFFICIENT).floatValue());

		// don't write noClip or prevGodMode because...
		// noClip shouldn't be preserved after a restart (your drone may fall through the world) and ...
		// prevGodMode is only used when noClip is set, keeping this value between restarts isn't required
		tag.putInt(Config.GOD_MODE, getConfigValues(Config.GOD_MODE).intValue());
	}

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
		setConfigValues(Config.CRASH_MOMENTUM_THRESHOLD, tag.getInt(Config.CRASH_MOMENTUM_THRESHOLD));

		this.playerID = tag.getUuid(Config.PLAYER_ID);
		setConfigValues(Config.MASS, tag.getFloat(Config.MASS));
		setConfigValues(Config.SIZE, tag.getInt(Config.SIZE));
		setConfigValues(Config.DRAG_COEFFICIENT, tag.getFloat(Config.DRAG_COEFFICIENT));

		// don't retrieve noClip or prevGodMode because they weren't written (reason in writeCustomDataToTag)
		setConfigValues(Config.GOD_MODE, tag.getInt(Config.GOD_MODE));
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

	@Override
	public boolean isGlowing() {
		return false;
	}

	@Override
	public boolean collides() {
		return true;
	}

	/**
	 * Break the {@link DroneEntity} when it's shot or otherwise damaged in some way.
	 * @param source
	 * @param amount
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
	 * Called whenever the {@link DroneEntity} is broken. Drops {@link DroneSpawnerItem} containing tag info.
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

	@Override
	public void remove() {
		super.remove();

		if (this.world.isClient()) {
			ClientInitializer.physicsWorld.remove(this);
		}
	}

	/**
	 * If the {@link PlayerEntity} is holding a {@link TransmitterItem} when they right
	 * click on the {@link DroneEntity}, bind it using the drone's UUID.
	 * @param player
	 * @param hand
	 * @return
	 */
	@Override
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

	public boolean isActive() {
		return this.active;
	}

	public void setRigidBody(RigidBody body) {
		this.body = body;
	}

	public RigidBody getRigidBody() {
		return this.body;
	}

	public void setOrientation(Quat4f q) {
		Transform trans = this.body.getWorldTransform(new Transform());
		trans.setRotation(q);
		this.body.setWorldTransform(trans);
	}

	public Quat4f getOrientation() {
		return this.body.getWorldTransform(new Transform()).getRotation(new Quat4f());
	}

	public void setRigidBodyPos(Vector3f vec) {
		Transform trans = this.body.getWorldTransform(new Transform());
		trans.origin.set(vec);
		this.body.setWorldTransform(trans);
	}

	public void setMass(float mass) {
		float old = this.mass;
		this.mass = mass;

		if (old != mass) {
			refreshRigidBody();
		}
	}

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

	protected Vector3f getAirResistanceForce() {
		Vector3f vec3f = getRigidBody().getLinearVelocity(new Vector3f());
		Vec3d velocity = new Vec3d(vec3f.x, vec3f.y, vec3f.z);
		float k = (ClientInitializer.physicsWorld.airDensity * dragCoefficient * (float) Math.pow(size / 16f, 2)) / 2.0f;

		Vec3d airVec3d = velocity.multiply(k).multiply(velocity.lengthSquared()).negate();
		Vector3f airResistance = new Vector3f((float) airVec3d.x, (float) airVec3d.y, (float) airVec3d.z);
		return airResistance;
	}

	protected float calculateThrustCurve() {
		return (float) (Math.pow(getThrottle(), thrustCurve));
	}

	protected Vector3f getThrustForce(float deltaY) {
		Vector3f thrust = VectorHelper.vec3dToVector3f(getThrustVector().multiply(calculateThrustCurve()).multiply(this.thrust));
		Vector3f yaw = VectorHelper.vec3dToVector3f(getThrustVector().multiply(Math.abs(deltaY)));

		Vector3f out = new Vector3f();
		out.add(thrust, yaw);
		return out;
	}

	public void applyForce(Vector3f... forces) {
		for (Vector3f force : forces) {
			getRigidBody().applyCentralForce(force);
		}
	}

	public void rotateX(float deg) {
		Quat4f quat = getOrientation();
		QuaternionHelper.rotateX(quat, deg);
		setOrientation(quat);
	}

	public void rotateY(float deg) {
		Quat4f quat = getOrientation();
		QuaternionHelper.rotateY(quat, deg);
		setOrientation(quat);
	}

	public void rotateZ(float deg) {
		Quat4f quat = getOrientation();
		QuaternionHelper.rotateZ(quat, deg);
		setOrientation(quat);
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
