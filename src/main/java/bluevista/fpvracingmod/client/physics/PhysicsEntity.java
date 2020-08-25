package bluevista.fpvracingmod.client.physics;

import bluevista.fpvracingmod.client.ClientInitializer;
import bluevista.fpvracingmod.server.entities.DroneEntity;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.Transform;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

public class PhysicsEntity {
    public static final float DEFAULT_MASS = 0.750f;

    private float mass;
    private float linearDamping;

    private final Entity entity;
    private final boolean isClient;
    private RigidBody body;

    public PhysicsEntity(Entity entity) {
        this.mass = 0.750f; // Get from config
        this.linearDamping = 0.3f; // Get from config

        this.isClient = entity.world.isClient();
        this.entity = entity;
        this.body = createRigidBody();
        if(isClient) {
            ClientInitializer.physicsWorld.add(this);
        }
    }

    public void tick() {
        if(entity instanceof DroneEntity) {
            DroneEntity drone = (DroneEntity) entity;

            if(drone.getThrottle() > 0.15) {
                this.getRigidBody().setAngularVelocity(new Vector3f(0, 0, 0));
            }

            Vec3d v = drone.getThrustVector().multiply(1, -1, 1).multiply(drone.getThrottle()).multiply(64);
            this.applyForce(new Vector3f((float) v.x, (float) v.y, (float) v.z));
        }
    }

    public Entity getEntity() {
        return this.entity;
    }

    public Vector3f getPosition() {
        return this.body.getCenterOfMassPosition(new Vector3f());
    }

    public void setPosition(Vector3f v) {
        Transform trans = new Transform();
        this.body.getWorldTransform(trans);
        trans.origin.x = v.x;
        trans.origin.y = v.y;
        trans.origin.z = v.z;
        this.body.setWorldTransform(trans);
    }

    public Quat4f getOrientation() {
        return this.body.getWorldTransform(new Transform()).getRotation(new Quat4f());
    }

    public void setOrientation(Quat4f q) {
        Transform trans = this.body.getWorldTransform(new Transform());
        trans.setRotation(q);
        this.body.setWorldTransform(trans);
    }

    public void applyForce(Vector3f v) {
        this.body.applyCentralForce(v);
    }

    public float getMass() {
        return this.mass;
    }

    public RigidBody getRigidBody() {
        return this.body;
    }

    public void remove() {
        if(isClient) ClientInitializer.physicsWorld.remove(this);
    }

    public RigidBody createRigidBody() {
        Vector3f inertia = new Vector3f(0.0F, 0.0F, 0.0F);
        Vector3f box = new Vector3f(
                ((float) (entity.getCollisionBox().maxX - entity.getCollisionBox().minX) / 2.0F),
                ((float) (entity.getCollisionBox().maxY - entity.getCollisionBox().minY) / 2.0F),
                ((float) (entity.getCollisionBox().maxZ - entity.getCollisionBox().minZ) / 2.0F));
        CollisionShape shape = new BoxShape(box);
        shape.calculateLocalInertia(this.getMass(), inertia);

        Vec3d pos = entity.getPos();
        Vector3f position = new Vector3f((float) pos.x, (float) pos.y, (float) pos.z);

        DefaultMotionState motionState = new DefaultMotionState(new Transform(new Matrix4f(new Quat4f(0, 1, 0, 0), position, 1.0f)));
        RigidBodyConstructionInfo ci = new RigidBodyConstructionInfo(this.getMass(), motionState, shape, inertia);

        RigidBody body = new RigidBody(ci);
        body.setActivationState(CollisionObject.DISABLE_DEACTIVATION);
        body.setDamping(linearDamping, 0.9f);
        return body;
    }
}
