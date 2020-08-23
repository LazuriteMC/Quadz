package bluevista.fpvracingmod.client.physics;

import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.DbvtBroadphase;
import com.bulletphysics.collision.dispatch.CollisionConfiguration;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.StaticPlaneShape;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.linearmath.Clock;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.Transform;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.List;

public class PhysicsWorld {
    public static final float DEFAULT_GRAVITY = -9.81f;

    private final List<PhysicsEntity> physicsEntities;
    private final DiscreteDynamicsWorld dynamicsWorld;
    private final boolean isClient;
    private final Clock clock;

    public PhysicsWorld(boolean isClient) {
        this.physicsEntities = new ArrayList();
        this.clock = new Clock();
        this.isClient = isClient;

        // Create the world
        BroadphaseInterface broadphase = new DbvtBroadphase();
        CollisionConfiguration collisionConfiguration = new DefaultCollisionConfiguration();
        CollisionDispatcher dispatcher = new CollisionDispatcher(collisionConfiguration);
        SequentialImpulseConstraintSolver solver = new SequentialImpulseConstraintSolver();
        dynamicsWorld = new DiscreteDynamicsWorld(dispatcher, broadphase, solver, collisionConfiguration);
        dynamicsWorld.setGravity(new Vector3f(0, DEFAULT_GRAVITY, 0));

        // Ground
        CollisionShape groundShape = new StaticPlaneShape(new Vector3f(0, 1, 0), 1);
        DefaultMotionState groundMotionState = new DefaultMotionState(new Transform(new Matrix4f(new Quat4f(0, 0, 0, 1), new Vector3f(0, 60, 0), 1.0f)));
        RigidBodyConstructionInfo groundRigidBodyCI = new RigidBodyConstructionInfo(0, groundMotionState, groundShape, new Vector3f(0,0,0));
        RigidBody groundRigidBody = new RigidBody(groundRigidBodyCI);
        this.dynamicsWorld.addRigidBody(groundRigidBody);
    }

    public void stepWorld() {
        float d = clock.getTimeMicroseconds() / 1000000F;
        clock.reset();

        this.dynamicsWorld.stepSimulation(d, 10);
        this.physicsEntities.forEach(PhysicsEntity::tick);
    }

    public void add(PhysicsEntity physics) {
        this.dynamicsWorld.addRigidBody(physics.getRigidBody());
        this.physicsEntities.add(physics);
    }

    public void remove(PhysicsEntity physics) {
        this.dynamicsWorld.removeRigidBody(physics.getRigidBody());
        this.physicsEntities.remove(physics);
    }
}
