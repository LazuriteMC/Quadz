package bluevista.fpvracingmod.physics;

import bluevista.fpvracingmod.server.ServerInitializer;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.Transform;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.Vec3d;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

public class PhysicsEntity {
    private RigidBody body;
    private Entity entity;
    private Quat4f orientation;
    private float mass;

    public PhysicsEntity(Entity entity) {
        this.entity = entity;
        this.orientation = new Quat4f(0, 1, 0, 0);
        this.mass = 1.0F;
        this.body = createRigidBody();
//        this.body.setCollisionFlags(this.body.getCollisionFlags() | CollisionFlags.KINEMATIC_OBJECT);
//        this.body.setActivationState(CollisionObject.DISABLE_DEACTIVATION);
//        this.body.activate();
    }

    public Vec3d getPosition() {
        Vector3f v = this.body.getCenterOfMassPosition(new Vector3f());
        return new Vec3d(v.x, v.y, v.z);
    }

    public void applyForce(Vector3f v) {
        this.body.applyCentralForce(v);
    }

    public float getLinearVelocity() {
        return this.body.getLinearVelocity(new Vector3f()).length();
    }

    public void setOrientation(Quat4f orientation) {
        this.orientation = orientation;
    }

    public Quat4f getOrientation() {
        return this.orientation;
    }

    public void setMass(float mass) {
        this.mass = mass;
    }

    public float getMass() {
        return this.mass;
    }

    public RigidBody getRigidBody() {
        return this.body;
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

        DefaultMotionState motionState = new DefaultMotionState(new Transform(new Matrix4f(new Quat4f(0, 0, 0, 1), position, 1.0f)));
        RigidBodyConstructionInfo ci = new RigidBodyConstructionInfo(this.getMass(), motionState, shape, inertia);

        RigidBody body = new RigidBody(ci);
        ServerInitializer.physicsWorld.addRigidBody(body);
        return body;
    }

    public static void serializeForClient(PhysicsEntity physics, PacketByteBuf buf, boolean isControlling) {
        buf.writeBoolean(isControlling);
        if(!isControlling) {
            buf.writeFloat(physics.getOrientation().x);
            buf.writeFloat(physics.getOrientation().y);
            buf.writeFloat(physics.getOrientation().z);
            buf.writeFloat(physics.getOrientation().w);
        }

        buf.writeFloat(physics.getMass());
        buf.writeFloat(physics.getLinearVelocity());
        buf.writeFloat((float) physics.getPosition().x);
        buf.writeFloat((float) physics.getPosition().y);
        buf.writeFloat((float) physics.getPosition().z);
    }
}
