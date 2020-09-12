package bluevista.fpvracingmod.server.entities;

import bluevista.fpvracingmod.client.ClientInitializer;
import bluevista.fpvracingmod.client.ClientTick;
import bluevista.fpvracingmod.config.Config;
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
    public float mass;
    public float linearDamping;
    public float thrust;

    private RigidBody body;
    private GenericBuffer<Quat4f> quatBuf;
    private GenericBuffer<Vector3f> posBuf;
    private Quat4f prevQuat;

    public UUID playerID;
    private boolean active;

    public PhysicsEntity(EntityType type, World world, UUID playerID, Vec3d pos) {
        super(type, world);

        this.playerID = playerID;
        this.setPos(pos.x, pos.y, pos.z);

        this.quatBuf = new GenericBuffer<Quat4f>();
        this.posBuf = new GenericBuffer<Vector3f>();

    }

    @Override
    public void tick() {
        super.tick();

        if (this.world.isClient()) {
            active = ClientTick.isPlayerIDClient(playerID);

            if (active && this.body != null) {
                PhysicsEntityC2S.send(this);
            }

        } else {
            if (this.body == null) {
                this.createRigidBody();
            }
            PhysicsEntityS2C.send(this);

            if (this.world.getPlayerByUuid(playerID) == null) {
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

    public void setConfigValues(String key, Number value) {
        switch (key) {
            case Config.MASS:
                this.mass = value.floatValue();
                break;
            case Config.LINEAR_DAMPING:
                this.linearDamping = value.floatValue();
                break;
            case Config.THRUST:
                this.thrust = value.floatValue();
                break;
            default:
                break;
        }
    }

    public Number getConfigValues(String key) {
        switch (key) {
            case Config.MASS:
                return this.mass;
            case Config.LINEAR_DAMPING:
                return this.linearDamping;
            case Config.THRUST:
                return this.thrust;
            default:
                return null;
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

    public void setRigidBody(RigidBody body) {
        this.body = body;
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
        this.playerID = tag.getUuid(Config.PLAYER_ID);

        setConfigValues(Config.MASS, tag.getFloat(Config.MASS));
        setConfigValues(Config.LINEAR_DAMPING, tag.getFloat(Config.LINEAR_DAMPING));
        setConfigValues(Config.THRUST, tag.getFloat(Config.THRUST));
    }

    @Override
    protected void writeCustomDataToTag(CompoundTag tag) {
        QuaternionHelper.toTag(getOrientation(), tag);
        tag.putUuid(Config.PLAYER_ID, this.playerID);

        tag.putFloat(Config.MASS, getConfigValues(Config.MASS).floatValue());
        tag.putFloat(Config.LINEAR_DAMPING, getConfigValues(Config.LINEAR_DAMPING).floatValue());
        tag.putFloat(Config.THRUST, getConfigValues(Config.THRUST).floatValue());
    }

    @Override
    public void updateTrackedPositionAndAngles(double x, double y, double z, float yaw, float pitch, int interpolationSteps, boolean interpolate) {

    }

    public boolean isActive() {
        return this.active;
    }

    public void createRigidBody() {
        Box cBox = getBoundingBox();
        Vector3f inertia = new Vector3f(0.0F, 0.0F, 0.0F);
        Vector3f box = new Vector3f(
                ((float) (cBox.maxX - cBox.minX) / 2.0F) + 0.005f,
                ((float) (cBox.maxY - cBox.minY) / 2.0F) + 0.005f,
                ((float) (cBox.maxZ - cBox.minZ) / 2.0F) + 0.005f);
        CollisionShape shape = new BoxShape(box);
        shape.calculateLocalInertia(this.mass, inertia);

        Vec3d pos = this.getPos();
        Vector3f position = new Vector3f((float) pos.x, (float) pos.y + 0.125f, (float) pos.z);

        DefaultMotionState motionState = new DefaultMotionState(new Transform(new Matrix4f(new Quat4f(0, 1, 0, 0), position, 1.0f)));
        RigidBodyConstructionInfo ci = new RigidBodyConstructionInfo(this.mass, motionState, shape, inertia);

        RigidBody body = new RigidBody(ci);
        body.setActivationState(CollisionObject.DISABLE_DEACTIVATION);
        body.setDamping(linearDamping, 0);
        setRigidBody(body);
    }
}
