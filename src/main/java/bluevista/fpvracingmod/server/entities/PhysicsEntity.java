package bluevista.fpvracingmod.server.entities;

import bluevista.fpvracingmod.client.ClientInitializer;
import bluevista.fpvracingmod.client.ClientTick;
import bluevista.fpvracingmod.math.QuaternionHelper;
import bluevista.fpvracingmod.network.GenericBuffer;
import bluevista.fpvracingmod.network.entity.PhysicsEntityC2S;
import bluevista.fpvracingmod.network.entity.PhysicsEntityS2C;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.util.UUID;

public abstract class PhysicsEntity extends Entity {
    public final float LINEAR_DAMPING;
    public final float THRUST_NEWTONS;
    public final float MASS;

    private final RigidBody body;
    private GenericBuffer<Quat4f> quatBuf;
    private GenericBuffer<Vector3f> posBuf;
    private Quat4f prevQuat;

    public UUID playerID;
    private boolean active;

    public PhysicsEntity(EntityType type, World world, Vec3d pos) {
        super(type, world);

        this.MASS = 0.750f; // Get from config
        this.LINEAR_DAMPING = 0.3f; // Get from config
        this.THRUST_NEWTONS = 45.0f; // Get from config

        this.setPos(pos.x, pos.y, pos.z);

        this.quatBuf = new GenericBuffer<Quat4f>();
        this.posBuf = new GenericBuffer<Vector3f>();
        this.body = createRigidBody(getBoundingBox());

        if(this.world.isClient()) {
            ClientInitializer.physicsWorld.add(this);
            PhysicsEntityC2S.send(this);
        }
    }

    @Override
    public void tick() {
        super.tick();

        if(this.world.isClient()) {
            active = ClientTick.isPlayerIDClient(playerID);

            if(active) {
                PhysicsEntityC2S.send(this);
            }

        } else {
            PhysicsEntityS2C.send(this);

            if(this.world.getPlayerByUuid(playerID) == null) {
                this.kill();
            }
        }

        Vector3f pos = this.getRigidBody().getCenterOfMassPosition(new Vector3f());
        this.updatePosition(pos.x, pos.y, pos.z);
    }

    @Environment(EnvType.CLIENT)
    public void stepPhysics(float d) {
        if(playerID != null) {
            if (ClientInitializer.isPlayerIDClient(playerID)) {
                this.quatBuf.setCaptureRate(d);
                this.quatBuf.add(this.getOrientation());

                this.posBuf.setCaptureRate(d);
                this.posBuf.add(this.getRigidBody().getCenterOfMassPosition(new Vector3f()));
            } else {
                if (quatBuf != null) {
                    if (quatBuf.size() > 0) {
                        prevQuat = quatBuf.getLast();

                        Quat4f quat = quatBuf.poll(d);
                        if(quat != null) {
                            this.setOrientation(quat);
                        }
                    }
                }

                if (posBuf != null) {
                    if (posBuf.size() > 0) {
                        Vector3f pos = posBuf.poll(d);
                        if(pos != null) {
                            this.setRigidBodyPos(pos);
                        }
                    }
                }
            }
        }
    }

    public void lerpOrientation(float tickDelta) {
        if(prevQuat != null) {
            this.setOrientation(QuaternionHelper.lerp(tickDelta, prevQuat, getOrientation()));
        }
    }

    public GenericBuffer<Quat4f> getQuaternionBuffer() {
        return this.quatBuf;
    }

    public void setQuaternionBuffer(GenericBuffer<Quat4f> quatBuf) {
        this.quatBuf.set(quatBuf);
    }

    public GenericBuffer<Vector3f> getPositionBuffer() {
        return this.posBuf;
    }

    public void setPositionBuffer(GenericBuffer<Vector3f> posBuf) {
        this.posBuf.set(posBuf);
    }

    public BlockPos getRigidBodyBlockPos() {
        Vector3f vec = this.body.getCenterOfMassPosition(new Vector3f());
        return new BlockPos(vec.x, vec.y, vec.z);
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
    public void addVelocity(double x, double y, double z) {
        this.getRigidBody().applyCentralForce(new Vector3f((float) x, (float) y, (float) z));
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

    @Override
    public void updateTrackedPositionAndAngles(double x, double y, double z, float yaw, float pitch, int interpolationSteps, boolean interpolate) {

    }

    public boolean isActive() {
        return this.active;
    }

    public RigidBody createRigidBody(Box cBox) {
        Vector3f inertia = new Vector3f(0.0F, 0.0F, 0.0F);
        Vector3f box = new Vector3f(
                ((float) (cBox.maxX - cBox.minX) / 2.0F) + 0.005f,
                ((float) (cBox.maxY - cBox.minY) / 2.0F) + 0.005f,
                ((float) (cBox.maxZ - cBox.minZ) / 2.0F) + 0.005f);
        CollisionShape shape = new BoxShape(box);
        shape.calculateLocalInertia(this.MASS, inertia);

        Vec3d pos = this.getPos();
        Vector3f position = new Vector3f((float) pos.x, (float) pos.y + 0.125f, (float) pos.z);

        DefaultMotionState motionState = new DefaultMotionState(new Transform(new Matrix4f(new Quat4f(0, 1, 0, 0), position, 1.0f)));
        RigidBodyConstructionInfo ci = new RigidBodyConstructionInfo(this.MASS, motionState, shape, inertia);

        RigidBody body = new RigidBody(ci);
        body.setActivationState(CollisionObject.DISABLE_DEACTIVATION);
        body.setDamping(LINEAR_DAMPING, 0);
        return body;
    }
}
