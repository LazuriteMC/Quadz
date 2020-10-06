package bluevista.fpvracing.client.physics;

import bluevista.fpvracing.client.ClientInitializer;
import bluevista.fpvracing.client.ClientTick;
import bluevista.fpvracing.client.input.InputTick;
import bluevista.fpvracing.util.math.BetaflightHelper;
import bluevista.fpvracing.util.Matrix4fInject;
import bluevista.fpvracing.util.math.QuaternionHelper;
import bluevista.fpvracing.util.math.VectorHelper;
import bluevista.fpvracing.config.Config;
import bluevista.fpvracing.server.entities.DroneEntity;
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
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.*;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DronePhysics {
    private final DroneEntity drone;
    private int playerID;

    /* Physics Settings */
    public float dragCoefficient;
    public float mass;
    public int size;
    public float thrust;
    public float thrustCurve;
    public int crashMomentumThreshold;

    /* Misc Physics Info */
    private Quat4f prevOrientation;
    private Quat4f netOrientation;
    private RigidBody body;

    public DronePhysics(DroneEntity drone) {
        this.drone = drone;

        this.prevOrientation = new Quat4f(0, 1, 0, 0);
        this.netOrientation = new Quat4f(0, 1, 0, 0);
        this.playerID = -1;

        this.createRigidBody();
        this.rotateY(180f - drone.yaw);

        if (drone.world.isClient()) {
            ClientInitializer.physicsWorld.add(this);
        }
    }

    public void tick() {
        updateYawAndPitch();

        if (playerID != -1 && drone.world.getEntityById(playerID) == null) {
            drone.kill();
        }

        if (!isActive()) {
            setPrevOrientation(getOrientation());
            setOrientation(netOrientation);
        }
    }

    @Environment(EnvType.CLIENT)
    public void step(float delta) {
        if (isActive()) {
            calculateBlockDamage();

            if (!ClientTick.isServerModded || TransmitterItem.isBoundTransmitter(ClientInitializer.client.player.getMainHandStack(), drone)) {
                float deltaX = (float) BetaflightHelper.calculateRates(InputTick.axisValues.currX, drone.getConfigValues(Config.RATE).floatValue(), drone.getConfigValues(Config.EXPO).floatValue(), drone.getConfigValues(Config.SUPER_RATE).floatValue(), delta);
                float deltaY = (float) BetaflightHelper.calculateRates(InputTick.axisValues.currY, drone.getConfigValues(Config.RATE).floatValue(), drone.getConfigValues(Config.EXPO).floatValue(), drone.getConfigValues(Config.SUPER_RATE).floatValue(), delta);
                float deltaZ = (float) BetaflightHelper.calculateRates(InputTick.axisValues.currZ, drone.getConfigValues(Config.RATE).floatValue(), drone.getConfigValues(Config.EXPO).floatValue(), drone.getConfigValues(Config.SUPER_RATE).floatValue(), delta);

                rotateX(deltaX);
                rotateY(deltaY);
                rotateZ(deltaZ);

                Vector3f thrust = getThrustForce();
                Vector3f air = getAirResistanceForce();
                applyForce(thrust, air);
                decreaseAngularVelocity();
            }
        }
    }

    public void setConfigValues(String key, Number value) {
        switch (key) {
            case Config.PLAYER_ID:
                this.playerID = value.intValue();
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
            case Config.DRAG_COEFFICIENT:
                this.dragCoefficient = value.floatValue();
                break;
            default:
                break;
        }
    }

    public Number getConfigValues(String key) {
        switch (key) {
            case Config.PLAYER_ID:
                return this.playerID;
            case Config.THRUST:
                return this.thrust;
            case Config.THRUST_CURVE:
                return this.thrustCurve;
            case Config.MASS:
                return this.mass;
            case Config.SIZE:
                return this.size;
            case Config.DRAG_COEFFICIENT:
                return this.dragCoefficient;
            default:
                return null;
        }
    }

    /**
     * Gets whether the entity is active. It is active when the {@link RigidBody}
     * is in the {@link bluevista.fpvracing.client.physics.PhysicsWorld}.
     * @return whether or not the entity is active
     */
    public boolean isActive() {
        if (drone.world.isClient()) {
            return playerID == -1 || ClientTick.isPlayerIDClient(playerID);
        }

        return false;
    }

    public void setPlayerID(int playerID) {
        this.playerID = playerID;
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

    /**
     * Returns the target {@link DroneEntity}.
     * @return the target drone
     */
    public DroneEntity getDrone() {
        return this.drone;
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
     * Sets the position of the {@link RigidBody}.
     * @param vec the new position
     */
    public void setRigidBodyPos(Vector3f vec) {
        Transform trans = this.body.getWorldTransform(new Transform());
        trans.origin.set(vec);
        this.body.setWorldTransform(trans);
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
     * This method changes the yaw and the pitch of
     * the drone based on it's orientation.
     */
    public void updateYawAndPitch() {
        Quat4f cameraPitch = getOrientation();
        QuaternionHelper.rotateX(cameraPitch, -drone.getConfigValues(Config.CAMERA_ANGLE).intValue());
        drone.pitch = QuaternionHelper.getPitch(cameraPitch);

        drone.prevYaw = drone.yaw;
        drone.yaw = QuaternionHelper.getYaw(getOrientation());

        while(drone.yaw - drone.prevYaw < -180.0F) {
            drone.prevYaw -= 360.0F;
        }

        while(drone.yaw - drone.prevYaw >= 180.0F) {
            drone.prevYaw += 360.0F;
        }
    }

    /**
     * Gets the throttle position.
     * @return the throttle position
     */
    public float getThrottle() {
        return InputTick.axisValues.currT;
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
        Vector3f yaw = VectorHelper.vec3dToVector3f(getThrustVector().multiply(Math.abs(BetaflightHelper.calculateRates(InputTick.axisValues.currY, drone.getConfigValues(Config.RATE).intValue(), drone.getConfigValues(Config.EXPO).intValue(), drone.getConfigValues(Config.SUPER_RATE).intValue(), 1.0f))));

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
     * Calculates when block collisions damage the {@link DroneEntity}
     */
    private void calculateBlockDamage() {
        List<Block> damagingBlocks = Arrays.asList(
                Blocks.WATER,
                Blocks.BUBBLE_COLUMN,
                Blocks.LAVA,
                Blocks.FIRE,
                Blocks.SOUL_FIRE,
                Blocks.CAMPFIRE,
                Blocks.SOUL_CAMPFIRE,
                Blocks.CACTUS
        );

        if (drone.isKillable()) {
            // if it's raining
            if (drone.world.hasRain(drone.getBlockPos())) {
                drone.kill();
                return;
            }

            // inside collisions for all damaging blocks
            if (damagingBlocks.contains(drone.world.getBlockState(drone.getBlockPos()).getBlock())) {
                drone.kill();
                return;
            }

            // all direction collisions for certain damaging blocks
            for (Block block : this.getTouchingBlocks(Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)) {
                if (block.is(Blocks.CACTUS)) {
                    drone.kill();
                    return;
                }
            }

            // downward collisions for certain damaging blocks
            for (Block block : this.getTouchingBlocks(Direction.DOWN)) {
                if (block.is(Blocks.MAGMA_BLOCK)) {
                    drone.kill();
                    return;
                }
            }
        }
    }

    /**
     * Finds and returns a {@link Set} of {@link Block} objects that the
     * {@link DroneEntity} is touching based on the provided {@link Direction}(s)
     * @param directions the {@link Direction}s of the desired touching {@link Block}s
     * @return a list of touching {@link Block}s
     */
    private Set<Block> getTouchingBlocks(Direction... directions) {
        Dispatcher dispatcher = ClientInitializer.physicsWorld.getDynamicsWorld().getDispatcher();
        Set<Block> blocks = new HashSet<>();

        for (int manifoldNum = 0; manifoldNum < dispatcher.getNumManifolds(); ++manifoldNum) {
            PersistentManifold manifold = dispatcher.getManifoldByIndexInternal(manifoldNum);

            if (ClientInitializer.physicsWorld.collisionBlocks.containsValue((RigidBody) manifold.getBody0()) &&
                    ClientInitializer.physicsWorld.collisionBlocks.containsValue((RigidBody) manifold.getBody1())) {
                continue;
            }

            for (int contactNum = 0; contactNum < manifold.getNumContacts(); ++contactNum) {
                if (manifold.getContactPoint(contactNum).getDistance() <= 0.0f) {
                    if (getRigidBody().equals(manifold.getBody0()) || getRigidBody().equals(manifold.getBody1())) {
                        Vector3f droneRigidBodyPos = getRigidBody().equals(manifold.getBody0()) ? ((RigidBody) manifold.getBody0()).getCenterOfMassPosition(new Vector3f()) : ((RigidBody) manifold.getBody1()).getCenterOfMassPosition(new Vector3f());
                        Vector3f otherRigidBodyPos = getRigidBody().equals(manifold.getBody0()) ? ((RigidBody) manifold.getBody1()).getCenterOfMassPosition(new Vector3f()) : ((RigidBody) manifold.getBody0()).getCenterOfMassPosition(new Vector3f());

                        for (Direction direction : directions) {
                            switch (direction) {
                                case UP:
                                    if (droneRigidBodyPos.y < otherRigidBodyPos.y) {
                                        blocks.add(drone.world.getBlockState(new BlockPos(otherRigidBodyPos.x, otherRigidBodyPos.y, otherRigidBodyPos.z)).getBlock());
                                    }
                                    break;
                                case DOWN:
                                    if (droneRigidBodyPos.y > otherRigidBodyPos.y) {
                                        blocks.add(drone.world.getBlockState(new BlockPos(otherRigidBodyPos.x, otherRigidBodyPos.y, otherRigidBodyPos.z)).getBlock());
                                    }
                                    break;
                                case EAST:
                                    if (droneRigidBodyPos.x < otherRigidBodyPos.x) {
                                        blocks.add(drone.world.getBlockState(new BlockPos(otherRigidBodyPos.x, otherRigidBodyPos.y, otherRigidBodyPos.z)).getBlock());
                                    }
                                    break;
                                case WEST:
                                    if (droneRigidBodyPos.x > otherRigidBodyPos.x) {
                                        blocks.add(drone.world.getBlockState(new BlockPos(otherRigidBodyPos.x, otherRigidBodyPos.y, otherRigidBodyPos.z)).getBlock());
                                    }
                                    break;
                                case NORTH:
                                    if (droneRigidBodyPos.z < otherRigidBodyPos.z) {
                                        blocks.add(drone.world.getBlockState(new BlockPos(otherRigidBodyPos.x, otherRigidBodyPos.y, otherRigidBodyPos.z)).getBlock());
                                    }
                                    break;
                                case SOUTH:
                                    if (droneRigidBodyPos.z > otherRigidBodyPos.z) {
                                        blocks.add(drone.world.getBlockState(new BlockPos(otherRigidBodyPos.x, otherRigidBodyPos.y, otherRigidBodyPos.z)).getBlock());
                                    }
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                }
            }
        }
        return blocks;
    }

    /**
     * Calculates when the {@link DroneEntity} should crash
     */
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
                    if (getRigidBody().equals(manifold.getBody0()) || getRigidBody().equals(manifold.getBody1())) {

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
                        if (vec.length() > crashMomentumThreshold) {
                            drone.kill();
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
                dist.sub(getRigidBody().getCenterOfMassPosition(new Vector3f()));

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

            if (Math.abs(InputTick.axisValues.currX) * it > t ||
                    Math.abs(InputTick.axisValues.currY) * it > t ||
                    Math.abs(InputTick.axisValues.currZ) * it > t) {
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

        if (drone.world.isClient()) {
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

        Vec3d pos = drone.getPos();
        Vector3f position = new Vector3f((float) pos.x, (float) pos.y + 0.125f, (float) pos.z);

        DefaultMotionState motionState = new DefaultMotionState(new Transform(new javax.vecmath.Matrix4f(new Quat4f(0, 1, 0, 0), position, 1.0f)));
        RigidBodyConstructionInfo ci = new RigidBodyConstructionInfo(this.mass, motionState, shape, inertia);

        RigidBody body = new RigidBody(ci);
        body.setActivationState(CollisionObject.DISABLE_DEACTIVATION);
        setRigidBody(body);
    }
}
