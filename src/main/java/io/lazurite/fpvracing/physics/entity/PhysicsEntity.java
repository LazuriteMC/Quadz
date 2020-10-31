package io.lazurite.fpvracing.physics.entity;

import com.bulletphysics.dynamics.RigidBody;
import io.lazurite.fpvracing.network.tracker.GenericDataTrackerRegistry;
import io.lazurite.fpvracing.network.packet.EntityPhysicsS2C;
import io.lazurite.fpvracing.physics.Air;
import io.lazurite.fpvracing.server.ServerInitializer;
import io.lazurite.fpvracing.util.math.QuaternionHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.world.World;

import javax.vecmath.Vector3f;

public abstract class PhysicsEntity extends Entity {
    public static final GenericDataTrackerRegistry.Entry<Integer> PLAYER_ID = GenericDataTrackerRegistry.register("playerId", -1, ServerInitializer.INTEGER_TYPE, PhysicsEntity.class);
    public static final GenericDataTrackerRegistry.Entry<Float> MASS = GenericDataTrackerRegistry.register("mass", 1.0F, ServerInitializer.FLOAT_TYPE, PhysicsEntity.class);
    public static final GenericDataTrackerRegistry.Entry<Integer> SIZE = GenericDataTrackerRegistry.register("size", 8, ServerInitializer.INTEGER_TYPE, PhysicsEntity.class);
    public static final GenericDataTrackerRegistry.Entry<Float> DRAG_COEFFICIENT = GenericDataTrackerRegistry.register("dragCoefficient", 0.5F, ServerInitializer.FLOAT_TYPE, PhysicsEntity.class);

    protected IPhysicsHandler physics;

    public PhysicsEntity(EntityType<?> type, World world) {
        super(type, world);

        if (world.isClient()) {
            physics = new ClientPhysicsHandler(this);
        } else {
            physics = new ServerPhysicsHandler(this);
        }
    }

    @Environment(EnvType.CLIENT)
    public void step(ClientPhysicsHandler physics, float delta) {
        Vector3f direction = physics.getLinearVelocity();
        direction.normalize();
        physics.applyForce(Air.getResistanceForce(
                direction,
                getValue(SIZE),
                getValue(DRAG_COEFFICIENT)
        ));
    }

    public <T> void setValue(GenericDataTrackerRegistry.Entry<T> entry, T value) {
        this.getDataTracker().set(entry.getTrackedData(), value);
    }

    public <T> T getValue(GenericDataTrackerRegistry.Entry<T> entry) {
        T data = getDataTracker().get(entry.getTrackedData());

        if (data == null) {
            return entry.getFallback();
        }

        return data;
    }

    public IPhysicsHandler getPhysics() {
        return physics;
    }

    /**
     * Sets the mass of the physics entity. Also refreshes the {@link RigidBody}.
     * @param mass the new mass
     */
    public void setMass(float mass) {
        float old = getValue(MASS);
        setValue(MASS, mass);

        if (old != mass && world.isClient()) {
            ((ClientPhysicsHandler) physics).createRigidBody();
        }
    }

    /**
     * Sets the size of the physics entity. Also refreshes the {@link RigidBody}.
     * @param size the new size
     */
    public void setSize(int size) {
        float old = getValue(SIZE);
        setValue(SIZE, size);

        if (old != size && world.isClient()) {
            ((ClientPhysicsHandler) physics).createRigidBody();
        }
    }

    /**
     * Sets the player ID used by the physics entity.
     * @param playerID the player ID
     */
    public void setPlayerID(int playerID) {
        setValue(PLAYER_ID, playerID);

        PlayerEntity player = (PlayerEntity) getEntityWorld().getEntityById(playerID);
        if (player != null) {
            setCustomName(new LiteralText(player.getGameProfile().getName()));
            setCustomNameVisible(true);
        }
    }

    public void updatePositionAndAngles(Vector3f position, float yaw, float pitch) {
        this.updatePositionAndAngles(position.x, position.y, position.z, yaw, pitch);
        physics.setPosition(position);
        setYaw(yaw);

        if (!world.isClient()) {
            EntityPhysicsS2C.send(physics, true);
        }
    }

    public void updateEulerRotations() {
        prevYaw = yaw;
        yaw = QuaternionHelper.getYaw(physics.getOrientation());

        while(yaw - prevYaw < -180.0F) {
            prevYaw -= 360.0F;
        }

        while(yaw - prevYaw >= 180.0F) {
            prevYaw += 360.0F;
        }
    }

    @Override
    public void setYaw(float yaw) {
        if (world.isClient()) {
            ((ClientPhysicsHandler) physics).rotateY(yaw);
        }

        this.prevYaw = this.yaw;
        this.yaw = yaw;
    }
}
