package bluevista.fpvracing.physics;

import bluevista.fpvracing.physics.block.BlockCollisions;
import bluevista.fpvracing.client.ClientInitializer;
import bluevista.fpvracing.physics.entity.ClientEntityPhysics;
import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.DbvtBroadphase;
import com.bulletphysics.collision.dispatch.CollisionConfiguration;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.linearmath.Clock;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class PhysicsWorld {
    public static final String GRAVITY = "gravity";
    public static final String AIR_DENSITY = "airDensity";
    public static final String BLOCK_RADIUS = "blockRadius";

    private float gravity;
    private float airDensity;
    private int blockRadius;

    public final Clock clock;
    public final List<ClientEntityPhysics> entities;
    public final BlockCollisions blockCollisions;
    private final DiscreteDynamicsWorld dynamicsWorld;

    public PhysicsWorld() {
        this.entities = new ArrayList<>();
        this.blockCollisions = new BlockCollisions(this);
        this.clock = new Clock();
        this.readFromConfig();

        BroadphaseInterface broadphase = new DbvtBroadphase();
        CollisionConfiguration collisionConfiguration = new DefaultCollisionConfiguration();
        CollisionDispatcher dispatcher = new CollisionDispatcher(collisionConfiguration);
        SequentialImpulseConstraintSolver solver = new SequentialImpulseConstraintSolver();
        dynamicsWorld = new DiscreteDynamicsWorld(dispatcher, broadphase, solver, collisionConfiguration);
        dynamicsWorld.setGravity(new Vector3f(0, gravity, 0));
    }

    public void readFromConfig() {
        blockRadius = Integer.parseInt(ClientInitializer.getConfig().getProperties().getProperty(BLOCK_RADIUS));
        gravity = Float.parseFloat(ClientInitializer.getConfig().getProperties().getProperty(GRAVITY));
        airDensity = Float.parseFloat(ClientInitializer.getConfig().getProperties().getProperty(AIR_DENSITY));
        if (dynamicsWorld != null) setGravity();
    }

    public void stepWorld() {
        ClientWorld world = ClientInitializer.client.world;
        List<ClientEntityPhysics> toRemove = new ArrayList<>();

        float delta = clock.getTimeMicroseconds() / 1000000F;
        float maxSubSteps = 5.0f;
        clock.reset();

        this.entities.forEach(physics -> {
            if (physics.getEntity().removed) {
                toRemove.add(physics);
            }

            if (world != null) {
                if (physics.isActive()) {
                    physics.getEntity().step(delta);

                    /* Add the rigid body to the world if it isn't already there */
                    if (!physics.getRigidBody().isInWorld()) {
                        this.dynamicsWorld.addRigidBody(physics.getRigidBody());
                    }

                    /* Load in block collisions */
                    if (physics.getEntity().noClip == false) {
                        this.blockCollisions.load(physics.getEntity(), world);
                    }
                } else {
                    /* Remove the rigid body if it is in the world */
                    if (physics.getRigidBody().isInWorld()) {
                        this.dynamicsWorld.removeRigidBody(physics.getRigidBody());
                    }
                }
            }
        });

        this.blockCollisions.unload();
        toRemove.forEach(entities::remove);
        this.dynamicsWorld.stepSimulation(delta, (int) maxSubSteps, delta/maxSubSteps);
    }

    public void setGravity() {
        dynamicsWorld.setGravity(new Vector3f(0, gravity, 0));
    }

    public float getGravity() {
        return gravity;
    }

    public float getAirDensity() {
        return airDensity;
    }

    public int getBlockRadius() {
        return blockRadius;
    }

    public void add(ClientEntityPhysics physics) {
        this.entities.add(physics);
    }

    public void remove(ClientEntityPhysics physics) {
        this.dynamicsWorld.removeRigidBody(physics.getRigidBody());
        this.entities.remove(physics);
    }

    public void addRigidBody(RigidBody body) {
        this.dynamicsWorld.addRigidBody(body);
    }

    public void removeRigidBody(RigidBody body) {
        this.dynamicsWorld.removeRigidBody(body);
    }

    public List<RigidBody> getRigidBodies() {
        List<RigidBody> bodies = new ArrayList<>();

        entities.forEach(physics -> bodies.add(physics.getRigidBody()));
        bodies.addAll(this.blockCollisions.getRigidBodies());

        return bodies;
    }

    public DiscreteDynamicsWorld getDynamicsWorld() {
        return this.dynamicsWorld;
    }

    public BlockCollisions getBlockCollisions() {
        return this.blockCollisions;
    }
}
