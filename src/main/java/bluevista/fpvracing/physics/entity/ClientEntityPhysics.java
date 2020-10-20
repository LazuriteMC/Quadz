package bluevista.fpvracing.physics.entity;

import bluevista.fpvracing.client.ClientInitializer;
import bluevista.fpvracing.client.ClientTick;
import bluevista.fpvracing.client.input.InputTick;
import bluevista.fpvracing.network.entity.EntityPhysicsC2S;
import bluevista.fpvracing.physics.block.BlockCollisions;
import bluevista.fpvracing.server.entities.FlyableEntity;
import bluevista.fpvracing.server.entities.QuadcopterEntity;
import bluevista.fpvracing.util.math.QuaternionHelper;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.Transform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.*;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.util.Arrays;
import java.util.List;

@Environment(EnvType.CLIENT)
public class ClientEntityPhysics implements IEntityPhysics {
    private final Quat4f prevOrientation;
    private final Quat4f netOrientation;
    private FlyableEntity entity;
    private RigidBody body;

    public ClientEntityPhysics(FlyableEntity entity) {
        this.entity = entity;
        this.prevOrientation = new Quat4f(0, 1, 0, 0);
        this.netOrientation = new Quat4f(0, 1, 0, 0);

        this.createRigidBody();
        ClientInitializer.physicsWorld.add(this);
    }

    /**
     * Gets whether the entity is active. It is active when the {@link RigidBody}
     * is in the {@link bluevista.fpvracing.physics.PhysicsWorld}.
     * @return whether or not the entity is active
     */
    public boolean isActive() {
        return entity.age > 1 && ClientTick.isPlayerIDClient(entity.getPlayerID());
    }

    /**
     * Rotate the entity's {@link Quat4f} by the given degrees on the X axis.
     * @param deg degrees to rotate by
     */
    public void rotateX(float deg) {
        Quat4f quat = getOrientation();
        QuaternionHelper.rotateX(quat, deg);
        setOrientation(quat);
    }

    /**
     * Rotate the entity's {@link Quat4f} by the given degrees on the Y axis.
     * @param deg degrees to rotate by
     */
    public void rotateY(float deg) {
        Quat4f quat = getOrientation();
        QuaternionHelper.rotateY(quat, deg);
        setOrientation(quat);
    }

    /**
     * Rotate the entity's {@link Quat4f} by the given degrees on the Z axis.
     * @param deg degrees to rotate by
     */
    public void rotateZ(float deg) {
        Quat4f quat = getOrientation();
        QuaternionHelper.rotateZ(quat, deg);
        setOrientation(quat);
    }

    /**
     * Gets the {@link RigidBody}.
     * @return the drone's current {@link RigidBody}
     */
    public RigidBody getRigidBody() {
        return this.body;
    }

    @Override
    public void sendPackets() {
        if (isActive()) {
            EntityPhysicsC2S.send(this);
        } else {
            setPrevOrientation(getOrientation());
            setOrientation(netOrientation);
        }
    }

    @Override
    public FlyableEntity getEntity() {
        return entity;
    }

    /**
     * Sets the position of the {@link RigidBody}.
     * @param vec the new position
     */
    @Override
    public void setPosition(Vector3f vec) {
        Transform trans = this.body.getWorldTransform(new Transform());
        trans.origin.set(vec);
        this.body.setWorldTransform(trans);
    }

    @Override
    public Vector3f getPosition() {
        return this.body.getCenterOfMassPosition(new Vector3f());
    }

    @Override
    public void setLinearVelocity(Vector3f linearVelocity) {
        this.body.setLinearVelocity(linearVelocity);
    }

    @Override
    public Vector3f getLinearVelocity() {
        return this.body.getLinearVelocity(new Vector3f());
    }

    @Override
    public void setAngularVelocity(Vector3f angularVelocity) {
        this.body.setAngularVelocity(angularVelocity);
    }

    @Override
    public Vector3f getAngularVelocity() {
        return this.body.getAngularVelocity(new Vector3f());
    }

    /**
     * Sets the orientation of the {@link RigidBody}.
     * @param q the new orientation
     */
    @Override
    public void setOrientation(Quat4f q) {
        Transform trans = this.body.getWorldTransform(new Transform());
        trans.setRotation(q);
        this.body.setWorldTransform(trans);
    }

    /**
     * Gets the orientation of the {@link RigidBody}.
     * @return a new {@link Quat4f} containing orientation
     */
    @Override
    public Quat4f getOrientation() {
        return this.body.getWorldTransform(new Transform()).getRotation(new Quat4f());
    }

    /**
     * Sets the previous orientation of the {@link QuadcopterEntity}.
     * @param prevOrientation the new previous orientation
     */
    public void setPrevOrientation(Quat4f prevOrientation) {
        this.prevOrientation.set(prevOrientation);
    }

    /**
     * Gets the previous orientation of the {@link QuadcopterEntity}.
     * @return a new previous orientation
     */
    public Quat4f getPrevOrientation() {
        Quat4f out = new Quat4f();
        out.set(prevOrientation);
        return out;
    }

    /**
     * Sets the orientation received over the network.
     * @param netOrientation the new net orientation
     */
    public void setNetOrientation(Quat4f netOrientation) {
        this.netOrientation.set(netOrientation);
    }

    /**
     * Apply a list of forces. Mostly a convenience method.
     * @param forces an array of forces to apply to the {@link RigidBody}
     */
    public void applyForce(Vector3f... forces) {
        for (Vector3f force : forces) {
            getRigidBody().applyCentralForce(force);
        }
    }

    public void decreaseAngularVelocity() {
        List<RigidBody> bodies = ClientInitializer.physicsWorld.getRigidBodies();
        boolean mightCollide = false;
        float t = 0.25f;

        for (RigidBody body : bodies) {
            if (body != getRigidBody()) {
                Vector3f dist = body.getCenterOfMassPosition(new Vector3f());
                dist.sub(getRigidBody().getCenterOfMassPosition(new Vector3f()));

                if (dist.length() < 1.0f) {
                    mightCollide = true;
                    break;
                }
            }
        }

        if (!mightCollide) {
            getRigidBody().setAngularVelocity(new Vector3f(0, 0, 0));
        } else {
            float it = 1 - InputTick.axisValues.currT;

            if (Math.abs(InputTick.axisValues.currX) * it > t ||
                    Math.abs(InputTick.axisValues.currY) * it > t ||
                    Math.abs(InputTick.axisValues.currZ) * it > t) {
                getRigidBody().setAngularVelocity(new Vector3f(0, 0, 0));
            }
        }
    }

    /**
     * Calculates when block collisions damage the {@link Entity}
     */
    public void calculateBlockDamage() {
        List<Block> damagingBlocks = Arrays.asList(
                Blocks.WATER,
                Blocks.BUBBLE_COLUMN,
                Blocks.LAVA,
                Blocks.FIRE,
                Blocks.SOUL_FIRE,
                Blocks.CAMPFIRE,
                Blocks.SOUL_CAMPFIRE,
                Blocks.CACTUS
        );

        if (entity.isKillable()) {
            // if it's raining
            if (entity.world.hasRain(entity.getBlockPos())) {
                entity.kill();
                return;
            }

            // inside collisions for all damaging blocks
            if (damagingBlocks.contains(entity.world.getBlockState(entity.getBlockPos()).getBlock())) {
                entity.kill();
                return;
            }

            // all direction collisions for certain damaging blocks
            for (Block block : BlockCollisions.getTouchingBlocks(entity, Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)) {
                if (block.is(Blocks.CACTUS)) {
                    entity.kill();
                    return;
                }
            }

            // downward collisions for certain damaging blocks
            for (Block block : BlockCollisions.getTouchingBlocks(entity, Direction.DOWN)) {
                if (block.is(Blocks.MAGMA_BLOCK)) {
                    entity.kill();
                    return;
                }
            }
        }
    }

    /**
     * Creates a new {@link RigidBody} based off of the drone's attributes.
     */
    public void createRigidBody() {
        float s = entity.getSize() / 16.0f;
        Box cBox = new Box(-s / 2.0f, -s / 8.0f, -s / 2.0f, s / 2.0f, s / 8.0f, s / 2.0f);
        Vector3f inertia = new Vector3f(0.0F, 0.0F, 0.0F);
        Vector3f box = new Vector3f(
                ((float) (cBox.maxX - cBox.minX) / 2.0F) + 0.005f,
                ((float) (cBox.maxY - cBox.minY) / 2.0F) + 0.005f,
                ((float) (cBox.maxZ - cBox.minZ) / 2.0F) + 0.005f);
        CollisionShape shape = new BoxShape(box);
        shape.calculateLocalInertia(entity.getMass(), inertia);

        Vec3d pos = entity.getPos();
        Vector3f position = new Vector3f((float) pos.x, (float) pos.y + 0.125f, (float) pos.z);

        DefaultMotionState motionState;
        if (getRigidBody() != null) {
            RigidBody old = getRigidBody();
            motionState = new DefaultMotionState(old.getWorldTransform(new Transform()));
            ClientInitializer.physicsWorld.removeRigidBody(old);
        } else {
            motionState = new DefaultMotionState(new Transform(new Matrix4f(new Quat4f(0, 1, 0, 0), position, 1.0f)));
        }

        RigidBodyConstructionInfo ci = new RigidBodyConstructionInfo(entity.getMass(), motionState, shape, inertia);
        RigidBody body = new RigidBody(ci);
        body.setActivationState(CollisionObject.DISABLE_DEACTIVATION);

        ClientInitializer.physicsWorld.addRigidBody(body);
        this.body = body;
    }
}
