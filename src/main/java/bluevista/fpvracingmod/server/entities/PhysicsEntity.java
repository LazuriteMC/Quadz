package bluevista.fpvracingmod.server.entities;

import bluevista.fpvracingmod.client.ClientInitializer;
import bluevista.fpvracingmod.client.ClientTick;
import bluevista.fpvracingmod.math.QuaternionHelper;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.Transform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.util.UUID;

public abstract class PhysicsEntity extends Entity {
    private final RigidBody body;
    public float linearDamping;
    public float thrustNewtons;
    public UUID playerID;
    public float mass;

    public Quat4f prevOrientation;
    public boolean fresh = true;

    public PhysicsEntity(EntityType type, World world, Vec3d pos) {
        super(type, world);

        this.mass = 0.750f; // Get from config
        this.linearDamping = 0.3f; // Get from config
        this.thrustNewtons = 50.0f; // Get from config

        this.setPosition(pos.x, pos.y, pos.z);

        this.body = createRigidBody(getBoundingBox());
        if(this.world.isClient())
            ClientInitializer.physicsWorld.add(this);
    }

    @Override
    public void tick() {
        super.tick();
        if(this.world.isClient()) {
            Vector3f pos = this.getRigidBodyPos();
            this.setPosition(pos.x, pos.y, pos.z);

            this.prevOrientation = this.getOrientation();
        }
    }

    @Environment(EnvType.CLIENT)
    public void stepPhysics(float d) {
        if(playerID != null) {
            if (!ClientTick.isPlayerIDClient(playerID)) {
                Vector3f vec = this.getRigidBodyPos();
                this.resetPosition2(vec.x, vec.y, vec.z);

                if (prevOrientation != null) {
                    Quat4f curOrientation = this.getOrientation();
//                    prevOrientation.set(curOrientation);
                    curOrientation.x = MathHelper.lerp(d, prevOrientation.x, curOrientation.x);
                    curOrientation.y = MathHelper.lerp(d, prevOrientation.y, curOrientation.y);
                    curOrientation.z = MathHelper.lerp(d, prevOrientation.z, curOrientation.z);
                    curOrientation.w = MathHelper.lerp(d, prevOrientation.w, curOrientation.w);
                    this.setOrientation(curOrientation);
                }
            }
        }
    }

    public void setPosition(double x, double y, double z) {
        this.setPos(x, y, z);
    }

    public void resetPosition2(double x, double y, double z) {
        this.resetPosition(x, y, z);
    }

    public Vector3f getRigidBodyPos() {
        return this.body.getCenterOfMassPosition(new Vector3f());
    }

    public void setRigidBodyPos(Vector3f vec) {
        Transform trans = this.body.getWorldTransform(new Transform());
        trans.origin.set(vec);
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

    public RigidBody getRigidBody() {
        return this.body;
    }

    public void applyForce(Vector3f... forces) {
        for(Vector3f force : forces) {
            this.body.applyCentralForce(force);
        }
    }

    @Override
    public void remove() {
        super.remove();
        if(this.world.isClient()) {
            ClientInitializer.physicsWorld.remove(this);
        }
    }

    public void rotateX(float deg) {
        Quat4f quat = this.getOrientation();
        QuaternionHelper.rotateX(quat, deg);
        this.setOrientation(quat);
    }

    public void rotateY(float deg) {
        Quat4f quat = this.getOrientation();
        QuaternionHelper.rotateY(quat, deg);
        this.setOrientation(quat);
    }

    public void rotateZ(float deg) {
        Quat4f quat = this.getOrientation();
        QuaternionHelper.rotateZ(quat, deg);
        this.setOrientation(quat);
    }

    @Override
    protected void readCustomDataFromTag(CompoundTag tag) {
        setOrientation(QuaternionHelper.fromTag(tag));
        this.playerID = tag.getUuid("playerID");
    }

    @Override
    protected void writeCustomDataToTag(CompoundTag tag) {
        QuaternionHelper.toTag(getOrientation(), tag);
        tag.putUuid("playerID", this.playerID);
    }

    public RigidBody createRigidBody(Box cBox) {
        Vector3f inertia = new Vector3f(0.0F, 0.0F, 0.0F);
        Vector3f box = new Vector3f(
                ((float) (cBox.maxX - cBox.minX) / 2.0F) + 0.005f,
                ((float) (cBox.maxY - cBox.minY) / 2.0F) + 0.005f,
                ((float) (cBox.maxZ - cBox.minZ) / 2.0F) + 0.005f);
        CollisionShape shape = new BoxShape(box);
        shape.calculateLocalInertia(this.mass, inertia);

        Vec3d pos = this.getPos();
        Vector3f position = new Vector3f((float) pos.x, (float) pos.y, (float) pos.z);

        DefaultMotionState motionState = new DefaultMotionState(new Transform(new Matrix4f(new Quat4f(0, 1, 0, 0), position, 1.0f)));
        RigidBodyConstructionInfo ci = new RigidBodyConstructionInfo(this.mass, motionState, shape, inertia);

        RigidBody body = new RigidBody(ci);
        body.setActivationState(CollisionObject.DISABLE_DEACTIVATION);
        body.setDamping(linearDamping, 0.9f);
        return body;
    }
}
