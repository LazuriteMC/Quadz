package bluevista.fpvracingmod.server.entities;

import bluevista.fpvracingmod.client.ClientInitializer;
import bluevista.fpvracingmod.client.ClientTick;
import bluevista.fpvracingmod.config.Config;
import bluevista.fpvracingmod.helper.QuaternionHelper;
import bluevista.fpvracingmod.network.NetQuat4f;
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
    public static final UUID NULL_UUID = new UUID(0, 0);

    public UUID playerID;
    public NetQuat4f netQuat;
    private RigidBody body;
    private boolean active;

    private float dragCoefficient;
    private float mass;
    private int size;

    public PhysicsEntity(EntityType type, World world, UUID playerID, Vec3d pos, float yaw) {
        super(type, world);

        this.yaw = yaw;
        this.resetPosition(pos.x, pos.y, pos.z);
        this.createRigidBody();
        this.rotateY(180f - yaw);

        this.playerID = playerID;
        this.netQuat = new NetQuat4f(new Quat4f(0, 1, 0, 0));

        if (world.isClient()) {
            ClientInitializer.physicsWorld.add(this);
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (this.world.isClient()) {
            this.active = ClientTick.isPlayerIDClient(playerID);

            prevYaw = yaw;
            prevPitch = pitch;
            yaw = QuaternionHelper.getYaw(getOrientation());
            pitch = QuaternionHelper.getPitch(getOrientation());

            if (active) {
                Vector3f pos = this.getRigidBody().getCenterOfMassPosition(new Vector3f());
                this.updatePosition(pos.x, pos.y, pos.z);
                PhysicsEntityC2S.send(this);
            } else {
                this.netQuat.setPrev(this.getOrientation());
            }
        } else {
            PhysicsEntityS2C.send(this);

            if (this.world.getPlayerByUuid(playerID) == null) {
                this.kill();
            }
        }
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

    public void doAirResistance() {
        Vector3f vec3f = getRigidBody().getLinearVelocity(new Vector3f());
        Vec3d velocity = new Vec3d(vec3f.x, vec3f.y, vec3f.z);
        float k = (ClientInitializer.physicsWorld.airDensity * dragCoefficient * (float) Math.pow(size / 16f, 2)) / 2.0f;

        Vec3d airVec3d = velocity.multiply(k).multiply(velocity.lengthSquared()).negate();
        Vector3f airResistance = new Vector3f((float) airVec3d.x, (float) airVec3d.y, (float) airVec3d.z);
        this.applyForce(airResistance);
    }

    public void setMass(float mass) {
        float old = this.mass;
        this.mass = mass;

        if (old != mass) {
            refreshRigidBody();
        }
    }

    public void setSize(int size) {
        int old = this.size;
        this.size = size;

        if (old != size) {
            refreshRigidBody();
        }
    }

    public float getMass() {
        return this.mass;
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
        for (Vector3f force : forces) {
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
        this.playerID = tag.getUuid(Config.PLAYER_ID);
        setConfigValues(Config.MASS, tag.getFloat(Config.MASS));
        setConfigValues(Config.SIZE, tag.getInt(Config.SIZE));
        setConfigValues(Config.DRAG_COEFFICIENT, tag.getFloat(Config.DRAG_COEFFICIENT));
    }

    @Override
    protected void writeCustomDataToTag(CompoundTag tag) {
        tag.putUuid(Config.PLAYER_ID, this.playerID);
        tag.putFloat(Config.MASS, getConfigValues(Config.MASS).floatValue());
        tag.putInt(Config.SIZE, getConfigValues(Config.SIZE).intValue());
        tag.putFloat(Config.DRAG_COEFFICIENT, getConfigValues(Config.DRAG_COEFFICIENT).floatValue());
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

        if (this.world.isClient()) {
            ClientInitializer.physicsWorld.removeRigidBody(old);
            ClientInitializer.physicsWorld.addRigidBody(this.getRigidBody());
        }
    }

    public void createRigidBody() {
        float s = size / 16.0f;
        Box cBox = new Box(-s / 2.0f, -s / 8.0f, -s / 2.0f, s / 2.0f, s / 8.0f, s / 2.0f);
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
        this.body = body;
    }
}
