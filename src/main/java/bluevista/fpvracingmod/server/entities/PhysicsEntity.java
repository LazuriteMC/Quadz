package bluevista.fpvracingmod.server.entities;

import bluevista.fpvracingmod.client.ClientInitializer;
import bluevista.fpvracingmod.client.ClientTick;
import bluevista.fpvracingmod.config.Config;
import bluevista.fpvracingmod.helper.QuaternionHelper;
import bluevista.fpvracingmod.network.NetQuat4f;
import bluevista.fpvracingmod.network.entity.PhysicsEntityC2S;
import bluevista.fpvracingmod.network.entity.PhysicsEntityS2C;
import bluevista.fpvracingmod.network.entity.RigidBodyS2C;
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
    public static final UUID NULL_UUID = new UUID(0, 0);

    public UUID playerID;
    public NetQuat4f netQuat;

    private float linearDamping;
    private float mass;
    private RigidBody body;
    private boolean active;
    private boolean justSpawned;

    public PhysicsEntity(EntityType type, World world, UUID playerID, Vec3d pos) {
        super(type, world);

        this.setPos(pos.x, pos.y, pos.z);
        this.createRigidBody();

        this.playerID = playerID;
        this.netQuat = new NetQuat4f(this.getOrientation());
        this.justSpawned = true;

        if (world.isClient()) {
            ClientInitializer.physicsWorld.add(this);
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (this.world.isClient()) {
            this.active = ClientTick.isPlayerIDClient(playerID);

            if (active) {
                PhysicsEntityC2S.send(this);
            } else {
                this.netQuat.setPrev(this.getOrientation());
            }
        } else {
            PhysicsEntityS2C.send(this);
            RigidBodyS2C.send(this, justSpawned);

            if (this.world.getPlayerByUuid(playerID) == null) {
                this.kill();
            }
        }

        Vector3f pos = this.getRigidBody().getCenterOfMassPosition(new Vector3f());
        this.updatePosition(pos.x, pos.y, pos.z);

        justSpawned = false;
    }

    @Environment(EnvType.CLIENT)
    public void stepPhysics(float d, float tickDelta) {
        if (!this.isActive()) {
            this.setOrientation(this.netQuat.slerp(tickDelta));
        }
    }

    public void setConfigValues(String key, Number value) {
        switch (key) {
            case Config.MASS:
                this.setMass(value.floatValue());
                break;
            case Config.LINEAR_DAMPING:
                this.setLinearDamping(value.floatValue());
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
            default:
                return null;
        }
    }

    public void setMass(float mass) {
        float old = this.mass;
        this.mass = mass;

        if(old != mass) {
            refreshRigidBody();
        }
    }

    public float getMass() {
        return this.mass;
    }

    public void setLinearDamping(float linearDamping) {
        this.linearDamping = linearDamping;
        this.body.setDamping(linearDamping, this.body.getAngularDamping());
    }

    public float getLinearDamping() {
        return this.linearDamping;
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

        if (this.world.isClient()) {
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
    }

    @Override
    protected void writeCustomDataToTag(CompoundTag tag) {
        QuaternionHelper.toTag(getOrientation(), tag);
        tag.putUuid(Config.PLAYER_ID, this.playerID);

        tag.putFloat(Config.MASS, getConfigValues(Config.MASS).floatValue());
        tag.putFloat(Config.LINEAR_DAMPING, getConfigValues(Config.LINEAR_DAMPING).floatValue());
    }

    @Override
    public void updateTrackedPositionAndAngles(double x, double y, double z, float yaw, float pitch, int interpolationSteps, boolean interpolate) {

    }

    public boolean isActive() {
        return this.active;
    }

    public void refreshRigidBody() {
        RigidBody old = this.getRigidBody();
        this.createRigidBody();

        this.getRigidBody().setLinearVelocity(old.getLinearVelocity(new Vector3f()));
        this.getRigidBody().setAngularVelocity(old.getAngularVelocity(new Vector3f()));
        this.setRigidBodyPos(old.getCenterOfMassPosition(new Vector3f()));
        this.setOrientation(old.getOrientation(new Quat4f()));

        if(this.world.isClient()) {
            ClientInitializer.physicsWorld.removeRigidBody(old);
            ClientInitializer.physicsWorld.addRigidBody(this.getRigidBody());
        }
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
        this.body = body;
    }
}
