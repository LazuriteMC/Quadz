package bluevista.fpvracingmod.physics;

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
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.Transform;
import net.minecraft.network.PacketByteBuf;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.List;

public class PhysicsWorld {
    private List<RigidBody> rigidBodies;
    private DiscreteDynamicsWorld dynamicsWorld;

    public PhysicsWorld() {
        rigidBodies = new ArrayList();
        float gravity = -9.81f;

        // Create the world
        BroadphaseInterface broadphase = new DbvtBroadphase();
        CollisionConfiguration collisionConfiguration = new DefaultCollisionConfiguration();
        CollisionDispatcher dispatcher = new CollisionDispatcher(collisionConfiguration);
        SequentialImpulseConstraintSolver solver = new SequentialImpulseConstraintSolver();

        dynamicsWorld = new DiscreteDynamicsWorld(dispatcher, broadphase, solver, collisionConfiguration);
        dynamicsWorld.setGravity(new Vector3f(0, gravity, 0));

        // Ground
        CollisionShape groundShape = new StaticPlaneShape(new Vector3f(0, 1, 0), 1);
        DefaultMotionState groundMotionState = new DefaultMotionState(new Transform(new Matrix4f(new Quat4f(0, 0, 0, 1), new Vector3f(0, 60, 0), 1.0f)));
        RigidBodyConstructionInfo groundRigidBodyCI = new RigidBodyConstructionInfo(0, groundMotionState, groundShape, new Vector3f(0,0,0));
        RigidBody groundRigidBody = new RigidBody(groundRigidBodyCI);

        // Add rigid bodies
        this.addRigidBody(groundRigidBody);
    }

    public void stepWorld() {
        this.dynamicsWorld.stepSimulation(1/60.f, 10);

        for(RigidBody body : this.rigidBodies) {
            Transform trans = new Transform();
            body.getMotionState().getWorldTransform(trans);
            System.out.println(trans.origin);
        }
    }

    public void addRigidBody(RigidBody body) {
        this.rigidBodies.add(body);
        this.dynamicsWorld.addRigidBody(body);
    }

    public void removeRigidBody(RigidBody body) {
        this.rigidBodies.remove(body);
        this.dynamicsWorld.removeRigidBody(body);
    }

    public static void serializeRididBodies(PhysicsWorld world, PacketByteBuf buf) {
        for(RigidBody body : world.rigidBodies) {
            Transform trans = new Transform();
            body.getWorldTransform(trans);
        }
    }
}
